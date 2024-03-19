package com.segment.analytics.substrata.kotlin

class JSDataBridge(
    private val engine: JSEngine,
    private val dictionary : JSObject = engine.context.newObject()
) : KeyValueObject by dictionary {

    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    init {
        engine[DataBridgeKey] = dictionary
    }
}
