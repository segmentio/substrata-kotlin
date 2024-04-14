package com.segment.analytics.substrata.kotlin.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("PRAY", "Starting")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val engine = J2V8Engine()
//        Log.d("PRAY", "Created engine")
//        engine["foo"] = JSValue.JSString("")
//        val script1 = applicationContext.assets.open("sample.js")
//        val script2 = applicationContext.assets.open("sample2.js").bufferedReader().readText()
//        engine.bridge["foo"] = JSValue.JSString("bar")
//        engine.loadBundle(script1) { }
//        val res = engine.execute(
//            """
//            function barFoo() {
//                return "Foo";
//            }
//            const  foo = {x: "Test"};
//            const  bar = [10, 2, "foo", true];
//            console.log("123");
//            console.log(1);
//            console.log(2.9);
//            console.log(true);
//            console.log(foo);
//            console.log(bar);
//            console.log(DataBridge["foo"]);
//            foo.y = 2.8;
//            foo
//
//        """.trimIndent()
//        )
//
//        Log.d("PRAY", res.toString())
//        val res2 = engine.call("fooBar")
//        Log.d("PRAY", res2.toString())
//        val res3 = engine.call("barFoo")
//        Log.d("PRAY", res3.toString())
//        engine.execute(script2)
////        engine.exec(
////            """
////            pray
////        """.trimIndent() // ReferenceError
////        )
////        engine.exec(
////            """
////            pray()
////        """.trimIndent() // ReferenceError
////        )
////        engine.exec(
////            """
////            throw "pray-error"
////        """.trimIndent() // String "pray-error"
////        )
////        engine.exec(
////            """
////            throw 'Parameter is not a number!';
////        """.trimIndent() // String "pray-error"
////        )
////        engine.exec(
////            """
////            function
////        """.trimIndent() // SyntaxError
////        )
////        engine.exec(
////            """
////            var x = ""
////            var x = 2
////        """.trimIndent()
////        )
////        engine.exec(
////            """
////            const y = ""
////            const y = 2
////        """.trimIndent() // SyntaxError
////        )
////        try {
////            engine.call("playa")
////        } catch (ex: Exception) {
////            ex.printStackTrace()
////            Log.d("PRAY", ex.toString())
////        }
    }
}