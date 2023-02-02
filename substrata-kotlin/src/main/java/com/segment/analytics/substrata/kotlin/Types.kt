package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
import com.segment.analytics.substrata.kotlin.j2v8.add

/**
 * We cant add extensions to existing types like in swift, so its up to the user to provide
 * serializer/deserializer for custom types. We will provide all primitive ones.
 */
interface JSValue {

    fun toAny(runtime: V8) : Any?

    object JSUndefined : JSValue {
        override fun toAny(runtime: V8): Any = V8.getUndefined()
    }

    object JSNull : JSValue {
        override fun toAny(runtime: V8): Any? = null
    }

    companion object {
        fun from(obj: Any?) : JSValue{
            return when (obj) {
                null -> return JSNull
                is Boolean -> JSBool(obj)
                is Int -> JSInt(obj)
                is Double -> JSDouble(obj)
                is String -> JSString(obj)
                is V8Function -> JSFunction.from(obj)
                is V8Array -> JSArray.from(obj)
                is V8Object -> JSObject.from(obj)
                else -> JSUndefined
            }
        }
    }
}

@JvmInline
value class JSString(val content: String) : JSValue {
    override fun toAny(runtime: V8) = content
}

@JvmInline
value class JSBool(val content: Boolean) : JSValue {
    override fun toAny(runtime: V8) = content
}

@JvmInline
value class JSInt(val content: Int) : JSValue {
    override fun toAny(runtime: V8) = content
}

@JvmInline
value class JSDouble(val content: Double) : JSValue {
    override fun toAny(runtime: V8) = content
}

@JvmInline
value class JSArray(val content: List<JSValue>) : JSValue {
    override fun toAny(runtime: V8) : Any {
        val result = V8Array(runtime)
        try {
            for (value in content) {
                result.push(value.toAny(runtime))
            }
        } catch (e: IllegalStateException) {
            result.close()
            throw e
        }

        return result
    }

    companion object {
        internal fun from(v8Array: V8Array) : JSValue {
            val arr = mutableListOf<JSValue>()
            for (i in 0 until v8Array.length()) {
                arr.add(JSValue.from(v8Array[i]))
            }

            return JSArray(arr)
        }
    }
}

@JvmInline
value class JSObject(val content: Map<String, JSValue>) : JSValue {
    override fun toAny(runtime: V8) : Any {
        val result = V8Object(runtime)
        try {
            for ((key, value) in content) {
                result.add(key, value.toAny(runtime))
            }
        } catch (e: IllegalStateException) {
            result.close()
            throw e
        }
        return result
    }

    companion object {
        internal fun from(v8Object: V8Object) : JSValue {
            val map = mutableMapOf<String, JSValue>()
            for (key in v8Object.keys) {
                map[key] = JSValue.from(v8Object[key])
            }

            return JSObject(map)
        }
    }
}

class JSFunction(val function : JSFunctionDefinition) : JSValue {
    internal val callBack = JavaCallback { p0, p1 ->
        val obj = if (p0 != null) JSObject.from(p0) as JSObject else null
        val params = if (p1 != null) JSArray.from(p1) as JSArray else null
        function(obj, params)
    }

    override fun toAny(runtime: V8): Any {
        return V8Function(runtime, callBack)
    }

    companion object {
        internal fun from(v8Function: V8Function) = JSValue.JSNull
    }
}

typealias JSFunctionDefinition = (JSObject?, JSArray?) -> JSValue

class JSObjectRef(obj: Any)  {
    val ref : Any

    val data : JSObject

    init {
        require(obj is V8Object)

        ref = obj
        data = JSObject.from(ref) as JSObject
    }
}

open class JSExport : JSValue {
    override fun toAny(runtime: V8): Any? {
        TODO("Not yet implemented")
    }
}