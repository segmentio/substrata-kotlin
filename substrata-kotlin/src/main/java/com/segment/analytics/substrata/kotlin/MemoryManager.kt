package com.segment.analytics.substrata.kotlin

inline fun <T> JSContext.memScope(body: JSContext.() -> T): T {
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

    /*
    *   we only need to hold the reference to the pointers,
    *   let GC to collect the instance of JSConvertible
    * */
    private val references = mutableSetOf<Long>()

    private var releasing = false

    var released = false

    fun release() {
        if (released) return

        releasing = true

        try {
            for (ref in references) {
                context.release(ref)
            }
            context.removeReferenceHandler(this)
            references.clear()
        }
        finally {
            releasing = false
        }

        released = true
    }

    override fun onCreated(reference: JSConvertible) {
        references.add(reference.ref)
    }

    override fun onReleased(reference: JSConvertible) {
        if (releasing) return

        val iterator: MutableIterator<Long> = references.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() == reference.ref) {
                iterator.remove()
                return
            }
        }
    }
}

interface ReferenceHandler {
    fun onCreated(reference: JSConvertible)

    fun onReleased(reference: JSConvertible)
}
