package com.segment.analytics.substrata.kotlin.j2v8

import com.segment.analytics.kotlin.core.BaseEvent
import com.segment.analytics.substrata.kotlin.JSValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

fun J2V8Engine.toJSObject(event: BaseEvent): JSValue.JSObject =
    JSValue.JSObject(event.toV8Object(underlying))

fun J2V8Engine.toJSObject(map: Map<String, JsonElement>): JSValue.JSObject =
    JSValue.JSObject(underlying.toV8Object(map))

fun J2V8Engine.toJSArray(list: List<JsonElement>): JSValue.JSArray =
    JSValue.JSArray(underlying.toV8Array(list))

fun <T: BaseEvent> toSegmentEvent(jsObj: JSValue.JSObject): T? =
    jsObj.content.toSegmentEvent()

fun fromJSObject(jsObj: JSValue.JSObject): JsonObject? =
    fromV8Object(jsObj.content)

fun fromJSArray(jsObj: JSValue.JSArray): JsonArray? =
    fromV8Array(jsObj.content)