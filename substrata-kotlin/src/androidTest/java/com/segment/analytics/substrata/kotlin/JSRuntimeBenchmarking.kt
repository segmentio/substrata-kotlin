package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.segment.analytics.kotlin.core.TrackEvent
import com.segment.analytics.substrata.kotlin.j2v8.toSegmentEvent
import com.segment.analytics.substrata.kotlin.j2v8.toV8Object
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.util.Date
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class JSRuntimeBenchmarking {
    private val appContext = InstrumentationRegistry.getInstrumentation().context
    val localJSMiddlewareInputStream = appContext.assets.open("sample2.js")
    val script = localJSMiddlewareInputStream.bufferedReader().use(BufferedReader::readText)
    val testScript =
        appContext.assets.open("sample3.js").bufferedReader().use(BufferedReader::readText)

    internal class Console {
        fun log(message: String) {
            println("[INFO] $message")
        }

        fun error(message: String) {
            println("[ERROR] $message")
        }
    }

    @Test
    fun benchmarkSerializationJ2V8() {
        val runtime: V8 = V8.createV8Runtime().also {
            val console = Console()
            val v8Console = V8Object(it)
            v8Console.registerJavaMethod(console,
                "log",
                "log",
                arrayOf<Class<*>>(String::class.java))
            v8Console.registerJavaMethod(console,
                "error",
                "err",
                arrayOf<Class<*>>(String::class.java))
            it.add("console", v8Console)
        }
        val e = TrackEvent(
            event = "App Closed",
            properties = buildJsonObject { put("new", false); put("click", true) }
        ).apply {
            messageId = "qwerty-1234"
            anonymousId = "anonId"
            integrations = buildJsonObject {
                put("key1", "true")
                put("key2", "2")
                put("key3", "4f")
                put("key4", "4f")
            }
            this.context = buildJsonObject {
                put("key1", true)
                put("key2", 2)
                put("key3", 4f)
                put("key4", 4f)
                put("key4", buildJsonObject {
                    put("key1", true)
                    put("key2", false)
                })
            }
            timestamp = Date(0).toInstant().toString()
        }

        val time1 = benchmark {
            serializeToJS(runtime, e)
        }
        println("[Benchmarking] (J2V8) Native Encode is $time1 ms")
        val time2 = benchmark {
            e.toV8Object(runtime)
        }
        println("[Benchmarking] (J2V8) Custom Encode is $time2 ms")
//
//
//        val time4 = benchmark {
//            e.toV8Object(runtime)
//        }
//        println("[Benchmarking] (J2V8) Custom Encode is $time4 ms")
//        val time3 = benchmark {
//            serializeToJS(runtime, e)
//        }
//        println("[Benchmarking] (J2V8) Native Encode is $time3 ms")
    }

    @Test
    fun benchmarkDeSerializationJ2V8() {
        val runtime: V8 = V8.createV8Runtime().also {
            val console = Console()
            val v8Console = V8Object(it)
            v8Console.registerJavaMethod(console,
                "log",
                "log",
                arrayOf<Class<*>>(String::class.java))
            v8Console.registerJavaMethod(console,
                "error",
                "err",
                arrayOf<Class<*>>(String::class.java))
            it.add("console", v8Console)
        }
        val e = TrackEvent(
            event = "App Closed",
            properties = buildJsonObject { put("new", false); put("click", true) }
        ).apply {
            messageId = "qwerty-1234"
            anonymousId = "anonId"
            integrations = buildJsonObject {
                put("key1", "true")
                put("key2", "2")
                put("key3", "4f")
                put("key4", "4f")
            }
            this.context = buildJsonObject {
                put("key1", true)
                put("key2", 2)
                put("key3", 4f)
                put("key4", 4f)
                put("key4", buildJsonObject {
                    put("key1", true)
                    put("key2", false)
                })
            }
            timestamp = Date(0).toInstant().toString()
        }

        val obj = e.toV8Object(runtime)
        println(obj)
//        val time1 = benchmark {
//            deserializeFromJS<TrackEvent>(obj)
//        }
//        println("[Benchmarking] (J2V8) Native Decode is $time1 ms")
        val time2 = benchmark {
            obj.toSegmentEvent<TrackEvent>()
        }
        println("[Benchmarking] (J2V8) Custom Decode is $time2 ms")
        val x: TrackEvent? = obj.toSegmentEvent()
        x?.let {
            println(Json { prettyPrint = true }.encodeToString(TrackEvent.serializer(), it))
        }
    }


//    @Test
//    fun testPerformance() {
//        for (i in 0..0) {
//        testDataBridgeQuick()
//        testDataBridgeJ2V8()
//            testReturnCallbackQuick()
//            testReturnCallbackJ2V8()
//        }
//    }
}

fun benchmark(times: Int = 10000, closure: () -> Unit): Double {
    val list = mutableListOf<Long>()
    for (i in 1..times) {
        list.add(measureTimeMillis(closure))
    }
    return list.average()
}
