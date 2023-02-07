package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
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
class JSEngine(private val timeoutInSeconds: Long = 120L) {

    companion object {
        val shared: JSEngine by lazy {
            JSEngine()
        }
    }

    lateinit var bridge: JSDataBridge

    internal var runtime: V8 = V8.createV8Runtime()

    // Main errorHandler that can be set by user. This allows us to handle any exceptions,
    // without worrying about propagating errors to the application and crashing it
    var errorHandler: JavascriptErrorHandler? = null

    init {
        // Following APIs are being called on jsExecutor and should not explicitly use jsExecutor
        setupConsole()
        setupDataBridge()
    }

    fun release() {
        runtime.release(false)
    }

    fun loadBundle(bundleStream: InputStream, completion: (JSEngineError?) -> Unit) {
        var jsError: JSEngineError? = null
        val script: String? = BufferedReader(bundleStream.reader()).use {
            try {
                it.readText()
            } catch (e: IOException) {
                jsError = JSEngineError.UnableToLoad
                null
            }
        }

        runtime.executeScript(script)
        completion(jsError)
    }

    operator fun get(key: String) = runtime.memScope {
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


    operator fun set(key: String, value: Boolean) {
        runtime.add(key, value)
    }

    operator fun set(key: String, value: Int) {
        runtime.add(key, value)
    }

    operator fun set(key: String, value: Double) {
        runtime.add(key, value)
    }

    operator fun set(key: String, value: String) {
        runtime.add(key, value)
    }

    operator fun set(key: String, value: JsonElement) {
        val converted = JsonElementConverter.write(value, this)
        runtime.add(key, converted)
    }

    operator fun set(key: String, value: JSConvertible) {
        val converted = value.convert(this)
        runtime.add(key, converted)
    }

    fun <T : JSExport> export(obj : T, objectName: String) {
        V8JavaAdapter.injectObject(objectName, obj, runtime)
    }

    fun <T : JSExport> export(clazz: KClass<T>, className: String) {
        V8JavaAdapter.injectClass(className, clazz.java, runtime)
    }

    fun export(function: JSFunction, functionName: String) {
        runtime.registerJavaMethod(function.callBack, functionName)
    }

    fun extend(objectName: String, function: JSFunction, functionName: String) {
        /*
          If already exists
          -> if an object, extend it
          -> else, reportError
          else create it
         */
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

    fun call(function: String, params: JSArray): JSResult {
        val parameters = params.content
        val rawResult = runtime.executeFunction(function, parameters)
        parameters.close()
        return JSResult(rawResult)
    }

    fun call(function: JSFunction, params: JSArray): JSResult {
        val parameters = params.content
        val rawResult = function.callBack.invoke(null, parameters)
        parameters.close()
        return JSResult(rawResult)
    }

    fun call(
        jsObject: JSObject,
        function: String,
        params: JSArray
    ): JSResult {
        val parameters = params.content
        val obj = jsObject.content
        val rawResult = obj.executeFunction(function, parameters)
        parameters.close()
        return JSResult(rawResult)
    }

    fun evaluate(script: String): JSResult {
        val r = runtime.executeScript(script)
        return JSResult(r)
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
        bridge = JSDataBridge(this)
    }

}

sealed class JSEngineError : Exception() {
    object BundleNotFound : JSEngineError()
    object UnableToLoad : JSEngineError()
    class UnknownError(val error: Exception) : JSEngineError()
    class EvaluationError(
        val type: String,
        val stackTrace: String,
        val causeDetails: String
    ) : JSEngineError()

    class TimeoutError(val msg: String) : JSEngineError()
}

typealias JavascriptErrorHandler = (JSEngineError) -> Unit