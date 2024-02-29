package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.*

internal inline fun <reified T> JSValue.cast() =
    if (context.isTypeOf<T>(this)) context.unwrap<T>(this)
    else null

fun JSValue.asString(): String? = cast()

fun JSValue.asBoolean(): Boolean? = cast()

fun JSValue.asInt(): Int?  = cast()

fun JSValue.asDouble(): Double?  = cast()

fun JSValue.asJSArray(): JSArray? =  cast()

fun JSValue.asJSObject(): JSObject? = cast()

fun String.toJSValue(context: JSContext) = context.newJSValue(this)

fun Boolean.toJSValue(context: JSContext) = context.newJSValue(this)

fun Int.toJSValue(context: JSContext) = context.newJSValue(this)

fun Double.toJSValue(context: JSContext) = context.newJSValue(this)

fun JSArray.toJSValue(context: JSContext): JSValue {
    val array = context.newJSValue(this)
    for ((index, value) in content.withIndex()) {
        context.setProperty(array, index, value.toJSValue(context))
    }
    return array
}

fun JSObject.toJSValue(context: JSContext): JSValue {
    val obj = context.newJSValue(this)
    for ((key, value) in content) {
        context.setProperty(obj, key, value.toJSValue(context))
    }
    return obj
}

fun JSNull.toJSValue(context: JSContext): JSValue = context.newJSValue(this)

fun JSUndefined.toJSValue(context: JSContext): JSValue = context.newJSValue(this)

fun Any.toJSValue(context: JSContext): JSValue = when(this) {
    is String -> this.toJSValue(context)
    is Boolean -> this.toJSValue(context)
    is Int -> this.toJSValue(context)
    is Double -> this.toJSValue(context)
    is JSArray -> this.toJSValue(context)
    is JSObject -> this.toJSValue(context)
    else -> throw Exception("/** TODO: */")
}


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
    fun read(obj: Any?) : T

    /**
     * convert content to a V8 compatible object
     */
    fun write(content: T) : Any
}

object JsonElementConverter : JSConverter<JsonElement> {
    override fun read(obj: Any?): JsonElement = obj.toJsonElement()

    override fun write(content: JsonElement): Any = content.unwrap()

    private fun JsonElement.unwrap() : Any {
        return when (this) {
            is JsonPrimitive -> unwrap()
            is JsonObject -> unwrap()
            is JsonArray -> unwrap()
            else -> JSNull
        }
    }

    private fun JsonPrimitive.unwrap() : Any {
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
        return JSNull
    }

    private fun JsonArray.unwrap() = JSArray(this.toMutableList())

    private fun JsonObject.unwrap() = JSObject(this.toMutableMap())

    private fun Any?.toJsonElement(): JsonElement{
        return when (this) {
            null -> JsonNull
            is Boolean -> JsonPrimitive(this)
            is Int -> JsonPrimitive(this)
            is Double -> JsonPrimitive(this)
            is String -> JsonPrimitive(this)
            is JSArray -> toJsonArray()
            is JSObject -> toJsonObject()
            else -> JsonNull
        }
    }

    private fun JSArray.toJsonArray() = buildJsonArray {
        val jsArray = this@toJsonArray
        val jsonArray = this

        for (i in 0 until jsArray.content.size) {
            val value = jsArray.getJsonElement(i)
            if (value != JsonNull) {
                jsonArray.add(value)
            }
        }
    }

    private fun JSObject.toJsonObject() = buildJsonObject {
        val jsObject = this@toJsonObject
        val jsonObject = this

        for (key in jsObject.content.keys) {
            jsonObject.put(key, jsObject.getJsonElement(key))
        }
    }
}
