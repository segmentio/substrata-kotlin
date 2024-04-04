package com.segment.analytics.substrata.kotlin

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class JSScope(val timeoutInSeconds: Long = 120L): Releasable {
    @PublishedApi
    internal val executor = Executors.newSingleThreadExecutor()

    @PublishedApi
    internal lateinit var engine : JSEngine

    init {
        executor.submit {
            engine = JSEngine()
        }.get()
    }

    inline fun launch(
        exceptionHandler: JSExceptionHandler? = null,
        crossinline closure: (JSEngine) -> Unit) = engine.context.memScope {
        try {
            executor.submit {
                closure(engine)
            }
        } catch (ex: Exception) {
            exceptionHandler?.onError(ex)
        }
    }

    inline fun sync(
        exceptionHandler: JSExceptionHandler? = null,
        crossinline closure: (JSEngine) -> Unit) = engine.context.memScope {
        try {
            executor.submit {
                closure(engine)
            }.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.onError(ex)
        }
    }

    inline fun <T> await(
        exceptionHandler: JSExceptionHandler? = null,
        crossinline closure: (JSEngine) -> T): T?  = engine.context.memScope {
        return try {
            executor.submit(Callable { closure(engine) }).get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.onError(ex)
            null
        }
    }

    override fun release() {
        executor.submit {
            engine.release()
        }.get(timeoutInSeconds, TimeUnit.SECONDS)
        executor.shutdown()
    }
}

interface JSExceptionHandler {
    fun onError(e: Exception)
}
