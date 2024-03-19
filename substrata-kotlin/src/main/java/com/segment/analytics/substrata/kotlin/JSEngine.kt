package com.segment.analytics.substrata.kotlin

import java.io.BufferedReader
import java.io.InputStream

/**
 * J2V8Engine singleton.  Due to the performance cost of creating runtimes in J2V8,
 * we'll use a singleton primarily; though it is possible to create an instance
 * of your own as well.
 *
 * Most of the APIs do a good job of managing memory, but there are some exceptions.
 * Expose, Extend, Execute, Call all *can* have potential side-effects and create
 * memory so we should not use _memScope_ to manage memory automatically.
 */
class JSEngine internal constructor(
    private val runtime: JSRuntime,
    val context: JSContext = runtime.createJSContext(),
    private val global: JSObject = context.getGlobalObject(),
    private val timeoutInSeconds: Long = 120L
): Releasable, KeyValueObject by global {

    val bridge: JSDataBridge = JSDataBridge(this)

    val JSNull get() = context.JSNull

    val JSUndefined get() = context.JSUndefined

    constructor(): this(QuickJS.createJSRuntime())

    override fun release() {
        context.release()
        runtime.release()
    }

    fun loadBundle(bundleStream: InputStream) {
        val script: String = BufferedReader(bundleStream.reader()).readText()
        context.executeScript(script)
    }

    override operator fun get(key: String): Any? {
        var result: Any? = context.JSUndefined
        global[key].let { value ->
            if (value != context.JSNull && value != context.JSUndefined) {
                result = value
            } else try {
                context.executeScript(key).let { v ->
                    result = v
                }
            } catch (_ : Exception) {}
        }
        return result
    }

//
//    fun <T : JSExport> export(objectName: String, obj : T) {
//        V8JavaAdapter.injectObject(objectName, obj, runtime)
//    }
//
//    fun <T : JSExport> export(className: String, clazz: KClass<T>) {
//        V8JavaAdapter.injectClass(className, clazz.java, runtime)
//    }
//
//    fun export(functionName: String, function: JSFunction) {
//        runtime.registerJavaMethod(function.callBack, functionName)
//    }
//
//    fun extend(objectName: String, functionName: String, function: JSFunction) {
//        /*
//          If already exists
//          -> if an object, extend it
//          -> else, reportError
//          else create it
//         */
//        val v8Obj: V8Object = runtime.get(objectName).let { value ->
//            when (value) {
//                null, V8.getUndefined() -> {
//                    V8Object(runtime)
//                }
//                is V8Object -> {
//                    value
//                }
//                else ->
//                    throw Exception(
//                        "attempting to add fn to a non-object value. $functionName cannot be added to $objectName"
//                    )
//            }
//        }
//        v8Obj.let {
//            it.registerJavaMethod(function.callBack, functionName)
//            runtime.add(objectName, it)
//        }
//    }

//    fun call(function: String, params: JSArray? = null) = context.executeFunction(function, params)

//    fun call(function: JSFunction, params: JSArray? = null): JSResult {
//        val parameters = params?.content
//        val rawResult = function.callBack.invoke(null, parameters)
//        parameters?.close()
//        return JSResult(rawResult)
//    }
//
//    fun call(
//        jsObject: JSObject,
//        function: String,
//        params: JSArray? = null
//    ): JSResult {
//        val parameters = params?.content
//        val obj = jsObject.content
//        val rawResult = obj.executeFunction(function, parameters)
//        parameters?.close()
//        return JSResult(rawResult)
//    }

    fun evaluate(script: String) = context.executeScript(script)

//
//    /* ===========================================================================
//    APIs being called on the jsExecutor and should not be synchronized explicitly
//    ============================================================================== */
//    private fun setupConsole() {
//        // TODO add more versatility + Android log support
//        val v8Console = JSObject()
//        v8Console.registerJavaMethod({ _, v8Array ->
//            val msg = v8Array[0].toString()
//            println("[JSConsole.I] - $msg")
//        }, "log")
//        v8Console.registerJavaMethod({ _, v8Array ->
//            val msg = v8Array.toString()
//            println("[JSConsole.E] - $msg")
//        }, "err")
//        runtime.add("console", v8Console)
//    }

}