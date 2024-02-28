package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.JsonElement

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

class JSArray(
    val content: MutableList<Any> = mutableListOf()
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
    val content: MutableMap<String, Any> = mutableMapOf()
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

    fun getJsonElement(key: String) = JsonElementConverter.read(content[key])

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

object JSNull

open class JSExport