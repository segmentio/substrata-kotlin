/**
 * Functions to help bridge between kotlin JSON and V8 JSON
 * - Converting BaseEvent to V8Object and vice-versa
 * - Converting JsonObject to V8Object and vice-versa
 */
package com.segment.analytics.substrata.kotlin.j2v8

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.utils.MemoryManager
/**
 * Lambda scope for allocating and safely cleaning V8Object
 */
inline fun <T> V8.memScope(body: () -> T): T {
    val scope = MemoryManager(this)
    try {
        return body()
    } finally {
        scope.release()
    }
}