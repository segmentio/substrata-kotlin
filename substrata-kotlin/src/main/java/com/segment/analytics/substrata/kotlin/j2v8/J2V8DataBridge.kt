package com.segment.analytics.substrata.kotlin.j2v8

import com.segment.analytics.substrata.kotlin.JSConvertible
import com.segment.analytics.substrata.kotlin.JSResult
import com.segment.analytics.substrata.kotlin.JavascriptDataBridge
import kotlinx.serialization.json.JsonElement

class J2V8DataBridge(
    private val engine: J2V8Engine
) : JavascriptDataBridge {
    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    override operator fun get(key: String): JSResult = JSResult(engine["$DataBridgeKey.$key"])

    override fun set(key: String, value: Boolean) {
        engine["$DataBridgeKey.$key"] = value
    }

    override fun set(key: String, value: Int) {
        engine["$DataBridgeKey.$key"] = value
    }

    override fun set(key: String, value: Double) {
        engine["$DataBridgeKey.$key"] = value
    }

    override operator fun set(key: String, value: String) {
        engine["$DataBridgeKey.$key"] = value
    }

    override fun set(key: String, value: JsonElement) {
        engine["$DataBridgeKey.$key"] = value
    }

    override fun set(key: String, value: JSConvertible) {
        engine["$DataBridgeKey.$key"] = value
    }
}
