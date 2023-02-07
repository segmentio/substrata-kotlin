package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import io.alicorn.v8.V8JavaAdapter
import kotlinx.serialization.json.JsonElement
import java.io.BufferedReader
import java.io.InputStream
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
class JSEngine internal constructor(private val timeoutInSeconds: Long = 120L) {

    lateinit var bridge: JSDataBridge

    internal var runtime: V8 = V8.createV8Runtime()

    init {
        // Following APIs are being called on jsExecutor and should not explicitly use jsExecutor
        setupConsole()
        setupDataBridge()
    }

    fun release() {
        runtime.release(false)
    }

    fun loadBundle(bundleStream: InputStream) {
        val script: String = BufferedReader(bundleStream.reader()).readText()
        runtime.executeScript(script)
    }

    operator fun get(key: String) = runtime.memScope {
        var result: Any = V8.getUndefined()
        runtime.get(key).let { value ->
            if (value != null && value != V8.getUndefined()) {
                result = value
            } else try {
                runtime.executeScript(key)?.let { v ->
                    result = v
                }
            } catch (_ : Exception) {}
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

    fun <T: JSConvertible> set(key: String, value: T, converter: JSConverter<T>) {
        val converted = converter.write(value, this)
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
                else ->
                    throw Exception(
                        "attempting to add fn to a non-object value. $functionName cannot be added to $objectName"
                    )
            }
        }
        v8Obj?.let {
            it.registerJavaMethod(function.callBack, functionName)
            runtime.add(objectName, it)
        }
    }

    fun call(function: String, params: JSArray? = null): JSResult {
        val parameters = params?.content
        val rawResult = runtime.executeFunction(function, parameters)
        parameters?.close()
        return JSResult(rawResult)
    }

    fun call(function: JSFunction, params: JSArray? = null): JSResult {
        val parameters = params?.content
        val rawResult = function.callBack.invoke(null, parameters)
        parameters?.close()
        return JSResult(rawResult)
    }

    fun call(
        jsObject: JSObject,
        function: String,
        params: JSArray? = null
    ): JSResult {
        val parameters = params?.content
        val obj = jsObject.content
        val rawResult = obj.executeFunction(function, parameters)
        parameters?.close()
        return JSResult(rawResult)
    }

    fun evaluate(script: String): JSResult {
        val r = runtime.executeScript(script)
        return JSResult(r)
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