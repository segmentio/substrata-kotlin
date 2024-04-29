package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TypesTests {

    lateinit var runtime: JSRuntime
    lateinit var context: JSContext

    @Before
    fun setUp() {
        runtime = QuickJS.createJSRuntime()
        context = runtime.createJSContext()
    }

    @After
    fun tearDown() {
        context.release()
        runtime.release()
    }

    @Test
    fun testJSObject() = context.memScope {
        val obj = newObject()
        obj["int"] = 1
        obj["bool"] = true
        obj["string"] = "test"
        obj["double"] = 1.1

        val nestedObj = newObject()
        nestedObj["int"] = 2
        nestedObj["bool"] = false
        nestedObj["string"] = "testtest"
        nestedObj["double"] = 2.2

        obj["object"] = nestedObj

        assertEquals(1, obj.getInt("int"))
        assertEquals(true, obj.getBoolean("bool"))
        assertEquals("test", obj.getString("string"))
        assertEquals(1.1, obj.getDouble("double"), 0.01)
        val jsObject = obj.getJSObject("object")
        assertEquals(2, jsObject.getInt("int"))
        assertEquals(false, jsObject.getBoolean("bool"))
        assertEquals("testtest", jsObject.getString("string"))
        assertEquals(2.2, jsObject.getDouble("double"), 0.01)
    }

    @Test
    fun testJSArray() = context.memScope {
        val arr = newArray()
        arr.add(1)
        arr.add(true)
        arr.add("test")
        arr.add(1.1)

        val nestedObj = newObject()
        nestedObj["int"] = 2
        nestedObj["bool"] = false
        nestedObj["string"] = "testtest"
        nestedObj["double"] = 2.2

        val nestedArr = newArray()
        nestedArr.add(3)
        nestedArr.add(true)
        nestedArr.add("testtesttest")
        nestedArr.add(3.3)

        arr.add(nestedObj)
        arr.add(nestedArr)

        assertEquals(1, arr.getInt(0))
        assertEquals(true, arr.getBoolean(1))
        assertEquals("test", arr.getString(2))
        assertEquals(1.1, arr.getDouble(3), 0.01)
        val jsObject = arr.getJSObject(4)
        assertEquals(2, jsObject.getInt("int"))
        assertEquals(false, jsObject.getBoolean("bool"))
        assertEquals("testtest", jsObject.getString("string"))
        assertEquals(2.2, jsObject.getDouble("double"), 0.01)
        val jsArr = arr.getJSArray(5)
        assertEquals(4, jsArr.size)
        assertEquals(3, jsArr.getInt(0))
        assertEquals(true, jsArr.getBoolean(1))
        assertEquals("testtesttest", jsArr.getString(2))
        assertEquals(3.3, jsArr.getDouble(3), 0.01)


        assertEquals(1, arr[0])
        assertEquals(true, arr[1])
        assertEquals("test", arr[2])
        assertEquals(1.1, arr[3])
        assertEquals(2, jsObject["int"])
        assertEquals(false, jsObject["bool"])
        assertEquals("testtest", jsObject["string"])
        assertEquals(2.2, jsObject["double"] as Double, 0.01)
        assertEquals(3, jsArr[0])
        assertEquals(true, jsArr[1])
        assertEquals("testtesttest", jsArr[2])
        assertEquals(3.3, jsArr[3] as Double, 0.01)
    }

    @Test
    fun testJsonElement() = context.memScope {
        val obj = newObject()
        obj["json"] = buildJsonObject {
            put("int", 1)
            put("boolean", true)
            put("string", "test")
            put("double", 1.1)
            put("long", 1710556642L)
            put("object", buildJsonObject {
                put("int", 2)
                put("boolean", false)
                put("string", "testtest")
                put("double", 2.2)
            })
            put("array", buildJsonArray {
                add(3)
                add(true)
                add("testtesttest")
                add(3.3)
            })
        }

        val json = obj.getJsonElement("json").jsonObject
        assertEquals(1, json["int"]?.jsonPrimitive?.int)
        assertEquals(true, json["boolean"]?.jsonPrimitive?.boolean)
        assertEquals("test", json["string"]?.jsonPrimitive?.content)
        assertEquals(1.1, json["double"]?.jsonPrimitive?.double)
        assertEquals(1710556642L, json["long"]?.jsonPrimitive?.long)

        val nestedObj = json["object"]?.jsonObject!!
        assertEquals(2, nestedObj["int"]?.jsonPrimitive?.int)
        assertEquals(false, nestedObj["boolean"]?.jsonPrimitive?.boolean)
        assertEquals("testtest", nestedObj["string"]?.jsonPrimitive?.content)
        assertEquals(2.2, nestedObj["double"]?.jsonPrimitive?.double)

        val nestedArr = json["array"]?.jsonArray!!
        assertEquals(3, nestedArr[0].jsonPrimitive.int)
        assertEquals(true, nestedArr[1].jsonPrimitive.boolean)
        assertEquals("testtesttest", nestedArr[2].jsonPrimitive.content)
        assertEquals(3.3, nestedArr[3].jsonPrimitive.double, 0.01)
    }

    @Test
    fun testJsonArray() = context.memScope {
        val arr = newArray()
        arr.add(buildJsonObject {
            put("int", 2)
            put("boolean", false)
            put("string", "testtest")
            put("double", 2.2)
        })
        arr.add(
            buildJsonArray {
                add(3)
                add(true)
                add("testtesttest")
                add(3.3)
            }
        )

        val nestedObj = arr.getJsonElement(0).jsonObject
        assertEquals(2, nestedObj["int"]?.jsonPrimitive?.int)
        assertEquals(false, nestedObj["boolean"]?.jsonPrimitive?.boolean)
        assertEquals("testtest", nestedObj["string"]?.jsonPrimitive?.content)
        assertEquals(2.2, nestedObj["double"]?.jsonPrimitive?.double)

        val nestedArr = arr.getJsonElement(1).jsonArray
        assertEquals(3, nestedArr[0].jsonPrimitive.int)
        assertEquals(true, nestedArr[1].jsonPrimitive.boolean)
        assertEquals("testtesttest", nestedArr[2].jsonPrimitive.content)
        assertEquals(3.3, nestedArr[3].jsonPrimitive.double, 0.01)
    }

    @Test
    fun testJsonElementConverter() {
        val json = """
            {"properties":{"version":1,"test": null, "build":1,"from_background":false},"event":"Application Opened","type":"track","messageId":"2132f014-a8fe-41b6-b714-0226db39e0d3","anonymousId":"a7bffc58-991e-4a2d-98a7-2a04abb3ea93","integrations":{},"context":{"library":{"name":"analytics-kotlin","version":"1.15.0"},"instanceId":"49f19161-6d56-4024-b23d-7f32d6ab9982","app":{"name":"analytics-kotlin-live","version":1,"namespace":"com.segment.analytics.liveplugins.app","build":1},"device":{"id":"87bc73d4e4ca1608da083975d36421aef0411dff765c9766b9bfaf266b7c1586","manufacturer":"Google","model":"sdk_gphone64_arm64","name":"emu64a","type":"android"},"os":{"name":"Android","version":14},"screen":{"density":2.75,"height":2154,"width":1080},"network":{},"locale":"en-US","userAgent":"Dalvik/2.1.0 (Linux; U; Android 14; sdk_gphone64_arm64 Build/UE1A.230829.036.A1)","timezone":"America/Chicago","livePluginMessage":"This came from a LivePlugin"},"userId":"","_metadata":{"bundled":[],"unbundled":[],"bundledIds":[]},"timestamp":"2024-04-25T16:40:55.994Z"}
        """.trimIndent()
        val content = Json.parseToJsonElement(json)

        context.memScope {
            val jsObject = JsonElementConverter.write(content, this)
            assert(jsObject is JSObject)
            val jsonObject = JsonElementConverter.read(jsObject)
            assertNotNull(jsonObject)
        }
    }
}