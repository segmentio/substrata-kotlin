package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.*

inline fun <reified T> JSConvertible.unwrap() : T = with(context) {
    val result = when(T::class) {
        String::class -> getString(ref)
        Boolean::class -> getBool(ref)
        Int::class -> getInt(ref)
        Double::class -> getDouble(ref)
        JSArray::class ->
            if (this@unwrap is JSValue) {
                JSArray(this@unwrap)
            }
            else {
                JSArray(ref, context)
            }
        JSObject::class ->
            if (this@unwrap is JSValue) {
                JSObject(this@unwrap)
            }
            else {
                JSObject(ref, context)
            }
        else -> JSNull
    }
    return result as T
}

internal inline fun <reified T> JSConvertible.isTypeOf() = when(T::class) {
    String::class -> context.isString(ref)
    Boolean::class -> context.isBool(ref)
    Int::class -> context.isNumber(ref)
    Double::class -> context.isNumber(ref)
    JSArray::class -> context.isArray(ref)
    JSObject::class -> context.isObject(ref)
    else -> false
}

internal inline fun <reified T> JSConvertible.cast() =
    if (isTypeOf<T>()) unwrap<T>()
    else null

fun JSConvertible.asString(): String? = cast()

fun JSConvertible.asBoolean(): Boolean? = cast()

fun JSConvertible.asInt(): Int?  = cast()

fun JSConvertible.asDouble(): Double?  = cast()

fun JSConvertible.asJSArray(): JSArray? =  cast()

fun JSConvertible.asJSObject(): JSObject? = cast()

fun String.toJSValue(context: JSContext) = context.newJSValue(this)

fun Boolean.toJSValue(context: JSContext) = context.newJSValue(this)

fun Int.toJSValue(context: JSContext) = context.newJSValue(this)

fun Double.toJSValue(context: JSContext) = context.newJSValue(this)

fun JSNull.toJSValue(context: JSContext): JSConvertible = context.newJSValue(this)

fun JSUndefined.toJSValue(context: JSContext): JSConvertible = context.newJSValue(this)

fun Any.toJSValue(context: JSContext): JSConvertible = when(this) {
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
    fun write(content: T, context: JSContext) : Any
}

object JsonElementConverter : JSConverter<JsonElement> {
    override fun read(obj: Any?): JsonElement = obj.toJsonElement()

    override fun write(content: JsonElement, context: JSContext): Any = content.unwrap(context)

    internal fun JsonElement.unwrap(context: JSContext) : Any {
        return when (this) {
            is JsonPrimitive -> unwrap()
            is JsonObject -> unwrap(context)
            is JsonArray -> unwrap(context)
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

    private fun JsonArray.unwrap(context: JSContext): JSArray {
        val jsArray = context.newArray()
        for (index in this.indices) {
            jsArray.add(this[index])
        }
        return jsArray
    }

    private fun JsonObject.unwrap(context: JSContext): JSObject {
        val jsObject = context.newObject()
        for ((key, value) in this) {
            jsObject[key] = value
        }
        return jsObject
    }

    fun Any?.toJsonElement(): JsonElement{
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

        for (i in 0..jsArray.size) {
            val value = jsArray.getJsonElement(i)
            if (value != JsonNull) {
                jsonArray.add(value)
            }
        }
    }

    private fun JSObject.toJsonObject() = buildJsonObject {
        val jsObject = this@toJsonObject
        val jsonObject = this

        for ((key, value) in jsObject.context.getProperties(jsObject)) {
            jsonObject.put(key, value.toJsonElement())
        }
    }
}
