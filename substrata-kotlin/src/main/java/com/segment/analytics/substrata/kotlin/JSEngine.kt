package com.segment.analytics.substrata.kotlin

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
class JSEngine private constructor(
    private val runtime: JSRuntime,
    val context: JSContext = runtime.createJSContext(),
    private val global: JSObject = context.getGlobalObject(),
    private val timeoutInSeconds: Long = 120L
): Releasable, KeyValueObject by global {

    val bridge: JSDataBridge = JSDataBridge(this)

    val JSNull get() = context.JSNull

    val JSUndefined get() = context.JSUndefined

    internal constructor(): this(QuickJS.createJSRuntime())

    init {
        setupConsole()
    }

    override fun release() {
        context.release()
        runtime.release()
    }

    fun loadBundle(bundleStream: InputStream) {
        val script: String = BufferedReader(bundleStream.reader()).readText()
        context.evaluate(script)
    }

    override operator fun get(key: String): Any? {
        var result: Any? = context.JSUndefined
        global[key].let { value ->
            if (value != context.JSNull && value != context.JSUndefined) {
                result = value
            } else try {
                context.evaluate(key).let { v ->
                    if (v is JSException) {
                        return JSUndefined
                    }
                    result = v
                }
            } catch (_ : Exception) {}
        }
        return result
    }


    fun export(obj : Any, className: String, objectName: String, filter: Set<String> = emptySet()) {
        val jsClass = export(className, obj::class, filter)

        jsClass.instance = obj
        val code = "let $objectName = new ${className}(); $objectName"
        evaluate(code)
        jsClass.instance = null
    }

    fun export(className: String, clazz: KClass<*>, filter: Set<String> = emptySet()) =
        global.register(className, JSClass(context, clazz, filter))


    fun export(function: String, overwrite: Boolean = false, body: JSFunctionBody) = extend(global, function, overwrite, body)

    fun extend(obj: JSObject, function: String, overwrite: Boolean = false, body: JSFunctionBody) = obj.register(function, overwrite, body)

    fun extend(objectName: String, functionName: String, overwrite: Boolean = false, body: JSFunctionBody) {
        /*
          If already exists
          -> if an object, extend it
          -> else, reportError
          else create it
         */
        val jsObj: JSObject = get(objectName).let { value ->
            when (value) {
                JSNull, JSUndefined, is JSException -> {
                    val newObj = context.newObject()
                    global[objectName] = newObj
                    newObj
                }
                is JSObject -> {
                    value
                }
                else ->
                    throw Exception(
                        "attempting to add fn to a non-object value. $functionName cannot be added to $objectName"
                    )
            }
        }
        extend(jsObj, functionName, overwrite, body)
    }

    fun call(function: String, vararg params: Any) = call(global, function, *params)

    fun call(objectName: String, function: String, vararg params: Any): Any {
        val jsObj = get(objectName)
        if (jsObj is JSObject) {
            return call(jsObj, function, *params)
        }
        else throw Exception("$objectName does not exist")
    }

    fun call(
        jsObject: JSObject,
        function: String,
        vararg params: Any
    ): Any {
        val func: JSFunction = context.getProperty(jsObject, function)
        return func(global, *params)
    }

    fun evaluate(script: String) = context.evaluate(script)


    /* ===========================================================================
    APIs being called on the jsExecutor and should not be synchronized explicitly
    ============================================================================== */
    private fun setupConsole() {
        val v8Console = context.newObject()
        v8Console.register("log") {
            val msg = it[0].toString()
            println("[JSConsole.I] - $msg")
        }
        v8Console.register("err") {
            val msg = it.toString()
            println("[JSConsole.E] - $msg")
        }
        this["console"] = v8Console
    }

}