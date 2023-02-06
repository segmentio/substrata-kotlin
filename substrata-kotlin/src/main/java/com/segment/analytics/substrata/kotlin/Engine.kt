package com.segment.analytics.substrata.kotlin

import kotlinx.serialization.json.JsonElement
import java.io.InputStream
import kotlin.reflect.KClass

interface JavascriptDataBridge {
    operator fun get(key: String): JSResult
    operator fun set(key: String, value: Boolean)
    operator fun set(key: String, value: Int)
    operator fun set(key: String, value: Double)
    operator fun set(key: String, value: String)
    operator fun set(key: String, value: JsonElement)
    operator fun set(key: String, value: JSConvertible)
}

interface JavascriptEngine {
    val bridge: JavascriptDataBridge

    fun loadBundle(bundleStream: InputStream, completion: (JSEngineError?) -> Unit)

    operator fun get(key: String): JSResult
    operator fun set(key: String, value: Boolean)
    operator fun set(key: String, value: Int)
    operator fun set(key: String, value: Double)
    operator fun set(key: String, value: String)
    operator fun set(key: String, value: JsonElement)
    operator fun set(key: String, value: JSConvertible)

    fun <T : JSExport> export(obj : T, objectName: String)
    fun <T : JSExport> export(clazz: KClass<T>, className: String)
    fun export(function: JSFunction, functionName: String)
    fun extend(objectName: String, function: JSFunction, functionName: String)

    fun call(function: String, params: JSArray): JSResult
    fun call(function: JSFunction, params: JSArray): JSResult
    fun call(jsObject: JSObject, function: String, params: JSArray): JSResult

    fun evaluate(script: String): JSResult

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