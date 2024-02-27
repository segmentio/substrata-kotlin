package com.segment.analytics.substrata.kotlin

class JSContext(
    val ref: Long
) {
    val referenceHandlers = mutableSetOf<ReferenceHandler>()

    fun addReferenceHandler(handler: ReferenceHandler) {
        referenceHandlers.add(handler)
    }

    fun removeReferenceHandler(handler: ReferenceHandler) {
        referenceHandlers.remove(handler)
    }

    fun notifyReferenceCreated(reference: JSValue) {
        for (handler in referenceHandlers) {
            handler.onCreated(reference)
        }
    }

    fun notifyReferenceReleased(reference: JSValue) {
        for (handler in referenceHandlers) {
            handler.onReleased(reference)
        }
    }
}