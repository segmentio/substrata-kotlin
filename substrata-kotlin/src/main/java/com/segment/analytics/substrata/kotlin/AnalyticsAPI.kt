package com.segment.analytics.substrata.kotlin

import android.annotation.SuppressLint
import android.content.Context
import com.eclipsesource.v8.V8Object
import com.segment.analytics.kotlin.android.AndroidStorageProvider
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.core.Configuration
import com.segment.analytics.kotlin.core.emptyJsonObject
import com.segment.analytics.substrata.kotlin.j2v8.fromV8Object

/*
Ideas on getting context
- Store in static field and initialize on plugin load
- create an interface for providing context??
- pass main analytics obj and then get application from there
*/

@SuppressLint("StaticFieldLeak")
object ContextProvider {
    var context: Context? = null
}

class AnalyticsAPI(writeKey: String) {

    init {
        println("[PRAY] Creating analytics with $writeKey")
    }

    val analytics = Analytics(
        Configuration(
            writeKey = writeKey,
            application = ContextProvider.context, // somehow fetch android context for storage
            storageProvider = AndroidStorageProvider
        )
    )

    // TODO figure out how to set these vars, do these have to be the same as the main analytics one? [Probably not]
    // launch in a scope and use store to keep them up-to-date
    var anonymousId: String = ""
    var userId: String = ""
    var traits: V8Object? = null
    var settings: V8Object? = null

    fun track(eventName: String, properties: V8Object? = null) {
        val jsonRep = fromV8Object(properties) ?: emptyJsonObject
        analytics.track(eventName, jsonRep)
    }

    fun identify(userId: String, traits: V8Object? = null) {
        val jsonRep = fromV8Object(traits) ?: emptyJsonObject
        analytics.identify(userId, jsonRep)
    }

    fun screen(screenName: String, category: String = "", properties: V8Object? = null) {
        val jsonRep = fromV8Object(properties) ?: emptyJsonObject
        analytics.screen(screenName, jsonRep, category)
    }

    fun group(groupId: String, traits: V8Object? = null) {
        val jsonRep = fromV8Object(traits) ?: emptyJsonObject
        analytics.group(groupId, jsonRep)
    }

    fun alias(userId: String) {
        analytics.alias(userId)
    }

    fun anonymousId(): String {
        return anonymousId
    }

    fun userId(): String {
        return userId
    }

    fun traits(): V8Object? {
        return traits
    }

    fun settings(): V8Object? {
        return settings
    }

    fun reset() {
        analytics.reset()
    }
}