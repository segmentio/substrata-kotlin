package com.segment.analytics.substrata.kotlin

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

    fun <C : Any> expose(clazz: KClass<C>, className: String)
    fun expose(function: JSValue.JSFunction, functionName: String)
    fun extend(objectName: String, function: JSValue.JSFunction, functionName: String)

    fun call(function: String, params: List<JSValue> = emptyList()): JSValue?
    fun call(function: JSValue.JSFunction, params: List<JSValue> = emptyList()): JSValue?
    fun call(
        receiver: JSValue.JSObjectReference,
        function: String,
        params: List<JSValue> = emptyList()
    ): JSValue?

    fun execute(script: String): JSValue?
    fun <T : Any> expose(key: String, value: T)

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