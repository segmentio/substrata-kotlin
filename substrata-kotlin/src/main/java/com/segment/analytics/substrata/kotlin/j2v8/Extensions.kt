package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.*
import kotlinx.serialization.json.*


fun V8Object.add(key: String, value: Any?) {
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

internal fun List<JsonElement>.toV8Array(runtime: V8): V8Array{
    val result = V8Array(runtime)
    try {
        for (value in this) {
            result.push(value.toAny(runtime))
        }
    } catch (e: IllegalStateException) {
        result.close()
        throw e
    }

    return result
}

internal fun JsonElement.toAny(runtime: V8) : Any? {
    return when (this) {
        is JsonPrimitive -> toAny(runtime)
        is JsonObject -> toAny(runtime)
        is JsonArray -> toAny(runtime)
        else -> null
    }
}

internal fun JsonPrimitive.toAny(runtime: V8) : Any? {
    this.booleanOrNull?.let {
        return it
    }
    this.intOrNull?.let {
        return it
    }
    this.longOrNull?.let {
        return it
    }
    this.doubleOrNull?.let {
        return it
    }
    return contentOrNull
}

internal fun JsonArray.toAny(runtime: V8): Any{
    val result = V8Array(runtime)
    try {
        for (value in this) {
            result.push(value.toAny(runtime))
        }
    } catch (e: IllegalStateException) {
        result.close()
        throw e
    }

    return result
}

internal fun JsonObject.toAny(runtime: V8): Any {
    val result = V8Object(runtime)
    try {
        for ((key, value) in this) {
            result.add(key, value.toAny(runtime))
        }
    } catch (e: IllegalStateException) {
        result.close()
        throw e
    }
    return result
}

internal fun Any?.toJsonElement(): JsonElement{
    return when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Int -> JsonPrimitive(this)
        is Double -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is V8Function -> JsonNull
        is V8Array -> toJsonArray()
        is V8Object -> toJsonObject()
        else -> JsonNull
    }
}

internal fun V8Array.toJsonArray() = buildJsonArray {
    val v8Array = this@toJsonArray
    val jsonArray = this

    for (i in 0 until length()) {
        val value = v8Array[i].toJsonElement()
        if (value != JsonNull) {
            jsonArray.add(value)
        }
    }
}

internal fun V8Object.toJsonObject() = buildJsonObject {
    val v8Object = this@toJsonObject
    val jsonObject = this

    for (key in v8Object.keys) {
        jsonObject.put(key, v8Object[key].toJsonElement())
    }
}