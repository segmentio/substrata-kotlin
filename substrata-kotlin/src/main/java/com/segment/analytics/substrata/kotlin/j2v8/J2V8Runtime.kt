package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.JavaCallback
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Function
import com.eclipsesource.v8.V8Object
import com.segment.analytics.substrata.kotlin.JSEngineError
import com.segment.analytics.substrata.kotlin.JSValue
import com.segment.analytics.substrata.kotlin.JavascriptDataBridge
import com.segment.analytics.substrata.kotlin.JavascriptEngine
import com.segment.analytics.substrata.kotlin.JavascriptErrorHandler
import com.segment.analytics.substrata.kotlin.jsValueToString
import com.segment.analytics.substrata.kotlin.wrapAsJSValue
import io.alicorn.v8.V8JavaAdapter
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

private const val TimeOutInSeconds = 60L

class J2V8Engine : JavascriptEngine {

    override lateinit var bridge: JavascriptDataBridge

    // All interaction with underlying runtime must be synchronized on the jsExecutor
    internal val jsExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    internal lateinit var underlying: V8

    var errorHandler: JavascriptErrorHandler? = null

    init {
        jsExecutor.await {
            underlying = V8.createV8Runtime()
            setupConsole()
            setupDataBridge()
        }
    }

    override fun loadBundle(completion: (Error) -> Unit) { // why closure not just error?
        val script = """
            function fooBar() {
                return "Bar";
            }
        """.trimIndent() // Get Script from local
        jsExecutor.sync {
            underlying.executeScript(script)
        }
    }

    override fun get(key: String): JSValue {
        val result = jsExecutor.await {
            var result: Any? = null
            underlying.memScope {
                underlying.get(key).let { value ->
                    if (!value.isNull()) {
                        result = value
                    } else {
                        underlying.executeScript(key).let { v ->
                            if (!v.isNull()) {
                                result = v
                            }
                        }
                    }
                }
            }
            result
        }
        return wrapAsJSValue(result)
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
                        underlying.add(key, jsRep)
                    }
                    is JSValue.JSArray -> {
                        val jsRep = value.content
                        underlying.add(key, jsRep)
                    }
                    is JSValue.JSFunction -> {
                        underlying.add(key, value.fn)
                    }
                    JSValue.JSUndefined -> {
                        // omit no point in setting a key with undefined value, right?
                        underlying.addUndefined(key)
                    }
                }
            }
        }
    }

    override fun <C : Any> expose(clazz: KClass<C>, className: String) {
        jsExecutor.sync {
            V8JavaAdapter.injectClass(className, clazz.java, underlying)
        }
    }

    override fun expose(function: JSValue.JSFunction, functionName: String) {
        jsExecutor.sync {
            underlying.add(functionName, function.fn)
        }
    }

    override fun extend(objectName: String, function: JSValue.JSFunction, functionName: String) {
        jsExecutor.sync {
            val v8Obj = V8Object(underlying) // maybe get if exists?
            v8Obj.add(functionName, function.fn)
            underlying.add(objectName, v8Obj)
        }
    }

    override fun call(function: String, params: List<JSValue>): JSValue {
        val result = jsExecutor.await {
            underlying.memScope {
                val parameters = V8Array(underlying).apply {
                    params.forEach { value ->
                        when (value) {
                            is JSValue.JSString -> push(value.content)
                            is JSValue.JSBool -> push(value.content)
                            is JSValue.JSInt -> push(value.content)
                            is JSValue.JSDouble -> push(value.content)
                            is JSValue.JSArray -> push(value.content)
                            is JSValue.JSObject -> push(value.content)
                            is JSValue.JSUndefined -> pushUndefined()
                        }
                    }
                }
                underlying.executeFunction(function, parameters)
            }
        }
        return wrapAsJSValue(result)
    }

    override fun execute(script: String): JSValue {
        val result = jsExecutor.await {
            underlying.executeScript(script)
        }
        return wrapAsJSValue(result)
    }

    private fun setupConsole() {
        // TODO add more versatility + Android log support
        val v8Console = V8Object(underlying)
        v8Console.registerJavaMethod({ _, v8Array ->
            val msg = jsValueToString(v8Array)
            println("[JSConsole.I] - $msg")
        }, "log")
        v8Console.registerJavaMethod({ _, v8Array ->
            val msg = jsValueToString(v8Array)
            println("[JSConsole.E] - $msg")
        }, "err")
        underlying.add("console", v8Console)
    }

    private fun setupDataBridge() {
        bridge = J2V8DataBridge(underlying, jsExecutor)
    }

    private fun reportError(error: JSEngineError) {
        errorHandler?.let { it(error) }
    }

}


fun J2V8Engine.expose(function: JavaCallback, functionName: String) {
    expose(JSValue.JSFunction(V8Function(underlying, function)), functionName)
}

fun J2V8Engine.extend(objectName: String, function: JavaCallback, functionName: String) {
    extend(objectName, JSValue.JSFunction(V8Function(underlying, function)), functionName)
}


class J2V8DataBridge(
    private val underlying: V8,
    private val jsExecutor: ExecutorService
) : JavascriptDataBridge {
    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    init {
        val dictionary = V8Object(underlying) // {}
        underlying.add(DataBridgeKey, dictionary)
    }

    override operator fun get(key: String): JSValue {
        val result = jsExecutor.await {
            underlying.executeScript("$DataBridgeKey.$key")
        }
        return wrapAsJSValue(result)
    }

    override operator fun set(key: String, value: JSValue) {
        jsExecutor.sync {
            val dataBridge = underlying.getObject(DataBridgeKey)
            when (value) {
                is JSValue.JSString -> dataBridge.add(key, value.content)
                is JSValue.JSBool -> dataBridge.add(key, value.content)
                is JSValue.JSInt -> dataBridge.add(key, value.content)
                is JSValue.JSDouble -> dataBridge.add(key, value.content)
                is JSValue.JSArray -> dataBridge.add(key, value.content)
                is JSValue.JSObject -> dataBridge.add(key, value.content)
                is JSValue.JSUndefined -> dataBridge.addUndefined(key)
            }
        }
    }
}

private fun ExecutorService.sync(runnable: Runnable) =
    submit(runnable).get(TimeOutInSeconds, TimeUnit.SECONDS)

private fun <T> ExecutorService.await(callable: Callable<T>) =
    submit(callable).get(TimeOutInSeconds, TimeUnit.SECONDS)

private fun Any?.isNull(): Boolean {
    return this == null
}
