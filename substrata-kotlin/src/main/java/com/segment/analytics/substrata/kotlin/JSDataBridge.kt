package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.JsonElement

class JSDataBridge(
    private val engine: JSEngine
) {
    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    operator fun get(key: String): JSResult = JSResult(engine["$DataBridgeKey.$key"])

    fun set(key: String, value: Boolean) {
        engine["$DataBridgeKey.$key"] = value
    }

    fun set(key: String, value: Int) {
        engine["$DataBridgeKey.$key"] = value
    }

    fun set(key: String, value: Double) {
        engine["$DataBridgeKey.$key"] = value
    }

    operator fun set(key: String, value: String) {
        engine["$DataBridgeKey.$key"] = value
    }

    fun set(key: String, value: JsonElement) {
        engine["$DataBridgeKey.$key"] = value
    }

    fun set(key: String, value: JSConvertible) {
        engine["$DataBridgeKey.$key"] = value
    }
}
