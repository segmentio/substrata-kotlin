package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.JavaCallback
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Function
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8ScriptCompilationException
import com.eclipsesource.v8.V8ScriptExecutionException
import com.eclipsesource.v8.V8Value
import com.segment.analytics.substrata.kotlin.JSEngineError
import com.segment.analytics.substrata.kotlin.JSValue
import com.segment.analytics.substrata.kotlin.JavascriptEngine
import com.segment.analytics.substrata.kotlin.JavascriptErrorHandler
import io.alicorn.v8.V8JavaAdapter
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

/**
 * J2V8Engine singleton.  Due to the performance cost of creating runtimes in J2V8,
 * we'll use a singleton primarily; though it is possible to create an instance
 * of your own as well.
 *
 * Most of the APIs do a good job of managing memory, but there are some exceptions.
 * Expose, Extend, Execute, Call all *can* have potential side-effects and create
 * memory so we should not use _memScope_ to manage memory automatically.
 */
class J2V8Engine(private val timeoutInSeconds: Long = 120L) : JavascriptEngine {

    companion object {
        val shared: J2V8Engine by lazy {
            J2V8Engine()
        }
    }

    override lateinit var bridge: J2V8DataBridge

    // All interaction with underlying runtime must be synchronized on the jsExecutor
    internal val jsExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    lateinit var underlying: V8

    // Main errorHandler that can be set by user. This allows us to handle any exceptions,
    // without worrying about propagating errors to the application and crashing it
    var errorHandler: JavascriptErrorHandler? = null

    init {
        jsExecutor.await {
            underlying = V8.createV8Runtime()
            // Following APIs are being called on jsExecutor and should not explicitly use jsExecutor
            setupConsole()
            setupDataBridge()
        }
    }

    override fun release() {
        underlying.release(false)
    }

    override fun loadBundle(bundleStream: InputStream, completion: (JSEngineError?) -> Unit) {
        var jsError: JSEngineError? = null
        val script: String? = BufferedReader(bundleStream.reader()).use {
            try {
                it.readText()
            } catch (e: IOException) {
                jsError = JSEngineError.UnableToLoad
                null
            }
        }

        script?.let {
            jsExecutor.sync {
                underlying.executeScript(it)
            }
        }
        completion(jsError)
    }

    override fun get(key: String): JSValue {
        val r = jsExecutor.await {
            underlying.memScope {
                var result: Any? = null
                underlying.get(key).let { value ->
                    if (!value.isNull() && !value.isUndefined()) {
                        result = value
                    } else {
                        underlying.executeScript(key).let { v ->
                            if (!v.isNull()) {
                                result = v
                            }
                        }
                    }
                }
                wrapAsJSValue(result)
            }
        }
        return r
    }

    override fun set(key: String, value: JSValue) {
        jsExecutor.sync {
            underlying.memScope {
                when (value) {
                    is JSValue.JSString -> {
                        underlying.add(key, value.content)
                    }
                    is JSValue.JSBool -> {
                        underlying.add(key, value.content)
                    }
                    is JSValue.JSDouble -> {
                        underlying.add(key, value.content)
                    }
                    is JSValue.JSInt -> {
                        underlying.add(key, value.content)
                    }
                    is JSValue.JSObject -> {
                        val jsRep = value.content
                        jsRep?.let {
                            underlying.add(key, underlying.toV8Object(it))
                        }
                    }
                    is JSValue.JSArray -> {
                        val jsRep = value.content
                        jsRep?.let {
                            underlying.add(key, underlying.toV8Array(it))
                        }
                    }
                    is JSValue.JSFunction -> {
                        // Essentially doing what expose does
                        underlying.registerJavaMethod(value.fn, key)
                    }
                    JSValue.JSUndefined -> {
                        // omit no point in setting a key with undefined value, right?
                        underlying.addUndefined(key)
                    }
                }
            }
        }
    }

    override fun <T : Any> expose(key: String, value: T) {
        jsExecutor.sync {
            V8JavaAdapter.injectObject(key, value, underlying)
        }
    }

    override fun <C : Any> expose(clazz: KClass<C>, className: String) {
        jsExecutor.sync {
            V8JavaAdapter.injectClass(className, clazz.java, underlying)
        }
    }

    override fun expose(function: JSValue.JSFunction, functionName: String) {
        jsExecutor.sync {
            underlying.registerJavaMethod(function.fn, functionName)
        }
    }

    override fun extend(objectName: String, function: JSValue.JSFunction, functionName: String) {
        /*
          If already exists
          -> if an object, extend it
          -> else, reportError
          else create it
         */
        jsExecutor.sync {
            val v8Obj: V8Object? = underlying.get(objectName).let { value ->
                when {
                    value.isNull() || value.isUndefined() -> {
                        V8Object(underlying)
                    }
                    value is V8Object -> {
                        value
                    }
                    else -> {
                        reportError(
                            JSEngineError.EvaluationError(
                                "TypeError",
                                "attempting to add fn to a non-object value",
                                "$functionName cannot be added to $objectName"
                            )
                        )
                        null
                    }
                }
            }
            v8Obj?.let {
                it.registerJavaMethod(function.fn, functionName)
                underlying.add(objectName, it)
            }
        }
    }

    override fun call(function: String, params: List<JSValue>): JSValue {
        val result = jsExecutor.await {
            val parameters = createV8Array(params)
            val res = wrapAsJSValue(underlying.executeFunction(function, parameters))
            releaseV8Array(parameters)
            res
        }
        return result
    }

    override fun call(function: JSValue.JSFunction, params: List<JSValue>): JSValue {
        val result = jsExecutor.await {
            val parameters = createV8Array(params)
            val res = wrapAsJSValue(function.fn.invoke(null, parameters))
            releaseV8Array(parameters)
            res
        }
        return result
    }

    override fun call(
        receiver: JSValue.JSObjectReference,
        function: String,
        params: List<JSValue>
    ): JSValue {
        val result = jsExecutor.await {
            val parameters = createV8Array(params)
            val res = wrapAsJSValue(receiver.ref.executeFunction(function, parameters))
            releaseV8Array(parameters)
            res
        }
        return result
    }

    override fun execute(script: String): JSValue {
        val result = jsExecutor.await {
            val r = underlying.executeScript(script)
            wrapAsJSValue(r)
        }
        return result
    }

    /**
     * Internal API to synchronize interactions with the V8 runtime,
     * and return value wrapped as a JSValue
     */
    internal fun syncRunEngine(closure: (V8) -> Any?): JSValue {
        val result = syncRun(closure)
        return jsExecutor.await { wrapAsJSValue(result) }
    }

    /*
    * PRIVATE APIs
    * Note: Since all public APIs are implicitly synchronized on the jsExecutor (single-threaded)
    *       ensure that any further interactions do not cause a deadlock
    * */

    private fun <T> syncRun(closure: (V8) -> T?): T? {
        val result = jsExecutor.await {
            closure(underlying)
        }
        return result
    }

    private fun reportError(error: JSEngineError) {
        errorHandler?.let { it(error) }
    }

    private fun ExecutorService.sync(runnable: Runnable) {
        try {
            submit(runnable).get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            processException(ex)
        }
    }

    private fun <T> ExecutorService.await(callable: Callable<T>): T {
        return try {
            submit(callable).get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            processException(ex) as T
        }
    }

    // wrap exception in JSEngineError and return undefined if its a reference error
    private fun processException(ex: Exception): JSValue {
        var returnVal: JSValue = JSValue.JSUndefined
        when (ex) {
            is ExecutionException -> {
                when (val cause = ex.cause) {
                    is V8ScriptExecutionException -> {
                        reportError(
                            JSEngineError.EvaluationError(
                                cause.jsMessage,
                                cause.jsStackTrace,
                                cause.toString()
                            )
                        )
                    }
                    is V8ScriptCompilationException -> {
                        val type = cause.jsMessage.substringBefore(":")
                        if (type == "ReferenceError") {
                            // ReferenceError signifies undefined value
                            returnVal = JSValue.JSUndefined
                        } else {
                            reportError(
                                JSEngineError.EvaluationError(
                                    type,
                                    cause.jsStackTrace,
                                    cause.toString()
                                )
                            )
                        }
                    }
                    else -> reportError(JSEngineError.UnknownError(ex))
                }
            }
            is TimeoutException -> {
                reportError(JSEngineError.TimeoutError(ex.message ?: ""))
            }
            else -> {
                reportError(JSEngineError.UnknownError(ex))
            }
        }
        return returnVal
    }

    // Use this API for wrapping values coming from J2V8
    // Must be run on the J2V8 thread
    internal fun wrapAsJSValue(obj: Any?): JSValue {
        return when (obj) {
            null -> return JSValue.JSNull
            is Boolean -> JSValue.JSBool(obj)
            is Int -> JSValue.JSInt(obj)
            is Double -> JSValue.JSDouble(obj)
            is String -> JSValue.JSString(obj)
            is V8Function -> {
                // We are wrapping the v8Function invoke, but not synchronizing on the
                // underlying runtime, bcos the Engine has a call api that will do it for us
                // Also a potential pitfall is that someone exposes this returned wrapped
                // callback, which is gonna be another wrapping, but y would anyone do that
                val cb = JavaCallback { rec, params ->
                    obj.call(rec, params)
                }
                JSValue.JSFunction(cb)
            }
            is V8Array -> JSValue.JSArray(obj)
            is V8Object -> JSValue.JSObject(obj)
            else -> JSValue.JSUndefined
        }
    }


    /* ===========================================================================
    APIs being called on the jsExecutor and should not be synchronized explicitly
    ============================================================================== */
    private fun setupConsole() {
        // TODO add more versatility + Android log support
        val v8Console = V8Object(underlying)
        v8Console.registerJavaMethod({ _, v8Array ->
            val msg = jsValueToString(v8Array[0])
            println("[JSConsole.I] - $msg")
        }, "log")
        v8Console.registerJavaMethod({ _, v8Array ->
            val msg = jsValueToString(v8Array)
            println("[JSConsole.E] - $msg")
        }, "err")
        underlying.add("console", v8Console)
    }

    private fun setupDataBridge() {
        bridge = J2V8DataBridge(this)
    }

}

fun J2V8Engine.expose(function: JavaCallback, functionName: String) {
    expose(JSValue.JSFunction(function), functionName)
}

fun J2V8Engine.extend(objectName: String, function: JavaCallback, functionName: String) {
    extend(objectName, JSValue.JSFunction(function), functionName)
}

private fun Any?.isNull(): Boolean {
    return this == null
}

private fun Any?.isUndefined(): Boolean {
    return (this as? V8Value)?.isUndefined ?: false
}

private fun jsValueToString(value: Any?): String {
    return when (value) {
        is Boolean -> value.toString()
        is Int -> value.toString()
        is Double -> value.toString()
        is String -> value.toString()
        is V8Array -> buildString {
            append("[")
            val length = value.length()
            for (i in 0 until length) {
                append(jsValueToString(value.get(i)))
                if (i != length - 1) append(", ")
            }
            append("]")
        }
        is V8Object -> buildString {
            append("{")
            val iterator = value.keys.iterator()
            for (key in iterator) {
                append(key); append(":")
                append(jsValueToString(value.get(key)))
                if (iterator.hasNext()) append(", ")
            }
            append("}")
        }
        null -> "null"
        else -> "undefined"
    }
}

private fun J2V8Engine.createV8Array(params: List<JSValue>): V8Array {
    return V8Array(underlying).apply {
        params.forEach { value ->
            when (value) {
                is JSValue.JSString -> push(value.content)
                is JSValue.JSBool -> push(value.content)
                is JSValue.JSInt -> push(value.content)
                is JSValue.JSDouble -> push(value.content)
                is JSValue.JSFunction -> {
                    val fn = V8Function(underlying, value.fn)
                    push(fn)
                }
                is JSValue.JSArray -> {
                    val jsRep = value.content
                    jsRep?.let {
                        push(underlying.toV8Array(it))
                    }
                }
                is JSValue.JSObject -> {
                    val jsRep = value.content
                    jsRep?.let {
                        push(underlying.toV8Object(it))
                    }
                }
                is JSValue.JSUndefined -> pushUndefined()
            }
        }
    }
}

private fun releaseV8Array(v8Array: V8Array) {
    for (i in 0 until v8Array.length()) {
        var v8Val: Any? = null
        v8Val = v8Array.get(i)
        if (v8Val is Closeable) {
            v8Val.close()
        }
    }
    v8Array.close()
}
