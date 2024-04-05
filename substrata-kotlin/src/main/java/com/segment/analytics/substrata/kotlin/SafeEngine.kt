package com.segment.analytics.substrata.kotlin

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class JSScope(
    val timeoutInSeconds: Long = 120L,
    var exceptionHandler: JSExceptionHandler? = null
    ): Releasable {
    @PublishedApi
    internal val executor = Executors.newSingleThreadExecutor()

    @PublishedApi
    internal lateinit var engine : JSEngine

    init {
        executor.submit {
            engine = JSEngine()
        }.get()
    }

    inline fun launch(crossinline closure: JSEngine.() -> Unit) = engine.context.memScope {
        try {
            executor.submit {
                engine.closure()
            }
        } catch (ex: Exception) {
            exceptionHandler?.invoke(ex)
        }
    }

    inline fun sync(crossinline closure: JSEngine.() -> Unit) = engine.context.memScope {
        try {
            executor.submit {
                engine.closure()
            }.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.invoke(ex)
        }
    }

    inline fun <T> await(crossinline closure: JSEngine.() -> T): T?  = engine.context.memScope {
        return try {
            executor.submit(Callable { engine.closure() }).get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.invoke(ex)
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

typealias JSExceptionHandler = (Exception) -> Unit
