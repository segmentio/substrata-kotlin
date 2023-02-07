package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.V8Object
import kotlinx.serialization.json.JsonElement

class JSDataBridge(
    private val engine: JSEngine
) {
    private val dictionary : V8Object = V8Object(engine.runtime)

    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    init {
        engine.runtime.add(DataBridgeKey, dictionary)
    }

    operator fun get(key: String): JSResult = JSResult(dictionary[key])

    operator fun set(key: String, value: Boolean) {
        dictionary.add(key, value)
    }

    operator fun set(key: String, value: Int) {
        dictionary.add(key, value)
    }

    operator fun set(key: String, value: Double)  {
        dictionary.add(key, value)
    }

    operator fun set(key: String, value: String) {
        dictionary.add(key, value)
    }

    operator fun set(key: String, value: JsonElement) {
        val converted = JsonElementConverter.write(value, engine)
        dictionary.add(key, converted)
    }

    fun <T: JSConvertible> set(key: String, value: T, converter: JSConverter<T>) {
        val converted = converter.write(value, engine)
        dictionary.add(key, converted)
    }

    fun getString(key: String): String = dictionary.getString(key)

    fun getBoolean(key: String) = dictionary.getBoolean(key)

    fun getInt(key: String) = dictionary.getInteger(key)

    fun getDouble(key: String) = dictionary.getDouble(key)

    fun getJsonElement(key: String) : JsonElement {
        val value = this[key]
        return value.read(JsonElementConverter)
    }

    fun <T : JSConvertible> getJSConvertible(key: String, converter: JSConverter<T>) : T {
        val value = this[key]
        return value.read(converter)
    }
}
