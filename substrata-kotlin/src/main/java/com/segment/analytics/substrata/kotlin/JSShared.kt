package com.segment.analytics.substrata.kotlin

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class JSShared {
    companion object {

        private var _nextFunctionId = AtomicInteger(0)
        val nextFunctionId: Int
            get() {
                return _nextFunctionId.getAndIncrement()
            }

        var functions = ConcurrentHashMap<Int, JSFunctionBody>()
            private set

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

        fun reset() {
            _nextFunctionId = AtomicInteger(0)
            functions.clear()
        }
    }
}