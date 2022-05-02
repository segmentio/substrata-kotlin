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

    /*
        fun foo() {
            val x = JSValue.JSFunction { params -> params[0] }
            x(JSValue.JSUndefined)
        }
     */
//    class JSFunction(val fn: (params: JSArray) -> JSValue) : JSValue {
//
//        constructor(v8Function: V8Function) : this({ params ->
//            v8Function.call(null, params.content) as JSValue
//        })
//
//        // Non-null return type bcos JSValue.JSUndefined represents null
//        operator fun invoke(params: JSArray): JSValue {
//            return fn(params)
//        }
//    }

    class JSFunction(val fn: V8Function): JSValue

    object JSUndefined : JSValue
}