package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.*
import com.segment.analytics.substrata.kotlin.*
import io.alicorn.v8.V8JavaAdapter
import kotlinx.serialization.json.JsonElement
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.*
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
    internal lateinit var runtime: V8

    // Main errorHandler that can be set by user. This allows us to handle any exceptions,
    // without worrying about propagating errors to the application and crashing it
    var errorHandler: JavascriptErrorHandler? = null

    init {
        jsExecutor.await {
            runtime = V8.createV8Runtime()
            // Following APIs are being called on jsExecutor and should not explicitly use jsExecutor
            setupConsole()
            setupDataBridge()
        }
    }

    override fun release() {
        runtime.release(false)
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
                runtime.executeScript(it)
            }
        }
        completion(jsError)
    }

    override operator fun get(key: String) = jsExecutor.await {
        runtime.memScope {
            var result: Any = V8.getUndefined()
            runtime.get(key).let { value ->
                if (value != null && value != V8.getUndefined()) {
                    result = value
                } else {
                    runtime.executeScript(key)?.let { v ->
                        result = v
                    }
                }
            }
            JSResult(result)
        }
    }

    override fun set(key: String, value: Boolean) {
        runtime.add(key, value)
    }

    override fun set(key: String, value: Int) {
        runtime.add(key, value)
    }

    override fun set(key: String, value: Double) {
        runtime.add(key, value)
    }

    override operator fun set(key: String, value: String) {
        runtime.add(key, value)
    }

    override fun set(key: String, value: JsonElement) {
        val converted = JsonElementConverter.write(value, this)
        require(converted is V8Value)
        runtime.add(key, value)
    }

    override fun set(key: String, value: JSConvertible) {
        val converted = value.convert(this)
        require(converted is V8Value)
        runtime.add(key, value)
    }

    override fun <T : JSExport> export(obj : T, objectName: String) {
        jsExecutor.sync {
            V8JavaAdapter.injectObject(objectName, obj, runtime)
        }
    }

    override fun <T : JSExport> export(clazz: KClass<T>, className: String) {
        jsExecutor.sync {
            V8JavaAdapter.injectClass(className, clazz.java, runtime)
        }
    }

    override fun export(function: JSFunction, functionName: String) {
        jsExecutor.sync {
            runtime.registerJavaMethod(function.callBack, functionName)
        }
    }

    override fun extend(objectName: String, function: JSFunction, functionName: String) {
        /*
          If already exists
          -> if an object, extend it
          -> else, reportError
          else create it
         */
        jsExecutor.sync {
            val v8Obj: V8Object? = runtime.get(objectName).let { value ->
                when (value) {
                    null, V8.getUndefined() -> {
                        V8Object(runtime)
                    }
                    is V8Object -> {
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
                it.registerJavaMethod(function.callBack, functionName)
                runtime.add(objectName, it)
            }
        }
    }

    override fun call(function: String, params: JSArray): JSResult = jsExecutor.await {
        val parameters = params.content
        val rawResult = runtime.executeFunction(function, parameters)
        parameters.close()
        JSResult(rawResult)
    }

    override fun call(function: JSFunction, params: JSArray): JSResult = jsExecutor.await {
        val parameters = params.content
        val rawResult = function.callBack.invoke(null, parameters)
        parameters.close()
        JSResult(rawResult)
    }

    override fun call(
        jsObject: JSObject,
        function: String,
        params: JSArray
    ): JSResult = jsExecutor.await {
        val parameters = params.content
        val obj = jsObject.content
        val rawResult = obj.executeFunction(function, parameters)
        parameters.close()
        JSResult(rawResult)
    }

    override fun evaluate(script: String): JSResult {
        val result = jsExecutor.await {
            val r = runtime.executeScript(script)
            JSResult(r)
        }
        return result
    }

    /**
     * Internal API to synchronize interactions with the V8 runtime,
     * and return value wrapped as a JSValue
     */
    internal fun syncRunEngine(closure: (V8) -> Any): JSResult {
        val result = syncRun(closure)
        return jsExecutor.await { JSResult(result) }
    }

    /*
    * PRIVATE APIs
    * Note: Since all public APIs are implicitly synchronized on the jsExecutor (single-threaded)
    *       ensure that any further interactions do not cause a deadlock
    * */

    private fun <T> syncRun(closure: (V8) -> T): T {
        val result = jsExecutor.await {
            closure(runtime)
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
    private fun processException(ex: Exception) {
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
                        if (type != "ReferenceError") {
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
    }


    /* ===========================================================================
    APIs being called on the jsExecutor and should not be synchronized explicitly
    ============================================================================== */
    private fun setupConsole() {
        // TODO add more versatility + Android log support
        val v8Console = V8Object(runtime)
        v8Console.registerJavaMethod({ _, v8Array ->
            val msg = v8Array[0].toString()
            println("[JSConsole.I] - $msg")
        }, "log")
        v8Console.registerJavaMethod({ _, v8Array ->
            val msg = v8Array.toString()
            println("[JSConsole.E] - $msg")
        }, "err")
        runtime.add("console", v8Console)
    }

    private fun setupDataBridge() {
        bridge = J2V8DataBridge(this)
    }

}
