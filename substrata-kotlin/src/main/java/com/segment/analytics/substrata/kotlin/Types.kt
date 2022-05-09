package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Function
import com.eclipsesource.v8.V8Object
import com.segment.analytics.substrata.kotlin.j2v8.fromV8Array
import com.segment.analytics.substrata.kotlin.j2v8.fromV8Object
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * We cant add extensions to existing types like in swift, so its up to the user to provide
 * serializer/deserializer for custom types. We will provide all primitive ones.
 */
interface JSValue {

    @JvmInline
    value class JSString(val content: String) : JSValue

    @JvmInline
    value class JSBool(val content: Boolean) : JSValue

    @JvmInline
    value class JSInt(val content: Int) : JSValue

    @JvmInline
    value class JSDouble(val content: Double) : JSValue

    class JSObject(val content: V8Object) : JSValue {
        val mapRepresentation: JsonObject? by lazy { fromV8Object(content) }
    }

    class JSArray(val content: V8Array) : JSValue {
        val listRepresentation: JsonArray? by lazy { fromV8Array(content) }
    }

    class JSFunction(val fn: V8Function) : JSValue

    object JSUndefined : JSValue // might not need this, might just be a compile time error
    object JSNull : JSValue
}

fun jsValueToString(value: Any?): String {
    return when (value) {
        is Boolean -> value.toString()
        is Int -> value.toString()
        is Double -> value.toString()
        is String -> value.toString()
        is V8Array -> buildString {
            for (i in 0 until value.length() - 1) {
                append(jsValueToString(value.get(i)))
                append(", ")
            }
        }
        is V8Object -> buildString {
            for (key in value.keys) {
                append(jsValueToString(value.get(key)))
                append(", ")
            }
        }
        null -> "null"
        else -> "undefined"
    }
}

fun wrapAsJSValue(obj: Any?): JSValue {
    return when (obj) {
        null -> return JSValue.JSNull
        is Boolean -> JSValue.JSBool(obj)
        is Int -> JSValue.JSInt(obj)
        is Double -> JSValue.JSDouble(obj)
        is String -> JSValue.JSString(obj)
        is V8Array -> JSValue.JSArray(obj)
        is V8Object -> JSValue.JSObject(obj)
        else -> JSValue.JSUndefined
    }
}
