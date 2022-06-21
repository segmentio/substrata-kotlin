package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.V8Object
import com.segment.analytics.substrata.kotlin.JSValue
import com.segment.analytics.substrata.kotlin.JavascriptDataBridge

class J2V8DataBridge(
    private val engine: J2V8Engine
) : JavascriptDataBridge {
    companion object {
        private const val DataBridgeKey = "DataBridge"
    }

    init {
        // This constructor is being invoked from the v8 thread, so we dont need to use jsExecutor
        // if the above implementation changes, this should too
        engine.underlying.let { v8 ->
            val dictionary = V8Object(v8) // {}
            v8.add(DataBridgeKey, dictionary)
        }
    }

    override operator fun get(key: String): JSValue {
        val result = engine.syncRunEngine { v8 ->
            v8.executeScript("$DataBridgeKey.$key")
        }
        return engine.wrapAsJSValue(result)
    }

    override operator fun set(key: String, value: JSValue) {
        engine.syncRunEngine { v8 ->
            val dataBridge = v8.getObject(DataBridgeKey)
            when (value) {
                is JSValue.JSString -> dataBridge.add(key, value.content)
                is JSValue.JSBool -> dataBridge.add(key, value.content)
                is JSValue.JSInt -> dataBridge.add(key, value.content)
                is JSValue.JSDouble -> dataBridge.add(key, value.content)
                is JSValue.JSArray -> {
                    val jsRep = value.content
                    jsRep?.let {
                        dataBridge.add(key, engine.underlying.toV8Array(it))
                    }
                }
                is JSValue.JSObject -> {
                    val jsRep = value.content
                    jsRep?.let {
                        dataBridge.add(key, engine.underlying.toV8Object(it))
                    }
                }
                is JSValue.JSNull -> dataBridge.addNull(key)
                is JSValue.JSUndefined -> dataBridge.addUndefined(key)
                else -> Unit // NO-OP, do not allow assigning functions to dataBridge
            }
        }
    }
}
