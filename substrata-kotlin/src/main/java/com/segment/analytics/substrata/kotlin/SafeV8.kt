package com.segment.analytics.substrata.kotlin

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class JSScope(val timeoutInSeconds: Long = 120L) {
    @PublishedApi
    internal val executor = Executors.newSingleThreadExecutor()

    @PublishedApi
    internal lateinit var engine : JSEngine

    init {
        executor.submit {
            engine = JSEngine()
        }.get()
    }

    inline fun sync(exceptionHandler: JSExceptionHandler? = null, crossinline closure: (JSEngine) -> Unit) {
        try {
            executor.submit {
                closure(engine)
            }.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.onError(ex)
        }
    }

    inline fun <T> await(exceptionHandler: JSExceptionHandler? = null, crossinline closure: (JSEngine) -> T): T? {
        return try {
            executor.submit(Callable { closure(engine) }).get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.onError(ex)
            null
        }
    }
}

interface JSExceptionHandler {
    fun onError(e: Exception)
}
