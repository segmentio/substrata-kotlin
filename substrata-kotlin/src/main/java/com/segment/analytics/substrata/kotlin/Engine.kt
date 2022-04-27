package com.segment.analytics.substrata.kotlin

import com.eclipsesource.v8.V8Value
import java.lang.Exception
import kotlin.reflect.KClass

interface JavascriptDataBridge {
    operator fun get(key: String): JSValue?
    operator fun set(key: String, value: JSValue)
}

interface JavascriptEngine {
    val bridge: JavascriptDataBridge

    fun loadBundle(completion: (Error) -> Unit)

    operator fun get(key: String): JSValue?
    operator fun set(key: String, value: JSValue)

    fun <C : Any> expose(clazz: KClass<C>, className: String)
    fun expose(function: JSValue.JSFunction, functionName: String)
    fun expose(objectName: String, function: JSValue.JSFunction, functionName: String)

    fun call(function: JSValue, params: List<JSValue> = emptyList()): JSValue?
    fun call(function: String, params: List<JSValue> = emptyList()): JSValue?

    fun execute(script: String): JSValue?
}

sealed interface JSEngineError {
    object BundleNotFound : JSEngineError
    object UnableToLoad : JSEngineError
    class UnknownError(val error: Error) : JSEngineError
    class EvaluationError(val script: String) : JSEngineError
    class TimeoutError(val script: String) : JSEngineError
}

typealias JSErrorHandler = (JSEngineError) -> Unit