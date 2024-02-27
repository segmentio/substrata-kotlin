package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
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

inline fun <reified T> JSValue.asArray(): Array<T>? {
    if (!QuickJS.isArray(this.ref)) return null

    val sizeRef = QuickJS.getProperty(context.ref, this.ref, "length")
    val size: Int = get(context, sizeRef)
    val result = Array(size) { i ->
        val valueRef = QuickJS.getProperty(context.ref, this.ref, i)
        val value: T = get(context, valueRef)
        value
    }

    return result
}

inline fun <reified T> JSValue.asMap(): Map<String, T> {

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

fun <T: Any> Array<T>.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newArray(context.ref)
    for ((index, value) in this.withIndex()) {
        QuickJS.setProperty(context.ref, ref, index, value.toJSValue(context).ref)
    }
    return JSValue(ref, context)
}

fun <T: Any> Map<String, T>.toJSValue(context: JSContext): JSValue {
    val ref = QuickJS.newObject(context.ref)
    for ((key, value) in this) {
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

open class JSArray private constructor(){
    private lateinit var engine: JSEngine

    internal lateinit var content : V8Array

    constructor(engine: JSEngine) : this() {
        this.engine = engine
        content = V8Array(engine.runtime)
    }

    constructor(engine: JSEngine, v8Array: Any) : this() {
        require(v8Array is V8Array)
        this.engine = engine
        content = v8Array.twin()
    }

    fun add(value: Boolean) {
        content.push(value)
    }

    fun add(value: Int) {
        content.push(value)
    }

    fun add(value: Double) {
        content.push(value)
    }

    fun add(value: String) {
        content.push(value)
    }

    fun add(value: JsonElement) {
        content.push(JsonElementConverter.write(value, engine))
    }

    fun <T: JSConvertible> add(value: T, converter: JSConverter<T>) {
        val converted = converter.write(value, engine)
        content.push(converted)
    }

    fun getBoolean(index: Int) = content.getBoolean(index)

    fun getInt(index: Int) = content.getInteger(index)

    fun getDouble(index: Int) = content.getDouble(index)

    fun getString(index: Int): String = content.getString(index)

    fun getJsonElement(index: Int) = JsonElementConverter.read(content[index])

    operator fun get(index: Int): Any = content[index]

    fun <T> getJSConvertible(index: Int, converter: JSConverter<T>) : T = converter.read(content[index])

    fun release() = content.close()

    companion object {
        fun create(engine: JSEngine, closure: (JSArray) -> Unit) : JSArray {
            val array = JSArray(engine)
            closure(array)
            return array
        }
    }
}

open class JSObject(
    val context: JSContext,
    val pointer: Long) {

    fun add(key: String, value: Int) {
        context.setProperty(key, value)
    }

    fun add(key: String, value: Boolean) {
        content.add(key, value)
    }

    fun add(key: String, value: Double) {
        content.add(key, value)
    }

    fun add(key: String, value: String) {
        content.add(key, value)
    }

    fun add(key: String, value: JsonElement) {
        content.add(key, JsonElementConverter.write(value, engine))
    }

    fun <T: JSConvertible> add(key: String, value: T, converter: JSConverter<T>) {
        val converted = converter.write(value, engine)
        content.add(key, converted)
    }

    fun getBoolean(key: String) = content.getBoolean(key)

    fun getInt(key: String) = content.getInteger(key)

    fun getDouble(key: String) = content.getDouble(key)

    fun getString(key: String): String = content.getString(key)

    fun getJsonElement(key: String) = JsonElementConverter.read(content[key])

    operator fun get(key: String): Any = content[key]

    fun <T> getJSConvertible(key: String, converter: JSConverter<T>) : T = converter.read(content[key])

    fun release() = content.close()

    companion object {
        fun create(engine: JSEngine, closure: (JSObject) -> Unit) : JSObject {
            val obj = JSObject(engine)
            closure(obj)
            return obj
        }
    }
}

class JSFunction(val engine: JSEngine, val function : JSFunctionDefinition) {
    internal val callBack = JavaCallback { p0, p1 ->
        val obj = if (p0 != null) JSObject(engine, p0) else null
        val params = if (p1 != null) JSArray(engine, p1) else null
        function(obj, params)
    }
}

typealias JSFunctionDefinition = (JSObject?, JSArray?) -> Any?

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