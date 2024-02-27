package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlin.Exception

interface Releasable {
    fun release()
}

interface JSConvertible

open class JSValue(
    val ref: Long,
    val context: JSContext
) : Releasable {

    init {
        context.notifyReferenceCreated(this)
    }

    override fun release() {
        context.notifyReferenceReleased(this)
    }
}

fun JSValue.asString() =
    if (QuickJS.isString(this.ref)) QuickJS.getString(this.ref)
    else null


fun JSValue.asBoolean() =
    if (QuickJS.isBool(this.ref)) QuickJS.getBool(this.ref)
    else null

fun JSValue.asInt() =
    if (QuickJS.isNumber(this.ref)) QuickJS.getInt(this.ref)
    else null

fun JSValue.asDouble() =
    if (QuickJS.isNumber(this.ref)) QuickJS.getFloat64(this.ref)
    else null

fun JSValue.asJSArray(): JSArray? {
    if (!QuickJS.isArray(this.ref)) return null

    val sizeRef = QuickJS.getProperty(context.ref, this.ref, "length")
    val size: Int = get(context, sizeRef)
    val result = MutableList(size) { i ->
        val valueRef = QuickJS.getProperty(context.ref, this.ref, i)
        val value = getAny(context, valueRef)
        value
    }

    return JSArray(result)
}

fun JSValue.asJSObject(): JSObject? {
    if (!QuickJS.isObject(this.ref)) return null

    val names = QuickJS.getOwnPropertyNames(context.ref, ref)
    val result = mutableMapOf<String, Any?>()
    for (name in names) {
        val valueRef = QuickJS.getProperty(context.ref, this.ref, name)
        val value = getAny(context, valueRef)
        result[name] = value
    }

    return JSObject(result)
}

inline fun <reified T> get(context: JSContext, ref: Long): T {
    val value = JSValue(ref, context)
    val result = when(T::class) {
        String::class -> value.asString()
        Boolean::class -> value.asBoolean()
        Int::class -> value.asInt()
        Double::class -> value.asDouble()
        else -> null
    }
    return result as T
}

fun getAny(context: JSContext, ref: Long): Any? {
    val type = QuickJS.getType(ref)
    val value = JSValue(ref, context)
    return when (type) {
        QuickJS.TYPE_STRING -> value.asString()
        QuickJS.TYPE_BOOLEAN -> value.asBoolean()
        QuickJS.TYPE_INT -> value.asInt()
        QuickJS.TYPE_FLOAT64 -> value.asDouble()
        else -> {}
    }
}

fun String.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newString(context.ref, this)
    return JSValue(ref, context)
}

fun Boolean.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newBool(context.ref, this)
    return JSValue(ref, context)
}

fun Int.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newInt(context.ref, this)
    return JSValue(ref, context)
}

fun Double.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newFloat64(context.ref, this)
    return JSValue(ref, context)
}

fun JSArray.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newArray(context.ref)
    for ((index, value) in content.withIndex()) {
        QuickJS.setProperty(context.ref, ref, index, value.toJSValue(context).ref)
    }
    return JSValue(ref, context)
}

fun JSObject.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newObject(context.ref)
    for ((key, value) in content) {
        QuickJS.setProperty(context.ref, ref, key, value.toJSValue(context).ref)
    }
    return JSValue(ref, context)
}

fun Any.toJSValue(context: JSContext): JSValue = when(this) {
    is String -> this.toJSValue(context)
    is Boolean -> this.toJSValue(context)
    is Int -> this.toJSValue(context)
    is Double -> this.toJSValue(context)
    is Array<*> -> this.toJSValue(context)
    else -> throw Exception("/** TODO: */")
}

class JSArray(
    val content: MutableList<Any?> = mutableListOf()
) {

    fun add(value: Boolean) {
        content.add(value)
    }

    fun add(value: Int) {
        content.add(value)
    }

    fun add(value: Double) {
        content.add(value)
    }

    fun add(value: String) {
        content.add(value)
    }

    fun add(value: JsonElement) {
        content.add(JsonElementConverter.write(value))
    }

    fun <T: JSConvertible> add(value: T, converter: JSConverter<T>) {
        val converted = converter.write(value)
        content.add(converted)
    }

    fun getBoolean(index: Int) = content[index] as? Boolean

    fun getInt(index: Int) = content[index] as? Int

    fun getDouble(index: Int) = content[index] as? Double

    fun getString(index: Int) = content[index] as? String

    fun getJsonElement(index: Int) = JsonElementConverter.read(content[index])

    operator fun get(index: Int): Any = content[index]

    fun <T> getJSConvertible(index: Int, converter: JSConverter<T>) : T = converter.read(content[index])
}

class JSObject(
    val content: MutableMap<String, Any?> = mutableMapOf()
) {

    fun add(key: String, value: Int) {
        content[key] = value
    }

    fun add(key: String, value: Boolean) {
        content[key] = value
    }

    fun add(key: String, value: Double) {
        content[key] = value
    }

    fun add(key: String, value: String) {
        content[key] = value
    }

    fun add(key: String, value: JsonElement) {
        content[key] = JsonElementConverter.write(value)
    }

    fun <T: JSConvertible> add(key: String, value: T, converter: JSConverter<T>) {
        content[key] = converter.write(value)
    }

    fun getBoolean(key: String) = content[key] as? Boolean

    fun getInt(key: String) = content[key] as? Int

    fun getDouble(key: String) = content[key] as? Double

    fun getString(key: String) = content[key] as? String

    fun getJsonElement(key: String) = content[key]?.let { JsonElementConverter.read(it) }

    operator fun get(key: String) = content[key]

    fun <T> getJSConvertible(key: String, converter: JSConverter<T>) = content[key]?.let { converter.read(it) }
}

class JSFunction(ref: Long, context: JSContext) : JSValue(ref, context) {
    operator fun invoke(obj: JSValue, vararg params: JSValue): Any? {
        // TODO: check if the same context
        val refs = params.map { it.ref }.toTypedArray()
        val ret = QuickJS.call(context.ref, ref, obj.ref, refs)
        return getAny(context, ret)
    }
}


open class JSResult internal constructor(content: Any?) {
    private var released = false

    val content: Any

    init {
        this.content = content ?: V8.getUndefined()
    }

    fun <T> read(converter: JSConverter<T>) : T {
        if (released) {
            throw Exception("JSResult has been released. Make sure you only read the value once.")
        }

        val result = converter.read(content)

        if (content is Releasable) {
            content.close()
        }
        released = true

        return result
    }
}

open class JSExport