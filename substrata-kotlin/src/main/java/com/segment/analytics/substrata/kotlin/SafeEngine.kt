package com.segment.analytics.substrata.kotlin

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

class JSScope(
    val timeoutInSeconds: Long = 120L,
    var exceptionHandler: JSExceptionHandler? = null
    ): Releasable {

    companion object {
        const val SUBSTRATA_THREAD = "SegmentSubstrataThread"
    }

    @PublishedApi
    internal val executor = Executors.newSingleThreadExecutor {
        Thread(it, SUBSTRATA_THREAD)
    }

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
    fun launch(global: Boolean = false, closure: JSEngine.() -> Unit) {
        try {
            optimize(global, closure)
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
    fun sync(global: Boolean = false, closure: JSEngine.() -> Unit) {
        try {
            optimize(global, closure).get(timeoutInSeconds, TimeUnit.SECONDS)
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
    fun <T> await(global: Boolean = false, closure: JSEngine.() -> T): T? {
        return try {
            optimize(global, closure).get(timeoutInSeconds, TimeUnit.SECONDS)
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

    private fun<T> optimize(global: Boolean = false,  closure: JSEngine.() -> T): Future<T> {
        val callable = Callable {
            val ret = engine.context.memScope(global) {
                engine.closure()
            }
            ret
        }

        // if we are already in the current thread, no need to submit to thread pool task to avoid deadlock
        return if (Thread.currentThread().name == SUBSTRATA_THREAD) {
            val task = FutureTask(callable)
            task.run()
            task
        }
        else {
            executor.submit(callable)
        }
    }
}

typealias JSExceptionHandler = (Exception) -> Unit
