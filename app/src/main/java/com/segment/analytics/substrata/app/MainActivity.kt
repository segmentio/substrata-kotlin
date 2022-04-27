package com.segment.analytics.substrata.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.segment.analytics.substrata.kotlin.JSValue
import com.segment.analytics.substrata.kotlin.R
import com.segment.analytics.substrata.kotlin.j2v8.J2V8Engine

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("PRAY", "Starting")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val engine = J2V8Engine()
        Log.d("PRAY", "Created engine")
        engine.bridge["foo"] = JSValue.JSString("bar")
        engine.loadBundle { }
        val res = engine.execute("""
            function barFoo() {
                return "Foo";
            }
            const  foo = {x: "Test"};
            console.log("123");
            console.log(1);
            console.log(2.9);
            console.log(true);
            console.log(foo);
            console.log(DataBridge["foo"]);
            foo
        """.trimIndent())
        Log.d("PRAY", res.toString())
        val res2 = engine.call("fooBar")
        Log.d("PRAY", res2.toString())
        val res3 = engine.call("barFoo")
        Log.d("PRAY", res3.toString())
    }
}