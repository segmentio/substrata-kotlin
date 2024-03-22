package com.segment.analytics.substrata.kotlin

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class JSShared {
    companion object {

        private val _nextFunctionId = AtomicInteger(0)
        val nextFunctionId: Int
            get() {
                return _nextFunctionId.getAndIncrement()
            }

        val functions = ConcurrentHashMap<Int, JSFunctionBody>()

        @JvmStatic
        fun jsCallback(context: JSContext, functionId: Int, args: LongArray): Long {
            functions[functionId]?.let { f ->
                val jsValues = args.map { return@map context.get<Any>(it) }
                f(jsValues)?.let {
                    if (it !is Unit) return it.toJSValue(context).ref
                }
            }

            return context.JSUndefined.ref
        }
    }
}