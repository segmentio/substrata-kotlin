package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eclipsesource.v8.V8
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EngineTests {

    private val scope = JSScope()

    private val exceptionHandler = object : JSExceptionHandler {
        override fun onError(e: Exception) {
            exception = e
        }
    }

    private var exception : Exception? = null

    @Test
    fun testInit() {
        scope.sync(exceptionHandler) { engine ->
            val console = engine.runtime.get("console")
            assertNotNull(console)

            val dataBridge = engine.runtime.get("DataBridge")
            assertNotNull(dataBridge)
        }
        assertNull(exception)
    }

    @Test
    fun testDataBridge() {
        scope.sync(exceptionHandler) { engine ->
            engine.bridge["string"] = "123"
            engine.bridge["int"] = 123
            engine.bridge["bool"] = false

            val bridge = engine.runtime.getObject("DataBridge")
            assertEquals("123", bridge.get("string"))
            assertEquals(123, bridge.get("int"))
            assertEquals(false, bridge.get("bool"))

            assertEquals("123", engine.bridge.getString("string"))
            assertEquals(123, engine.bridge.getInt("int"))
            assertEquals(false, engine.bridge.getBoolean("bool"))
        }
        assertNull(exception)
    }

    @Test
    fun testLoadBundle() {
        val script = """
            console.log("Starting");
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync(exceptionHandler) {
            it.loadBundle(script.byteInputStream())
            assertEquals("Ready to setup", it.bridge.getString("foo"))
        }
        assertNull(exception)
    }

    @Test
    fun testGet() {
        val script = """
            console.log("Starting");
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync(exceptionHandler) { engine ->
            engine.loadBundle(script.byteInputStream())
            assertEquals("Ready to setup", engine["DataBridge.foo"].content)
            assertEquals("Ready to setup", engine["foo"].content)
            assertEquals(V8.getUndefined(), engine["bar"].content)
        }
        assertNull(exception)
    }

    @Test
    fun testSet() {
        val script = """
            console.log("Starting");
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync {
            it.loadBundle(script.byteInputStream())
            it["foo"] = "Modified"
            assertEquals("Ready to setup", it["DataBridge.foo"].content)
            assertEquals("Modified", it["foo"].content)
        }
        assertNull(exception)
    }

    @Test
    fun testExtend() {
        var data = "Something"
        scope.sync(exceptionHandler) { engine ->
            engine.extend("foobar", "method1", JSFunction(engine) { obj, params ->
                data = "Something new"
                null
            })
            engine.evaluate("foobar.method1()")
        }
        assertEquals("Something new", data)
        assertNull(exception)
    }

    @Test
    fun testExtendObject() {
        val script = """
            var x = {};
        """.trimIndent()
        var data = "Something"
        scope.sync { engine ->
            engine.loadBundle(script.byteInputStream())
            engine.extend("x", "method1", JSFunction(engine) { obj, params ->
                data = "Something new"
                null
            })
            engine.evaluate("x.method1()")
        }
        assertEquals("Something new", data)
        assertNull(exception)
    }

    @Test
    fun testCall() {
        val script = """
            function add(x, y) {
                return x+y;
            }
        """.trimIndent()
        var data = "Something"
        scope.sync { engine ->
            engine.loadBundle(script.byteInputStream())
            val retVal = engine.call("add", JSArray.create(engine) {
                it.add(10)
                it.add(20)
            })
            assertEquals(30, retVal.content)
            engine.call(JSFunction(engine) { obj, params ->
                data = "Modified"
                null
            })
        }
        assertEquals("Modified", data)
        assertNull(exception)
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
        scope.sync { engine ->
            engine.loadBundle(script.byteInputStream())
            val calc = JSObject(engine, engine.runtime.getObject("calc"))
            val retVal = engine.call(calc, "add", JSArray.create(engine) {
                it.add(10)
                it.add(20)
            })
            assertEquals(30, retVal)
        }
        assertNull(exception)
    }

    @Test
    fun testExposeObjectAndClass() {
        class Bucket : JSExport() {
            var empty: Boolean = true

            fun fill() {
                empty = false
            }

            fun isEmpty(): Boolean {
                return empty
            }
        }
        val bucket1 = Bucket()
        scope.sync {engine ->
            engine.export( "Bucket", Bucket::class)
            engine.export( "bucketMain", bucket1)
            engine.evaluate("""var bucketTmp = new Bucket();""")

            val test0 = engine.evaluate("bucketMain.isEmpty() && bucketTmp.isEmpty()")
            assertEquals(true, test0)
            val test1 = engine.evaluate("bucketMain.fill(); bucketMain.isEmpty();")
            assertEquals(false, test1)
            val test2 = engine.evaluate("bucketTmp.fill(); bucketTmp.isEmpty();")
            assertEquals(false, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testExposeMethod() {
        scope.sync { engine ->
            engine.export("add", JSFunction(engine) { obj, params ->
                params?.getInt(0)?.plus(params.getInt(1))
            })

            val ret = engine.call("add", JSArray.create(engine) {
                it.add(10)
                it.add(20)
            })
            assertEquals(30, ret)
        }
    }

}