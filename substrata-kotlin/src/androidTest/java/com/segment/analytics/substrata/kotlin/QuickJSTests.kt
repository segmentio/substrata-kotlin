package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuickJSTests {

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
    fun testBool() = context.memScope {
        val v = newBool(true)
        val type = context.getType(v)
        assertEquals(QuickJS.TYPE_BOOLEAN, type)
        assertTrue(isBool(v))
        assertTrue(getBool(v))
    }

    @Test
    fun testInt() = context.memScope  {
        val v = newInt(123)
        val type = context.getType(v)
        assertEquals(QuickJS.TYPE_INT, type)
        assertTrue(isNumber(v))
        assertEquals(123, getInt(v))
    }

    @Test
    fun testDouble() = context.memScope  {
        val v = newDouble(123.1)
        val type = context.getType(v)
        assertEquals(QuickJS.TYPE_FLOAT64, type)
        assertTrue(isNumber(v))
        assertEquals(123.1, getDouble(v), 0.01)
    }

    @Test
    fun testString() = context.memScope  {
        val v = newString("abc")
        val type = context.getType(v)
        assertEquals(QuickJS.TYPE_STRING, type)
        assertTrue(isString(v))
        assertEquals("abc", getString(v))
    }

    @Test
    fun testArray() = context.memScope  {
        val arr = newArray()
        setProperty(arr, 0, newInt(1))
        setProperty(arr, 1, newBool(true))
        setProperty(arr, 2, newString("test"))
        assertTrue(isArray(arr))

        val type = context.getType(arr)
        assertEquals(QuickJS.TYPE_OBJECT, type)

        val e1: JSValue = getProperty(arr, 0)
        val e2: JSValue = getProperty(arr, 1)
        val e3: JSValue = getProperty(arr, 2)

        assertTrue(isNumber(e1))
        assertTrue(isBool(e2))
        assertTrue(isString(e3))
    }

    @Test
    fun testObject() = context.memScope  {
        val obj = newObject()
        setProperty(obj, "int", newInt(1))
        setProperty(obj, "bool", newBool(true))
        setProperty(obj, "string", newString("test"))

        val nestedObj = newObject()
        setProperty(nestedObj, "int", newInt(2))
        setProperty(nestedObj, "bool", newBool(false))
        setProperty(nestedObj, "string", newString("testtest"))
        setProperty(obj, "object", nestedObj)
        assertTrue(isObject(obj))

        val type = context.getType(obj)
        assertEquals(QuickJS.TYPE_OBJECT, type)

        val e1: JSValue = getProperty(obj, "int")
        val e2: JSValue = getProperty(obj, "bool")
        val e3: JSValue = getProperty(obj, "string")
        val e4: JSValue = getProperty(obj, "object")
        val e41: JSValue = getProperty(e4, "int")
        val e42: JSValue = getProperty(e4, "bool")
        val e43: JSValue = getProperty(e4, "string")

        assertTrue(isNumber(e1))
        assertTrue(isBool(e2))
        assertTrue(isString(e3))
        assertTrue(isObject(e4))
        assertTrue(isNumber(e41))
        assertTrue(isBool(e42))
        assertTrue(isString(e43))

        assertEquals(1, getInt(e1))
        assertEquals(true, getBool(e2))
        assertEquals("test", getString(e3))
        assertEquals(2, getInt(e41))
        assertEquals(false, getBool(e42))
        assertEquals("testtest", getString(e43))
    }

    @Test
    fun testNull() = context.memScope {
        val nil = JSNull
        val type = context.getType(nil)
        assertEquals(QuickJS.TYPE_NULL, type)
    }

    @Test
    fun testUndefined() = context.memScope {
        val undefined = JSUndefined
        val type = context.getType(undefined)
        assertEquals(QuickJS.TYPE_UNDEFINED, type)
    }

    @Test
    fun testGetPropertyNames() = context.memScope {
        val obj = newObject()
        setProperty(obj, "int", newInt(1))
        setProperty(obj, "bool", newBool(true))
        setProperty(obj, "string", newString("test"))

        val names = getPropertyNames(obj)
        assertEquals(3, names.size)
        assertEquals("int", names[0])
        assertEquals("bool", names[1])
        assertEquals("string", names[2])
    }
}