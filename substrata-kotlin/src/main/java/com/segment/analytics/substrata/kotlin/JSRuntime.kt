package com.segment.analytics.substrata.kotlin

import java.lang.IllegalStateException

class JSRuntime(val ref: Long): Releasable {
    fun createJSContext(): JSContext {
        val context = QuickJS.newContext(ref)
        if (context == 0L) {
            throw IllegalStateException("Failed to create JSContext")
        }
        return JSContext(context)
    }

    override fun release() {
        QuickJS.freeRuntime(ref)
    }
}