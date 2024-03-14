package com.segment.analytics.substrata.kotlin

import com.segment.analytics.substrata.kotlin.JsonElementConverter.toJsonElement
import com.segment.analytics.substrata.kotlin.JsonElementConverter.unwrap
import kotlinx.serialization.json.JsonElement

interface Releasable {
    fun release()
}

interface JSConvertible {
    val context: JSContext
}

open class JSValue(
    val ref: Long,
    val context: JSContext
) : Releasable {

    init {
        context.notifyReferenceCreated(this)
    }

    override fun release() {
        context.notifyReferenceReleased(this)
        context.release(ref)
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
//        content.add(JsonElementConverter.write(value))
    }

    fun <T: JSConvertible> add(value: T, converter: JSConverter<T>) {
//        val converted = converter.write(value)
//        content.add(converted)
    }

    fun getBoolean(index: Int) = content[index] as? Boolean

    fun getInt(index: Int) = content[index] as? Int

    fun getDouble(index: Int) = content[index] as? Double

    fun getString(index: Int) = content[index] as? String

    fun getJsonElement(index: Int) = JsonElementConverter.read(content[index])

    operator fun get(index: Int): Any = content[index]

    fun <T> getJSConvertible(index: Int, converter: JSConverter<T>) : T = converter.read(content[index])
}

class JSObject private constructor(
    context: JSContext,
    ref: Long
): JSValue(ref, context), KeyValueObject {
    constructor(jsValue: JSValue) : this(
        jsValue.context,
        jsValue.ref)

    constructor(context: JSContext) : this(
        context.newJSValue<JSObject>(null))

    override fun set(key: String, value: Int) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun set(key: String, value: Boolean) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun set(key: String, value: Double) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun set(key: String, value: String) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun set(key: String, value: JSObject) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun set(key: String, value: JSArray) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun set(key: String, value: JsonElement) {
        context.setProperty(this, key, value.unwrap(context).toJSValue(context))
    }

    override fun <T : JSConvertible> set(key: String, value: T, converter: JSConverter<T>) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun getBoolean(key: String): Boolean = context.getProperty(this, key)

    override fun getInt(key: String): Int = context.getProperty(this, key)

    override fun getDouble(key: String): Double = context.getProperty(this, key)

    override fun getString(key: String): String = context.getProperty(this, key)

    override fun getJSObject(key: String): JSObject = context.getProperty(this, key)

    override fun getJSArray(key: String): JSArray = context.getProperty(this, key)

    override fun getJsonElement(key: String): JsonElement {
        val jsObject =  context.getProperty<JSObject>(this, key)
        return jsObject.toJsonElement()
    }

    override fun get(key: String): Any? = context.getProperty(this, key)

    override fun <T : JSConvertible> getJSConvertible(key: String, converter: JSConverter<T>): T {
        val jsObject =  context.getProperty<JSObject>(this, key)
        return converter.read(jsObject)
    }
}

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
    operator fun set(key: String, value: Int)

    operator fun set(key: String, value: Boolean)

    operator fun set(key: String, value: Double)

    operator fun set(key: String, value: String)

    operator fun set(key: String, value: JSObject)

    operator fun set(key: String, value: JSArray)

    operator fun set(key: String, value: JsonElement)

    fun <T: JSConvertible> set(key: String, value: T, converter: JSConverter<T>)

    fun getBoolean(key: String): Boolean

    fun getInt(key: String): Int

    fun getDouble(key: String): Double

    fun getString(key: String): String

    fun getJSObject(key: String): JSObject

    fun getJSArray(key: String): JSArray

    fun getJsonElement(key: String): JsonElement

    operator fun get(key: String): Any?

    fun <T: JSConvertible> getJSConvertible(key: String, converter: JSConverter<T>): T
}