package com.segment.analytics.substrata.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

            val ret = evaluate("""
                let v = DataBridge["int"]
                v
            """.trimIndent())
            assertEquals(123, ret)
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
    fun testConstructorWithExportedObject() {
        class Bucket {
            var empty: Boolean = false

            constructor() {}

            constructor(b: JSObject) {
                this.empty = b.getBoolean("empty")
            }
        }
        val bucket1 = Bucket()
        bucket1.empty = true
        scope.sync {
            export( "Bucket", Bucket::class)

            export( bucket1, "Bucket", "bucketMain")
            evaluate("let bucket = new Bucket(bucketMain); bucket")

            val test0 = evaluate("bucket.empty")
            assertEquals(true, test0)
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
    fun testCallWithJsonElement() {
        val message = "This came from a LivePlugin"
        val script = """
            class MyTest {
                track(event) {
                    event.context.livePluginMessage = "$message";
                    const mcvid = DataBridge["mcvid"]
                    if (mcvid) {
                        event.context.mcvid = mcvid;
                    }
                    return event
                }
            }
            let myTest = new MyTest()
            myTest
        """.trimIndent()
        val json = """
            {"properties":{"version":1,"build":1,"from_background":false},"event":"Application Opened","type":"track","messageId":"2132f014-a8fe-41b6-b714-0226db39e0d3","anonymousId":"a7bffc58-991e-4a2d-98a7-2a04abb3ea93","integrations":{},"context":{"library":{"name":"analytics-kotlin","version":"1.15.0"},"instanceId":"49f19161-6d56-4024-b23d-7f32d6ab9982","app":{"name":"analytics-kotlin-live","version":1,"namespace":"com.segment.analytics.liveplugins.app","build":1},"device":{"id":"87bc73d4e4ca1608da083975d36421aef0411dff765c9766b9bfaf266b7c1586","manufacturer":"Google","model":"sdk_gphone64_arm64","name":"emu64a","type":"android"},"os":{"name":"Android","version":14},"screen":{"density":2.75,"height":2154,"width":1080},"network":{},"locale":"en-US","userAgent":"Dalvik/2.1.0 (Linux; U; Android 14; sdk_gphone64_arm64 Build/UE1A.230829.036.A1)","timezone":"America/Chicago"},"userId":"","_metadata":{"bundled":[],"unbundled":[],"bundledIds":[]},"timestamp":"2024-04-25T16:40:55.994Z"}
        """.trimIndent()
        val content = Json.parseToJsonElement(json)

        scope.sync {
            val ret = evaluate(script)
            assert(ret is JSObject)
            val res: Any = call(ret as JSObject, "track", JsonElementConverter.write(content, context))
            assert(res is JSObject)
            val jsonObject = JsonElementConverter.read(res)
            assertNotNull(jsonObject)
            assertEquals(message, jsonObject.jsonObject["context"]?.jsonObject?.get("livePluginMessage")?.jsonPrimitive?.content)
        }
        assertNull(exception)
    }

    @Test
    fun testOverloads() {
        class MyTest {
            fun track() = 0

            fun track(str: String) = str

            fun track(i: Int, str: String) = "$i and $str"
        }

        scope.sync {
            export("MyTest", MyTest::class)
            val ret = evaluate("let myTest = new MyTest(); myTest")
            assert(ret is JSObject)
            val jsObject = ret as JSObject
            assertEquals(0, call(jsObject, "track"))
            assertEquals("testtest", call(jsObject, "track", "testtest"))
            assertEquals("0 and testtest", call(jsObject, "track", 0, "testtest"))
        }
        assertNull(exception)
    }

    @Test
    fun testNestedScopes() {
        scope.sync {
            val l1 = scope.await {
                val l2 = scope.await {
                    scope.sync {
                        Thread.sleep(500L)
                    }
                    1
                }

                (l2 ?: 0) + 1
            }

            assertEquals(2, l1)
        }
        assertNull(exception)
    }

    @Test
    fun testNestedScopesInCallback() {
        class MyTest {
            var engine: JSScope? = null

            fun track() {
                engine?.sync {
                    println("callback")
                }
            }
        }
        val myTest = MyTest()
        myTest.engine = scope

        scope.sync {
            export(myTest, "MyTest", "myTest")
            call("myTest", "track")
        }
        assertNull(exception)
    }

    @Test
    fun testGlobalScopeDoesPersist() {
        var ret: JSObject? = null
        scope.sync {
            ret = scope.await(global = true) {
                val jsObject = context.newObject()
                jsObject["a"] = 1
                jsObject
            }
        }
        assertNotNull(ret)
        assert(ret is JSObject)

        scope.sync {
            val a = (ret as JSObject)["a"]
            assertEquals(1, a)
        }

        assertNull(exception)
    }

    @Test
    fun testException() {
        class MyJSClass {
            fun test(): Int {
                throw Exception("something wrong")
            }
        }
        scope.sync {
            export( "MyJSClass", MyJSClass::class)
            evaluate("""
                let o = MyJSClass()
                o.test()
            """)
        }
        assertNotNull(exception)
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