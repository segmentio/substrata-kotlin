package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.JsonElement
import java.io.InputStream
import kotlin.reflect.KClass

interface JavascriptDataBridge {
    operator fun get(key: String): JSValue?
    operator fun set(key: String, value: JSValue)
}

interface JavascriptEngine {
    val bridge: JavascriptDataBridge

    fun loadBundle(bundleStream: InputStream, completion: (JSEngineError?) -> Unit)

    operator fun get(key: String): JSValue?
    operator fun set(key: String, value: JSValue)

    fun <T : JSExport> export(obj : T, objectName: String)
    fun <T : JSExport> export(clazz: KClass<T>, className: String)
    fun export(function: JSFunction, functionName: String)
    fun extend(objectName: String, function: JSFunction, functionName: String)

    fun call(function: String, params: List<JSValue> = emptyList()): JSValue
    fun call(function: JSFunction, params: List<JSValue> = emptyList()): JSValue
    fun call(jsObject: JSObjectRef, function: String, params: List<JSValue> = emptyList()): JSValue

    fun call(function: String, params: List<JsonElement> = emptyList()): JsonElement
    fun call(function: JSFunction, params: List<JsonElement> = emptyList()): JsonElement
    fun call(jsObject: JSObjectRef, function: String, params: List<JsonElement> = emptyList()): JsonElement

    fun evaluate(script: String): JSValue

    fun release()
}

sealed class JSEngineError : Exception() {
    object BundleNotFound : JSEngineError()
    object UnableToLoad : JSEngineError()
    class UnknownError(val error: Exception) : JSEngineError()
    class EvaluationError(
        val type: String,
        val stackTrace: String,
        val causeDetails: String
    ) : JSEngineError()

    class TimeoutError(val msg: String) : JSEngineError()
}

typealias JavascriptErrorHandler = (JSEngineError) -> Unit