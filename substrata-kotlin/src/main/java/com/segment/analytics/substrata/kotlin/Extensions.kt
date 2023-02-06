package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value


internal fun V8Object.add(key: String, value: Any?) {
    when (value) {
        null -> {
            addNull(key)
        }
        V8.getUndefined() -> {
            addUndefined(key)
        }
        is Double -> {
            add(key, value)
        }
        is Int -> {
            add(key, value)
        }
        is Float -> {
            add(key, value as Double)
        }
        is Number -> {
            add(key, value as Double)
        }
        is Boolean -> {
            add(key, value)
        }
        is String -> {
            add(key, value)
        }
        else -> {
            require(value is V8Value)
            add(key, value)
        }
    }
}