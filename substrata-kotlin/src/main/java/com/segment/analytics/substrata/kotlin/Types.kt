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
    override val content: MutableMap<String, Any> = mutableMapOf()
): KeyValueObject

class JSFunction(ref: Long, context: JSContext) : JSValue(ref, context) {
    operator fun invoke(obj: JSValue, vararg params: JSValue): Any? {
        // TODO: check if the same context
        val refs = params.map { it.ref }.toLongArray()
        val ret = QuickJS.call(context.ref, ref, obj.ref, refs)
        return context.getAny(ret)
    }
}

object JSNull

object JSUndefined

open class JSExport

interface KeyValueObject {
    val content: MutableMap<String, Any>

    operator fun set(key: String, value: Int) {
        content[key] = value
    }

    operator fun set(key: String, value: Boolean) {
        content[key] = value
    }

    operator fun set(key: String, value: Double) {
        content[key] = value
    }

    operator fun set(key: String, value: String) {
        content[key] = value
    }

    operator fun set(key: String, value: JSObject) {
        content[key] = value
    }

    operator fun set(key: String, value: JSArray) {
        content[key] = value
    }

    operator fun set(key: String, value: JsonElement) {
        content[key] = JsonElementConverter.write(value)
    }

    fun <T: JSConvertible> set(key: String, value: T, converter: JSConverter<T>) {
        content[key] = converter.write(value)
    }

    fun getBoolean(key: String, default: Boolean = false) = content[key] as? Boolean ?: default

    fun getInt(key: String, default: Int = 0) = content[key] as? Int ?: default

    fun getDouble(key: String, default: Double = .0) = content[key] as? Double ?: default

    fun getString(key: String) = content[key] as? String

    fun getJSObject(key: String)  = content[key] as? JSObject

    fun getJSArray(key: String) = content[key] as? JSArray

    fun getJsonElement(key: String) = JsonElementConverter.read(content[key])

    operator fun get(key: String) = content[key]

    fun <T: JSConvertible> getJSConvertible(key: String, converter: JSConverter<T>) = content[key]?.let { converter.read(it) }
}