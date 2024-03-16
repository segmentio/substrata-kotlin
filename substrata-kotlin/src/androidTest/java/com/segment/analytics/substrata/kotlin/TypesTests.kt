package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
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
}