package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.*
import org.junit.After
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

    @After
    fun tearDown() {
        scope.release()
    }

    @Test
    fun testInit() {
        scope.sync(exceptionHandler) { engine ->
            val console = engine["console"]
            assertNotNull(console)
            val dataBridge = engine["DataBridge"]
            assertNotNull(dataBridge)
        }
        assertNull(exception)
    }

    @Test
    fun testConsole() {
        val script = """
            console.log("testing log")
            console.err("testing err")
        """.trimIndent()
        scope.sync(exceptionHandler) {
            it.evaluate(script)
            assertFalse(false)
        }
        assertNull(exception)
    }

    @Test
    fun testDataBridge() {
        scope.sync(exceptionHandler) { engine ->
            engine.bridge["string"] = "123"
            engine.bridge["int"] = 123
            engine.bridge["bool"] = false

            val bridge = engine.getJSObject("DataBridge")
            assertEquals("123", bridge["string"])
            assertEquals(123, bridge["int"])
            assertEquals(false, bridge["bool"])

            assertEquals("123", engine.bridge.getString("string"))
            assertEquals(123, engine.bridge.getInt("int"))
            assertEquals(false, engine.bridge.getBoolean("bool"))
        }
        assertNull(exception)
    }

    @Test
    fun testDataBridgeCrossScopes() {
        scope.sync(exceptionHandler) { engine ->
            engine.bridge["string"] = "123"
            engine.bridge["int"] = 123
            engine.bridge["bool"] = false
        }
        scope.sync(exceptionHandler) { engine ->
            val bridge = engine.getJSObject("DataBridge")
            assertEquals("123", bridge["string"])
            assertEquals(123, bridge["int"])
            assertEquals(false, bridge["bool"])

            assertEquals("123", engine.bridge.getString("string"))
            assertEquals(123, engine.bridge.getInt("int"))
            assertEquals(false, engine.bridge.getBoolean("bool"))
        }
        assertNull(exception)
    }

    @Test
    fun testLoadBundle() {
        val script = """
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
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync(exceptionHandler) { engine ->
            engine.loadBundle(script.byteInputStream())
            assertEquals("Ready to setup", engine["DataBridge.foo"])
            assertEquals("Ready to setup", engine["foo"])
            assertEquals(engine.JSUndefined, engine["bar"])
        }
        assertNull(exception)
    }

    @Test
    fun testSet() {
        val script = """
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync(exceptionHandler) {
            it.loadBundle(script.byteInputStream())
            it["foo"] = "Modified"
            assertEquals("Ready to setup", it["DataBridge.foo"])
            assertEquals("Modified", it["foo"])
        }
        assertNull(exception)
    }

    @Test
    fun testCall() {
        val script = """
            function add(x, y) {
                return x+y;
            }
        """.trimIndent()
        scope.sync(exceptionHandler) { engine ->
            engine.loadBundle(script.byteInputStream())
            val retVal = engine.call("add", 10, 20) as Int
            assertEquals(30, retVal)
        }
        assertNull(exception)
    }

    @Test
    fun testCallOnJSObject() {
        val script = """
            class Calculator {
                add(x, y) {
                    return x + y
                }
            }

            var calc = new Calculator();
        """.trimIndent()
        scope.sync(exceptionHandler) { engine ->
            engine.loadBundle(script.byteInputStream())
            val calc = engine.getJSObject("calc")
            val retVal = engine.call(calc, "add", 10, 20)
            assertEquals(30, retVal)
        }
        assertNull(exception)
    }

    @Test
    fun testCallOnObjectName() {
        val script = """
            class Calculator {
                add(x, y) {
                    return x + y
                }
            }

            var calc = new Calculator();
        """.trimIndent()
        scope.sync(exceptionHandler) { engine ->
            engine.loadBundle(script.byteInputStream())
            val retVal = engine.call("calc", "add", 10, 20)
            assertEquals(30, retVal)
        }
        assertNull(exception)
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
        scope.sync(exceptionHandler) {engine ->
            engine.export( "Bucket", Bucket::class)
            engine.export( bucket1, "Bucket", "bucketMain")
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
    fun testObject() {
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
        scope.sync(exceptionHandler) {engine ->
            engine.export( "Bucket", Bucket::class)
            engine.export( bucket1, "Bucket", "bucketMain")

            bucket1.empty = false
            val test3 = engine.evaluate("bucketMain.empty")
            assertEquals(false, test3)
            val test4 = engine.evaluate("bucketMain.isEmpty()")
            assertEquals(false, test4)
        }
        assertNull(exception)
    }

    @Test
    fun testConstructor() {
        class Bucket() {
            var empty: Boolean = false

            constructor(empty: Boolean) : this() {
                this.empty = empty
            }
        }
        val bucket1 = Bucket(true)
        scope.sync(exceptionHandler) {engine ->
            engine.export( "Bucket", Bucket::class)

            engine.evaluate("let bucket = new Bucket(true); bucket")
            engine.export( bucket1, "Bucket", "bucketMain")
            engine.evaluate("""var bucketTmp = new Bucket(true);""")

            val test0 = engine.evaluate("bucketMain.empty && bucketTmp.empty")
            assertEquals(true, test0)
            val test1 = engine.evaluate("bucketMain.empty = false; bucketMain.empty;")
            assertEquals(false, test1)
            val test2 = engine.evaluate("bucketTmp.empty = false; bucketTmp.empty;")
            assertEquals(false, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testGetterSetter() {
        class Bucket {
            var empty: Boolean = true
        }
        val bucket1 = Bucket()
        scope.sync(exceptionHandler) {engine ->
            engine.export( "Bucket", Bucket::class)

            engine.evaluate("let bucket = new Bucket(); bucket")
            engine.export( bucket1, "Bucket", "bucketMain")
            engine.evaluate("""var bucketTmp = new Bucket();""")

            val test0 = engine.evaluate("bucketMain.empty && bucketTmp.empty")
            assertEquals(true, test0)
            val test1 = engine.evaluate("bucketMain.empty = false; bucketMain.empty;")
            assertEquals(false, test1)
            val test2 = engine.evaluate("bucketTmp.empty = false; bucketTmp.empty;")
            assertEquals(false, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testExposeStaticClass() {
        scope.sync(exceptionHandler) {engine ->
            engine.export( "StaticBucket", StaticBucket::class)

            val test0 = engine.evaluate("StaticBucket.isEmpty()")
            assertEquals(true, test0)
            val test1 = engine.evaluate("StaticBucket.fill(); StaticBucket.isEmpty();")
            assertEquals(false, test1)
            val test2 = engine.evaluate("StaticBucket.empty = true; StaticBucket.empty;")
            assertEquals(true, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testExtendClass() {
        class MyJSClass {
            fun test(p: Int): Int {
                return p
            }
        }
        scope.sync(exceptionHandler) {engine ->
            engine.export( "MyJSClass", MyJSClass::class)
            engine.evaluate("""
        class OtherClass extends MyJSClass {
          constructor() {
            super()
            console.log("OtherClass created")
          }
        
          test(p) {
            console.log("OtherClass was muthatrukin called!!!!!!!")
            let r = super.test(p)
            console.log("super was just called.")
            return r + 1
          }
        }
        """)

            val test0 = engine.evaluate("let a = new MyJSClass(); a.test(1)")
            assertEquals(1, test0)
            val test1 = engine.evaluate("let b = new OtherClass(); b.test(1)")
            assertEquals(2, test1)
        }
        assertNull(exception)
    }

    @Test
    fun testExportMethod() {
        scope.sync(exceptionHandler) { engine ->
            engine.export("add") { instance, params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@export (x + y)
            }

            var ret = engine.call("add", 10, 20) as Int
            assertEquals(30, ret)

            ret = engine.evaluate("""
                var result = add(10, 20)
                result
            """.trimIndent()) as Int
            assertEquals(30, ret)
        }
        assertNull(exception)
    }

    @Test
    fun testExtendMethod() {
        scope.sync(exceptionHandler) { engine ->
            // extend non-exist variable
            engine.extend("calculator", "add") { instance, params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@extend (x + y)
            }

            var ret = engine.call("calculator", "add", 10, 20) as Int
            assertEquals(30, ret)

            // now calculator exists. extend when variable exists
            engine.extend("calculator", "minus") { instance, params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@extend (x - y)
            }

            ret = engine.call("calculator", "minus", 20, 10) as Int
            assertEquals(10, ret)


            ret = engine.evaluate("""
                var ret = calculator.minus(20, 10)
                ret
            """.trimIndent()) as Int
            assertEquals(10, ret)
        }
        assertNull(exception)
    }

    class StaticBucket {
        companion object {
            var empty: Boolean = true

            fun fill() {
                empty = false
            }

            fun isEmpty(): Boolean {
                return empty
            }
        }
    }
}