package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
import kotlinx.serialization.json.JsonElement
import java.lang.Exception

interface JSConvertible

class JSFunction(val engine: JSEngine, val function : JSFunctionDefinition) {
    internal val callBack = JavaCallback { p0, p1 ->
        val obj = if (p0 != null) JSObject(engine, p0) else null
        val params = if (p1 != null) JSArray(engine, p1) else null
        function(obj, params)
    }
}

typealias JSFunctionDefinition = (JSObject?, JSArray?) -> Any?

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

open class JSObject private constructor(){
    private lateinit var engine: JSEngine

    internal lateinit var content : V8Object

    constructor(engine: JSEngine) : this() {
        this.engine = engine
        content = V8Object(engine.runtime)
    }

    constructor(engine: JSEngine, v8Object: Any) : this() {
        require(v8Object is V8Object)
        this.engine = engine
        content = v8Object.twin()
    }

    fun add(key: String, value: Int) {
        content.add(key, value)
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