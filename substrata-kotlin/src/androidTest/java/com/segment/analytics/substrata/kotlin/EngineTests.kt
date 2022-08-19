package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.segment.analytics.substrata.kotlin.j2v8.J2V8Engine
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EngineTests {

    val engine = J2V8Engine()
    val underlying = engine.underlying

    @Test
    fun testInit() {
        runOnJSThread {
            val console = underlying.get("console")
            assertNotNull(console)

            val dataBridge = underlying.get("DataBridge")
            assertNotNull(dataBridge)
        }
    }

    @Test
    fun testDataBridge() {
        engine.bridge["string"] = "123".asJSValue()
        engine.bridge["number"] = 123.asJSValue()
        engine.bridge["bool"] = false.asJSValue()
        runOnJSThread {
            val bridge = underlying.getObject("DataBridge")
            assertEquals("123", bridge.get("string"))
            assertEquals(123, bridge.get("number"))
            assertEquals(false, bridge.get("bool"))
        }
        assertEquals("123".asJSValue(), engine.bridge["string"])
        assertEquals(123.asJSValue(), engine.bridge["number"])
        assertEquals(false.asJSValue(), engine.bridge["bool"])
    }

    @Test
    fun testLoadBundle() {
        val script = """
            console.log("Starting");
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        engine.loadBundle(script.byteInputStream()) {
            assertNull(it)
        }
        assertEquals("Ready to setup".asJSValue(), engine.bridge["foo"])
    }

    @Test
    fun testGet() {
        val script = """
            console.log("Starting");
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        engine.loadBundle(script.byteInputStream()) {
            assertNull(it)
        }
        assertEquals("Ready to setup".asJSValue(), engine["DataBridge.foo"])
        assertEquals("Ready to setup".asJSValue(), engine["foo"])
        assertEquals(JSValue.JSUndefined, engine["bar"])
    }

    @Test
    fun testSet() {
        val script = """
            console.log("Starting");
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        engine.loadBundle(script.byteInputStream()) {
            assertNull(it)
        }
        engine["foo"] = "Modified".asJSValue()
        assertEquals("Ready to setup".asJSValue(), engine["DataBridge.foo"])
        assertEquals("Modified".asJSValue(), engine["foo"])
    }

    @Test
    fun testExtend() {
        var data = "Something"
        engine.extend("foobar", JSValue.JSFunction { obj, params ->
            data = "Something new"
            null
        }, "method1")
        engine.execute("foobar.method1()")
        assertEquals("Something new", data)
    }

    @Test
    fun testExtendObject() {
        val script = """
            var x = {};
        """.trimIndent()
        var data = "Something"
        engine.loadBundle(script.byteInputStream()) {
            assertNull(it)
        }
        engine.extend("x", JSValue.JSFunction { obj, params ->
            data = "Something new"
            null
        }, "method1")
        engine.execute("x.method1()")
        assertEquals("Something new", data)
    }

    @Test
    fun testCall() {
        val script = """
            function add(x, y) {
                return x+y;
            }
        """.trimIndent()
        var data = "Something"
        engine.loadBundle(script.byteInputStream()) {
            assertNull(it)
        }
        val retVal = engine.call("add", listOf(10.asJSValue(), 20.asJSValue()))
        assertEquals(30.asJSValue(), retVal)
        engine.call(JSValue.JSFunction { obj, params ->
            data = "Modified"
            null
        })
        assertEquals("Modified", data)
    }

    @Test
    fun testCallOnJ2V8Object() {
        val script = """
            class Calculator {
                add(x, y) {
                    return x + y
                }
            }
            
            var calc = new Calculator();
        """.trimIndent()
        engine.loadBundle(script.byteInputStream()) {
            assertNull(it)
        }
        val calc = runOnJSThread { JSValue.JSObjectReference(underlying.getObject("calc")) }
        val retVal = engine.call(calc, "add", listOf(10.asJSValue(), 20.asJSValue()))
        assertEquals(30.asJSValue(), retVal)
    }

    @Test
    fun testExposeObjectAndClass() {
        class Bucket {
            var empty: Boolean = true

            fun fill() {
                empty = false
            }

            fun isEmpty(): Boolean {
                return empty
            }
        }
        val bucket1 = Bucket()
        engine.expose(Bucket::class, "Bucket")
        engine.expose("bucketMain", bucket1)
        engine.execute("""var bucketTmp = new Bucket();""")

        val test0 = engine.execute("bucketMain.isEmpty() && bucketTmp.isEmpty()")
        assertEquals(true.asJSValue(), test0)
        val test1 = engine.execute("bucketMain.fill(); bucketMain.isEmpty();")
        assertEquals(false.asJSValue(), test1)
        val test2 = engine.execute("bucketTmp.fill(); bucketTmp.isEmpty();")
        assertEquals(false.asJSValue(), test2)
    }

    @Test
    fun testExposeMethod() {
        engine.expose(JSValue.JSFunction { obj, params ->
            params.getInteger(0) + params.getInteger(1)
        }, "add")

        val ret = engine.call("add", listOf(10.asJSValue(), 20.asJSValue()))
        assertEquals(30.asJSValue(), ret)
    }

    private fun <T: Any> runOnJSThread(block: () -> T): T {
        return engine.jsExecutor.submit(block).get()
    }
}