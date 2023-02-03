package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
import com.segment.analytics.substrata.kotlin.j2v8.J2V8Engine
import com.segment.analytics.substrata.kotlin.j2v8.add
import com.segment.analytics.substrata.kotlin.j2v8.toAny
import com.segment.analytics.substrata.kotlin.j2v8.toJsonElement
import kotlinx.serialization.json.JsonElement
import java.lang.Exception

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
                null -> JSNull
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

@JvmInline
value class JSElement(val content: JsonElement): JSValue {
    override fun toAny(runtime: V8): Any? {
        TODO("Not yet implemented")
    }

}

open class JSParameters(private val engine: J2V8Engine) {
    internal val content = V8Array(engine.runtime)

    fun add(value: Boolean) = content.push(value)

    fun add(value: Int) = content.push(value)

    fun add(value: Double) = content.push(value)

    fun add(value: String) = content.push(value)

    fun add(value: JsonElement) = content.push(value.toAny(engine.runtime))

    fun add(value: JSValue) = content.push(value.toAny(engine.runtime))

    fun addAsJSValue(value: Any, converter: (Any) -> JSValue) {
        add(converter(value))
    }

    fun addAsJsonElement(value: Any, converter: (Any) -> JsonElement) {
        add(converter(value))
    }

    fun release() {
        content.close()
    }
}

class JSResult internal constructor(private val content: Any) {
    private var released = false

    fun <T> read(reader: Reader<T>) : T {
        if (released) {
            throw Exception("JSResult has been released. Make sure you only read the value once.")
        }

        val result = reader.convert(content)

        if (content is Releasable) {
            content.close()
        }
        released = true

        return result
    }

    interface Reader<T> {
        fun convert(obj: Any) : T
    }

    class JSValueReader : Reader<JSValue> {
        override fun convert(obj: Any) = JSValue.from(obj)
    }

    class JsonElementReader : Reader<JsonElement> {
        override fun convert(obj: Any) = obj.toJsonElement()
    }
}

class JSObjectRef(obj: Any)  {
    val ref : Any

    val data : JSObject

    init {
        require(obj is V8Object)

        ref = obj
        data = JSObject.from(ref) as JSObject
    }
}

open class JSExport