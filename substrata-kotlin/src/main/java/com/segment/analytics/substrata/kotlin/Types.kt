package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.*
import com.segment.analytics.substrata.kotlin.j2v8.*
import kotlinx.serialization.json.JsonElement
import java.lang.Exception

interface JSConvertible {
    /**
     * convert an object to target type.
     * the object can be any of the following types:
     *  * int
     *  * boolean
     *  * double
     *  * string
     *  * v8 value
     */
    fun convert(engine: J2V8Engine) : Any
}

class JSFunction(val engine: J2V8Engine, val function : JSFunctionDefinition) {
    internal val callBack = JavaCallback { p0, p1 ->
        val obj = if (p0 != null) JSObject(engine, p0) else null
        val params = if (p1 != null) JSArray(engine, p1) else null
        function(obj, params)
    }
}

typealias JSFunctionDefinition = (JSObject?, JSArray?) -> JSResult

open class JSArray private constructor(){
    private lateinit var engine: J2V8Engine

    internal lateinit var content : V8Array

    constructor(engine: J2V8Engine) : this() {
        this.engine = engine
        content = V8Array(engine.runtime)
    }

    constructor(engine: J2V8Engine, v8Array: Any) : this() {
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

    fun add(value: JSConvertible) {
        val converted = value.convert(engine)
        content.push(converted)
    }

    fun release() = content.close()
}

open class JSObject private constructor(){
    private lateinit var engine: J2V8Engine

    internal lateinit var content : V8Object

    constructor(engine: J2V8Engine) : this() {
        this.engine = engine
        content = V8Object(engine.runtime)
    }

    constructor(engine: J2V8Engine, v8Object: Any) : this() {
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

    fun add(key: String, value: JSConvertible) {
        val converted = value.convert(engine)
        content.add(key, converted)
    }

    fun release() = content.close()
}

class JSResult internal constructor(private val content: Any) {
    private var released = false

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