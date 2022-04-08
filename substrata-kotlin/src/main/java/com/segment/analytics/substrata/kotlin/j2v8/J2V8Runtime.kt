package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.substrata.kotlin.JSExtension
import com.segment.analytics.substrata.kotlin.JSRuntime
import com.segment.analytics.substrata.kotlin.JSValue
import io.alicorn.v8.V8JavaAdapter
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class J2V8Runtime(
    override val underlying: V8 = V8.createV8Runtime(),
) : JSRuntime<V8> {

    // Need to add single-thread for synchronizing all ops

    val extensions = mutableSetOf<JSExtension<V8>>()

    override fun configureRuntime() {
        for (extension in extensions) {
            extension.configureExtension()
        }
        TODO("Not yet implemented")
    }

    override fun get(key: String): JSValue {
        val keys = underlying.keys
        if (keys.isEmpty()) {
            return JSValue.JSUndefined
        }
        var result = underlying.getObject(key)
        if (result.isUndefined) {
            result = underlying.executeObjectScript(key) // Blows up when the key does not exist
        }
        return result as JSValue
    }

    override fun set(key: String, value: JSValue) {
        underlying.memScope {
            TODO("Not yet implemented")
        }
    }

    override fun call(functionName: String, params: List<JSValue>): JSValue {
        underlying.memScope {
            TODO("Not yet implemented")
        }
    }

    override fun evaluate(script: String): JSValue {
        underlying.memScope {
            TODO("Not yet implemented")
        }
    }

    override fun expose(extension: JSExtension<V8>) {
        extensions += extension
    }
/*
    private fun Any.toJSValue(): JSValue {
        when (this) {
            Boolean -> JSValue.JSBool(this as Boolean)
            Int -> JSValue.JSInt(this as Int)
            Double -> JSValue.JSFloat(this as Float)
            String -> JSValue.JSString(this as String)
            V8Array -> JSValue.JSArray()
            V8Object -> JSValue.JSObject(value as V8Object?)
            V8Value.UNDEFINED, V8Value.NULL, V8Value.V8_TYPED_ARRAY, V8Value.V8_FUNCTION, V8Value.V8_ARRAY_BUFFER -> JSValue.JSUndefined
            else -> JSValue.JSUndefined
        }
    }
 */
}

internal class ConsoleExtension(override val runtime: J2V8Runtime) : JSExtension<J2V8Runtime> {
    fun log(message: String) {
        println("[JS-Console:INFO] $message")
    }

    fun error(message: String) {
        println("[JS-Console:ERROR] $message")
    }

    override val name: String = "J2V8Console"

    override fun configureExtension() {
        val underlyingRuntime = runtime.underlying
        val v8Console = V8Object(underlyingRuntime)
        v8Console.registerJavaMethod(
            this,
            "log",
            "log",
            arrayOf<Class<*>>(String::class.java)
        )
        v8Console.registerJavaMethod(
            this,
            "error",
            "err",
            arrayOf<Class<*>>(String::class.java)
        )
        underlyingRuntime.add("console", v8Console)
    }
}

internal class AnalyticsExtension(override val runtime: J2V8Runtime) : JSExtension<J2V8Runtime> {
    override val name: String = "AnalyticsExtension"

    override fun configureExtension() {
        val underlyingRuntime = runtime.underlying
        V8JavaAdapter.injectClass(Analytics::class.java, underlyingRuntime)
    }
}