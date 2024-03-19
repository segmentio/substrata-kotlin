package com.segment.analytics.substrata.kotlin

import com.segment.analytics.substrata.kotlin.JsonElementConverter.toJsonElement
import com.segment.analytics.substrata.kotlin.JsonElementConverter.wrap
import kotlinx.serialization.json.JsonElement

interface Releasable {
    fun release()
}

/**
 * Unlike other JSConvertible by default does not register to memory management.
 * To make it memory manageable see the implementation of JSValue.
 * */
interface JSConvertible: Releasable {
    val ref: Long
    val context: JSContext
}

/**
 * Unlike other JSConvertible, JSValue auto registers itself to memory management.
 * It is released once the scope is end
 * */
class JSValue(
    override val ref: Long,
    override val context: JSContext) : JSConvertible, Releasable {

    init {
        context.notifyReferenceCreated(this)
    }

    override fun release() {
        releasePointer()
        releaseReference()
    }

    /**
     * Release the pointer in QuickJS
     * */
    fun releasePointer() {
        context.release(ref)
    }

    /**
     * Release the reference hold by its observers
     * */
    fun releaseReference() {
        context.notifyReferenceReleased(this)
    }
}

/**
 * JSArray uses Delegation pattern instead of inheritance to
 * avoid registered to memory management multiple times for the
 * same JSValue.
 * */
class JSArray(jsValue: JSValue): JSConvertible by jsValue {

    var size: Int = 0
        internal set

    constructor(ref: Long, context: JSContext) : this(JSValue(ref, context))

    fun add(value: Boolean) = with(context) {
        setProperty(this@JSArray, size++, newBool(value))
    }

    fun add(value: Int) = with(context) {
        setProperty(this@JSArray, size++, newInt(value))
    }

    fun add(value: Double) = with(context) {
        setProperty(this@JSArray, size++, newDouble(value))
    }

    fun add(value: JSObject) = with(context) {
        setProperty(this@JSArray, size++, value)
    }

    fun add(value: JSArray) = with(context) {
        setProperty(this@JSArray, size++, value)
    }

    fun add(value: String) = with(context) {
        setProperty(this@JSArray, size++, newString(value))
    }

    fun add(value: JsonElement) = with(context) {
        setProperty(this@JSArray, size++, value.wrap(this))
    }

    fun <T: JSConvertible> add(value: T, converter: JSConverter<T>) = with(context) {
        setProperty(this@JSArray, size++, value.toJSValue(this))
    }

    fun getBoolean(index: Int): Boolean = context.getProperty(this, index)

    fun getInt(index: Int): Int = context.getProperty(this, index)

    fun getDouble(index: Int): Double = context.getProperty(this, index)

    fun getString(index: Int): String = context.getProperty(this, index)

    fun getJSObject(index: Int): JSObject = context.getProperty(this, index)

    fun getJSArray(index: Int): JSArray = context.getProperty(this, index)

    fun getJsonElement(index: Int): JsonElement {
        val property: Any = context.getProperty(this, index)
        return property.toJsonElement()
    }

    operator fun get(index: Int): Any = context.getProperty(this, index)

    fun <T> getJSConvertible(index: Int, converter: JSConverter<T>) : T {
        val jsArray =  context.getProperty<JSArray>(this, index)
        return converter.read(jsArray)
    }
}

/**
 * JSObject uses Delegation pattern instead of inheritance to
 * avoid registered to memory management multiple times for the
 * same JSValue.
 * */
class JSObject(
    jsValue: JSValue
): JSConvertible by jsValue, KeyValueObject {

    constructor(ref: Long, context: JSContext): this(JSValue(ref, context))

    override fun set(key: String, value: Int) = with(context) {
        setProperty(this@JSObject, key, newInt(value))
    }

    override fun set(key: String, value: Boolean) = with(context) {
        setProperty(this@JSObject, key, newBool(value))
    }

    override fun set(key: String, value: Double) = with(context) {
        setProperty(this@JSObject, key, newDouble(value))
    }

    override fun set(key: String, value: String) = with(context) {
        setProperty(this@JSObject, key, newString(value))
    }

    override fun set(key: String, value: JSObject) = with(context) {
        setProperty(this@JSObject, key, value)
    }

    override fun set(key: String, value: JSArray) = with(context){
        setProperty(this@JSObject, key, value)
    }

    override fun set(key: String, value: JsonElement) = with(context) {
        val v = value.wrap(this)
        setProperty(this@JSObject, key, v)
    }

    override fun <T : JSConvertible> set(key: String, value: T, converter: JSConverter<T>) {
        context.setProperty(this, key, value.toJSValue(context))
    }

    override fun getBoolean(key: String): Boolean = context.getProperty(this, key)

    override fun getInt(key: String): Int {
        val property = context.getProperty<JSValue>(this, key)
        return context.getInt(property)
    }

    override fun getDouble(key: String): Double = context.getProperty(this, key)

    override fun getString(key: String): String = context.getProperty(this, key)

    override fun getJSObject(key: String): JSObject = context.getProperty(this@JSObject, key)

    override fun getJSArray(key: String): JSArray = context.getProperty(this, key)

    override fun getJsonElement(key: String): JsonElement {
        val property: Any =  context.getProperty(this, key)
        return property.toJsonElement()
    }

    override fun get(key: String): Any = context.getProperty(this, key)

    override fun <T : JSConvertible> getJSConvertible(key: String, converter: JSConverter<T>): T {
        val jsObject =  context.getProperty<JSObject>(this, key)
        return converter.read(jsObject)
    }
}

class JSFunction(jsValue: JSValue) : JSConvertible by jsValue {
    constructor(ref: Long, context: JSContext): this(JSValue(ref, context))
    inline operator fun <reified T> invoke(obj: JSConvertible, vararg params: Any): T {
        val p = params.map { it.toJSValue(context).ref }.toLongArray()
        val ret = context.call(this@JSFunction.ref, obj.ref, p)
        return context.get(ret)
    }
}

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