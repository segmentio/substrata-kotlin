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

    /**
     * run a task in background.
     *
     * if `global` set to true, the MemoryManager won't automatically release the JSValues created
     * within the task, making the values persist across tasks. values persisted in the global scope
     * are released when the engine is released. do not abuse global scope to avoid OOM exception
     *
     * @param global whether to run the task in the global scope.
     * @param closure content of the task
     */
    inline fun launch(global: Boolean = false, crossinline closure: JSEngine.() -> Unit) {
        try {
            executor.submit {
                if (global) {
                    engine.closure()
                }
                else {
                    engine.context.memScope {
                        engine.closure()
                    }
                }
            }
        } catch (ex: Exception) {
            exceptionHandler?.invoke(ex)
        }
    }

    /**
     * run a task in a blocking way.
     *
     * if `global` set to true, the MemoryManager won't automatically release the JSValues created
     * within the task, making the values persist across tasks. values persisted in the global scope
     * are released when the engine is released. do not abuse global scope to avoid OOM exception
     *
     * @param global whether to run the task in the global scope.
     * @param closure content of the task
     */
    inline fun sync(global: Boolean = false, crossinline closure: JSEngine.() -> Unit) {
        try {
            executor.submit {
                if (global) {
                    engine.closure()
                }
                else {
                    engine.context.memScope {
                        engine.closure()
                    }
                }
            }.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            exceptionHandler?.invoke(ex)
        }
    }

    /**
     * run a task in a blocking way and return the value of the task.
     *
     * if `global` set to true, the MemoryManager won't automatically release the JSValues created
     * within the task, making the values persist across tasks. values persisted in the global scope
     * are released when the engine is released. do not abuse global scope to avoid OOM exception.
     *
     * NOTE: do not return JSValue from the closure/task, since the returned JSValue might be already
     * released when exiting the scope. convert JSValue to the type you want and return the converted
     * value.
     *
     * @param global whether to run the task in the global scope.
     * @param closure content of the task
     */
    inline fun <T> await(global: Boolean = false, crossinline closure: JSEngine.() -> T): T? {
        return try {
            executor.submit(Callable {
                if (global) {
                    engine.closure()
                }
                else {
                    engine.context.memScope {
                        engine.closure()
                    }
                }
            }).get(timeoutInSeconds, TimeUnit.SECONDS)
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
