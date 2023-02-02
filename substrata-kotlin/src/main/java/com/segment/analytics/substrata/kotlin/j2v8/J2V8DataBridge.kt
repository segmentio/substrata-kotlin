package com.segment.analytics.substrata.kotlin.j2v8

import com.segment.analytics.substrata.kotlin.JSValue
import com.segment.analytics.substrata.kotlin.JavascriptDataBridge

class J2V8DataBridge(
    private val engine: J2V8Engine
) : JavascriptDataBridge {
    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    override operator fun get(key: String): JSValue = engine["$DataBridgeKey.$key"]

    override operator fun set(key: String, value: JSValue) {
        engine["$DataBridgeKey.$key"] = value
    }
}
