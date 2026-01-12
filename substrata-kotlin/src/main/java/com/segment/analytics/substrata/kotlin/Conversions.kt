package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.*

inline fun <reified T> JSConvertible.wrap() : T = with(context) {
    val result = when(T::class) {
        String::class -> getString(ref)
        Boolean::class -> getBool(ref)
        Int::class -> getInt(ref)
        Double::class -> getDouble(ref)
        Long::class -> getLong(ref)
        JSArray::class -> getJSArray(this@wrap)
        JSObject::class -> getJSObject(this@wrap)
        JSFunction::class -> getJSFunction(this@wrap)
        JSException::class -> getJSException()
        else -> context.JSNull
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
    JSFunction::class -> context.isFunction(ref)
    JSException::class -> true
    else -> false
}

internal inline fun <reified T> JSConvertible.cast() =
    if (isTypeOf<T>()) wrap<T>()
    else throw Exception("Failed to cast JSConvertible. Unknown type is passed")

fun JSConvertible.asString(): String = cast()

fun JSConvertible.asBoolean(): Boolean = cast()

fun JSConvertible.asInt(): Int  = cast()

fun JSConvertible.asDouble(): Double  = cast()

fun JSConvertible.asJSArray(): JSArray=  cast()

fun JSConvertible.asJSObject(): JSObject = cast()

fun JSConvertible.asJSFunction(): JSFunction = cast()

fun JSConvertible.asJSException(): JSException = wrap()

fun String.toJSValue(context: JSContext) = context.newJSValue(this)

fun Boolean.toJSValue(context: JSContext) = context.newJSValue(this)

fun Int.toJSValue(context: JSContext) = context.newJSValue(this)

fun Double.toJSValue(context: JSContext) = context.newJSValue(this)

fun Any?.toJSValue(context: JSContext): JSConvertible = when(this) {
    is JSException -> throw Exception(this.getException())
    is JSConvertible -> this
    is String -> this.toJSValue(context)
    is Boolean -> this.toJSValue(context)
    is Int -> this.toJSValue(context)
    is Double -> this.toJSValue(context)
    null -> context.JSNull
    else -> throw Exception("Type ${this.javaClass.name} cannot be cast to JSValue.")
}


interface JSConverter<T> {
    /**
     * NOTE: converter has to be used in a JSSCope
     *
     * convert an object to target type.
     * the object can be any of the following types:
     *  * int
     *  * boolean
     *  * double
     *  * string
     *  * JSConvertibles
     */
    fun read(obj: Any?) : T

    /**
     * NOTE: converter has to be used in a JSSCope
     *
     * convert content to a JS compatible object. the object could be:
     *  * int
     *  * boolean
     *  * double
     *  * string
     *  * JSConvertibles
     */
    fun write(content: T, context: JSContext) : Any
}

object JsonElementConverter : JSConverter<JsonElement> {
    override fun read(obj: Any?): JsonElement = obj.toJsonElement()

    override fun write(content: JsonElement, context: JSContext): Any = content.wrap(context)

    internal fun JsonElement.wrap(context: JSContext) : JSConvertible {
        return when (this) {
            is JsonPrimitive -> wrap(context)
            is JsonObject -> wrap(context)
            is JsonArray -> wrap(context)
            else -> context.JSNull
        }
    }

    private fun JsonPrimitive.wrap(context: JSContext) : JSConvertible {
        this.booleanOrNull?.let {
            return context.newBool(it)
        }
        this.intOrNull?.let {
            return context.newInt(it)
        }
        this.longOrNull?.let {
            return context.newLong(it)
        }
        this.doubleOrNull?.let {
            return context.newDouble(it)
        }
        if (this.isString) {
            return context.newString(content)
        }
        return context.JSNull
    }

    private fun JsonArray.wrap(context: JSContext): JSArray {
        val jsArray = context.newArray()
        for (index in this.indices) {
            jsArray.add(this[index])
        }
        return jsArray
    }

    private fun JsonObject.wrap(context: JSContext): JSObject {
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
            is Long -> JsonPrimitive(this)
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
