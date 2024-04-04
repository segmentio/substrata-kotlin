package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.*
import kotlinx.serialization.json.JsonObject
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EngineTests {

    private var exception : Exception? = null

    private val scope = JSScope {
        exception = it
    }

    @After
    fun tearDown() {
        scope.release()
    }

    @Test
    fun testInit() {
        scope.sync { 
            val console = this["console"]
            assertNotNull(console)
            val dataBridge = this["DataBridge"]
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
        scope.sync {
            evaluate(script)
            assertFalse(false)
        }
        assertNull(exception)
    }

    @Test
    fun testDataBridge() {
        scope.sync { 
            bridge["string"] = "123"
            bridge["int"] = 123
            bridge["bool"] = false

            val bridge = getJSObject("DataBridge")
            assertEquals("123", bridge["string"])
            assertEquals(123, bridge["int"])
            assertEquals(false, bridge["bool"])

            assertEquals("123", bridge.getString("string"))
            assertEquals(123, bridge.getInt("int"))
            assertEquals(false, bridge.getBoolean("bool"))
        }
        assertNull(exception)
    }

    @Test
    fun testDataBridgeCrossScopes() {
        scope.sync {
            bridge["string"] = "123"
            bridge["int"] = 123
            bridge["bool"] = false
        }
        scope.sync {
            val bridge = getJSObject("DataBridge")
            assertEquals("123", bridge["string"])
            assertEquals(123, bridge["int"])
            assertEquals(false, bridge["bool"])

            assertEquals("123", bridge.getString("string"))
            assertEquals(123, bridge.getInt("int"))
            assertEquals(false, bridge.getBoolean("bool"))
        }
        assertNull(exception)
    }

    @Test
    fun testLoadBundle() {
        val script = """
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync {
            loadBundle(script.byteInputStream()) {
                exception = it
            }
            assertEquals("Ready to setup", bridge.getString("foo"))
        }
        assertNull(exception)
    }

    @Test
    fun testGet() {
        val script = """
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync {
            loadBundle(script.byteInputStream())
            assertEquals("Ready to setup", this["DataBridge.foo"])
            assertEquals("Ready to setup", this["foo"])
            assertEquals(JSUndefined, get("bar"))
        }
        assertNull(exception)
    }

    @Test
    fun testSet() {
        val script = """
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync {
            loadBundle(script.byteInputStream())
            this["foo"] = "Modified"
            assertEquals("Ready to setup", this["DataBridge.foo"])
            assertEquals("Modified", this["foo"])
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
        scope.sync {
            loadBundle(script.byteInputStream())
            val retVal = call("add", 10, 20) as Int
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
        scope.sync {
            loadBundle(script.byteInputStream())
            val calc = getJSObject("calc")
            val retVal = call(calc, "add", 10, 20)
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
        scope.sync {
            loadBundle(script.byteInputStream())
            val retVal = call("calc", "add", 10, 20)
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
        scope.sync {
            export( "Bucket", Bucket::class)
            export( bucket1, "Bucket", "bucketMain")
            evaluate("""var bucketTmp = new Bucket();""")

            val test0 = evaluate("bucketMain.isEmpty() && bucketTmp.isEmpty()")
            assertEquals(true, test0)
            val test1 = evaluate("bucketMain.fill(); bucketMain.isEmpty();")
            assertEquals(false, test1)
            val test2 = evaluate("bucketTmp.fill(); bucketTmp.isEmpty();")
            assertEquals(false, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testClass() {
        class Bucket {
            var empty: Boolean = true

            fun fill() {
                empty = false
            }

            fun isEmpty(): Boolean {
                return empty
            }
        }
        scope.sync {
            export( "Bucket", Bucket::class)
            evaluate("""var bucketTmp = new Bucket();""")

            val test0 = evaluate("bucketTmp.isEmpty()")
            assertEquals(true, test0)
            val test2 = evaluate("bucketTmp.fill(); bucketTmp.isEmpty();")
            assertEquals(false, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testClassWithComplexProperty() {
        class JSAnalytics {
            var traits: JsonObject? = null
        }
        scope.sync {
            export( "Analytics", JSAnalytics::class)
            evaluate("""var analytics = new Analytics();""")

            val test0 = evaluate("analytics.traits")
            assertEquals(null, test0)
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
        scope.sync {
            export( "Bucket", Bucket::class)
            export( bucket1, "Bucket", "bucketMain")

            bucket1.empty = false
            val test3 = evaluate("bucketMain.empty")
            assertEquals(false, test3)
            val test4 = evaluate("bucketMain.isEmpty()")
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
        scope.sync {
            export( "Bucket", Bucket::class)

            evaluate("let bucket = new Bucket(true); bucket")
            export( bucket1, "Bucket", "bucketMain")
            evaluate("""var bucketTmp = new Bucket(true);""")

            val test0 = evaluate("bucketMain.empty && bucketTmp.empty")
            assertEquals(true, test0)
            val test1 = evaluate("bucketMain.empty = false; bucketMain.empty;")
            assertEquals(false, test1)
            val test2 = evaluate("bucketTmp.empty = false; bucketTmp.empty;")
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
        scope.sync {
            export( "Bucket", Bucket::class)

            evaluate("let bucket = new Bucket(); bucket")
            export( bucket1, "Bucket", "bucketMain")
            evaluate("""var bucketTmp = new Bucket();""")

            val test0 = evaluate("bucketMain.empty && bucketTmp.empty")
            assertEquals(true, test0)
            val test1 = evaluate("bucketMain.empty = false; bucketMain.empty;")
            assertEquals(false, test1)
            val test2 = evaluate("bucketTmp.empty = false; bucketTmp.empty;")
            assertEquals(false, test2)
        }
        assertNull(exception)
    }

    @Test
    fun testExposeStaticClass() {
        scope.sync {
            export( "StaticBucket", StaticBucket::class)

            val test0 = evaluate("StaticBucket.isEmpty()")
            assertEquals(true, test0)
            val test1 = evaluate("StaticBucket.fill(); StaticBucket.isEmpty();")
            assertEquals(false, test1)
            val test2 = evaluate("StaticBucket.empty = true; StaticBucket.empty;")
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
        scope.sync {
            export( "MyJSClass", MyJSClass::class)
            evaluate("""
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

            val test0 = evaluate("let a = new MyJSClass(); a.test(1)")
            assertEquals(1, test0)
            val test1 = evaluate("let b = new OtherClass(); b.test(1)")
            assertEquals(2, test1)
        }
        assertNull(exception)
    }

    @Test
    fun testExportMethod() {
        scope.sync {
            export("add") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@export (x + y)
            }

            var ret = call("add", 10, 20) as Int
            assertEquals(30, ret)

            ret = evaluate("""
                var result = add(10, 20)
                result
            """.trimIndent()) as Int
            assertEquals(30, ret)
        }
        assertNull(exception)
    }

    @Test
    fun testExtendMethod() {
        scope.sync {
            // extend non-exist variable
            extend("calculator", "add") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@extend (x + y)
            }

            var ret = call("calculator", "add", 10, 20) as Int
            assertEquals(30, ret)

            // now calculator exists. extend when variable exists
            extend("calculator", "minus") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@extend (x - y)
            }

            ret = call("calculator", "minus", 20, 10) as Int
            assertEquals(10, ret)


            ret = evaluate("""
                var ret = calculator.minus(20, 10)
                ret
            """.trimIndent()) as Int
            assertEquals(10, ret)
        }
        assertNull(exception)
    }

    @Test
    fun testAwait() {
        val ret = scope.await {
            export("add") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@export (x + y)
            }

            call("add", 10, 20) as Int
        }
        assertEquals(30, ret)
        assertNull(exception)

    }

    @Test
    fun testLaunch() {
        scope.launch {
            export("add") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@export (x + y)
            }

            call("add", 10, 20) as Int

        }
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