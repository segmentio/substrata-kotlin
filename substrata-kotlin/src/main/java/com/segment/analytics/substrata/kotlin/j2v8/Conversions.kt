package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Function
import com.eclipsesource.v8.V8Object
import kotlinx.serialization.json.*


interface JSConverter<T> {
    /**
     * convert an object to target type.
     * the object can be any of the following types:
     *  * int
     *  * boolean
     *  * double
     *  * string
     *  * v8 value
     */
    fun read(obj: Any) : T

    /**
     * convert content to a V8 compatible object
     */
    fun write(content: T, engine: J2V8Engine) : Any
}

object JsonElementConverter : JSConverter<JsonElement> {
    override fun read(obj: Any): JsonElement = obj.toJsonElement()

    override fun write(content: JsonElement, engine: J2V8Engine): Any = content.toAny(engine.runtime)

    private fun JsonElement.toAny(runtime: V8) : Any {
        return when (this) {
            is JsonPrimitive -> toAny(runtime)
            is JsonObject -> toAny(runtime)
            is JsonArray -> toAny(runtime)
            else -> V8.getUndefined()
        }
    }

    private fun JsonPrimitive.toAny(runtime: V8) : Any {
        this.booleanOrNull?.let {
            return it
        }
        this.intOrNull?.let {
            return it
        }
        this.longOrNull?.let {
            return it
        }
        this.doubleOrNull?.let {
            return it
        }
        return V8.getUndefined()
    }

    private fun JsonArray.toAny(runtime: V8): Any{
        val result = V8Array(runtime)
        try {
            for (value in this) {
                result.push(value.toAny(runtime))
            }
        } catch (e: IllegalStateException) {
            result.close()
            throw e
        }

        return result
    }

    private fun JsonObject.toAny(runtime: V8): Any {
        val result = V8Object(runtime)
        try {
            for ((key, value) in this) {
                result.add(key, value.toAny(runtime))
            }
        } catch (e: IllegalStateException) {
            result.close()
            throw e
        }
        return result
    }

    private fun Any?.toJsonElement(): JsonElement{
        return when (this) {
            null -> JsonNull
            is Boolean -> JsonPrimitive(this)
            is Int -> JsonPrimitive(this)
            is Double -> JsonPrimitive(this)
            is String -> JsonPrimitive(this)
            is V8Function -> JsonNull
            is V8Array -> toJsonArray()
            is V8Object -> toJsonObject()
            else -> JsonNull
        }
    }

    private fun V8Array.toJsonArray() = buildJsonArray {
        val v8Array = this@toJsonArray
        val jsonArray = this

        for (i in 0 until length()) {
            val value = v8Array[i].toJsonElement()
            if (value != JsonNull) {
                jsonArray.add(value)
            }
        }
    }

    private fun V8Object.toJsonObject() = buildJsonObject {
        val v8Object = this@toJsonObject
        val jsonObject = this

        for (key in v8Object.keys) {
            jsonObject.put(key, v8Object[key].toJsonElement())
        }
    }
}
