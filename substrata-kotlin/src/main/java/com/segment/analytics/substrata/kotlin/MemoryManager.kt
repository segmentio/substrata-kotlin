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
    private val references = mutableMapOf<Long, MutableSet<JSConvertible>>()

    private var releasing = false

    var released = false

    fun release() {
        if (released) return

        releasing = true

        try {
            for ((ref, list) in references) {
                context.release(ref)
                for (v in list) {
                    if (v is JSValue) {
                        v.releaseReference()
                    }
                }
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
        if (!references.containsKey(reference.ref)) {
            references.put(reference.ref, mutableSetOf())
        }
        references[reference.ref]!!.add(reference)
    }

    override fun onReleased(reference: JSConvertible) {
        if (releasing) return

        val iterator: MutableIterator<MutableMap.MutableEntry<Long, MutableSet<JSConvertible>>> = references.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key == reference.ref) {
                entry.value.remove(reference)
                if (entry.value.size == 0) {
                    iterator.remove()
                }
                break
            }
        }
    }
}

interface ReferenceHandler {
    fun onCreated(reference: JSConvertible)

    fun onReleased(reference: JSConvertible)
}
