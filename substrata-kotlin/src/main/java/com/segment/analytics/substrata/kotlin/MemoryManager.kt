package com.segment.analytics.substrata.kotlin

inline fun <T> JSContext.memScope(body: () -> T): T {
    val scope = MemoryManager(this)
    try {
        return body()
    } finally {
        scope.release()
    }
}

class MemoryManager(
    val context: JSContext
) : ReferenceHandler {
    init {
        context.addReferenceHandler(this)
    }

    private val references = mutableListOf<JSValue>()

    private var releasing = false

    var released = false

    fun release() {
        if (released) return

        releasing = true

        try {
            for (reference in references) {
                reference.release()
            }
            context.removeReferenceHandler(this)
            references.clear()
        }
        finally {
            releasing = false
        }

        released = true
    }

    override fun onCreated(reference: JSValue) {
        references.add(reference)
    }

    override fun onReleased(reference: JSValue) {
        if (releasing) return

        val iterator: MutableIterator<JSValue> = references.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() === reference) {
                iterator.remove()
                break
            }
        }
    }
}

interface ReferenceHandler {
    fun onCreated(reference: JSValue)

    fun onReleased(reference: JSValue)
}
