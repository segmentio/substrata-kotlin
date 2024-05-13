# Substrata-Kotlin
Small, Efficient, Easy.  Javascript Engine for Kotlin.

Using QuickJS has been simplified.  No more worrying on memory management, thread safety and type conversions. This engine handles all of that for you.

Substrata Kotlin is currently only available in Beta.

- [Substrata-Kotlin](#substrata-kotlin)
  - [Quick Start](#quick-start)
  - [Supported Types](#supported-types)
    - [Primitives (Pass-by-value)](#primitives-pass-by-value)
    - [JSConvertibles (Pass-by-value)](#jsconvertibles-pass-by-value)
    - [Kotlin JsonElement](#kotlin-jsonelement)
  - [JSScope](#jsscope)
    - [Launch, Sync and Await](#launch-sync-and-await)
  - [Share values between native and JavaScript](#share-values-between-native-and-javascript)
    - [DataBridge](#databridge)
  - [Memory and thread management](#memory-and-thread-management)
    - [Memory management](#memory-management)
    - [Thread management](#thread-management)
    - [Nested tasks](#nested-tasks)
  - [More Usages](#more-usages)
    - [Loading a Javascript bundle from disk](#loading-a-javascript-bundle-from-disk)
    - [Evaluate a script](#evaluate-a-script)
    - [Get/Set things as global variables](#getset-things-as-global-variables)
    - [Export functions to JavaScript](#export-functions-to-javascript)
    - [Export classes and objects to JavaScript](#export-classes-and-objects-to-javascript)
    - [Extend methods on object](#extend-methods-on-object)
    - [More](#more)
  - [License](#license)


## Quick Start

Values are converted automatically to the appropriate types  as needed.  Calls into the engine are all synchronized on the same serial dispatch queue.

```kotlin
// create a scope and set error handler
val scope = JSScope { error ->
    print("javascript error: $error")
    
}

// execute some js
val result = scope.await {
    return@await evaluate("1 + 2;")
}

// value is already unboxed!
if (result == 3) {
    // success!
}
```
## Supported Types

### Primitives (Pass-by-value)

The engine supports the following primitive types natively. It's very similar to the `Pass-by-value`. Primitives passed to/receive from the engine are values that does not hold a reference, thus is free to use out of the js scope. The engine also boxes/unboxes the value automatically.

* int
* boolean
* double
* string

```kotlin
// use the return value directly as Int
val ret: Int? = scope.await {
    export("add") { params ->
        val x = params[0] as Int
        val y = params[1] as Int
        return@export (x + y)
    }

    call("add", 10, 20)
}
```

### JSConvertibles (Pass-by-value)

`JSConvertible` can be any of the following types. This is very similar to `Pass-by-value` that an object/array passed to/receive from the engine is in a form of `JSValue`, which represents the reference to the holder of the original object/array. You'd have to write a converter to convert it back to its original form

* JSArray
* JSObject
* JSFunction
* JSException

### Kotlin JsonElement

The library has implemented a converter for Kotlin's JsonElement. You can add JsonElement directly to a `JSObject` or `JSArray` as following:

```kotlin
scope.sync {
    val obj = context.newObject()
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
}
```

The following example shows how to pass/receive JsonElement to/receive the engine
```kotlin
        val json = "some json string"
        val content = Json.parseToJsonElement(json)

        scope.sync {
            // convert JsonElement to JSObject
            val jsObject = JsonElementConverter.write(content, this)
            // convert JSObject to JsonElement
            val jsonObject = JsonElementConverter.read(jsObject)
        }
```

## JSScope

`JSScope` is a safe wrapper on the actual `JSEngine` that manages memory and threads automatically. Within the scope, it provides an instance of `JSEngine` which in turn provides a `JSContext` and `JSRuntime`. This mechanism ensures the tasks always executed on the right context and runtime.

### Launch, Sync and Await

`JSScope` provides 3 ways to run tasks depending on the use case:
* `launch`: used if a task should be executed in a fire and forgot manner
* `sync`: run the task in a blocking way
* `await`: runt the task in a blocking way and return the result of the executed tasks

```kotlin
// fire and forgot, no value returned
scope.launch {
    export("add") { params ->
        val x = params[0] as Int
        val y = params[1] as Int
        return@export (x + y)
    }
    call("add", 10, 20) as Int
}

// block the thread until completion, no value returned
scope.sync {
    export("add") { params ->
        val x = params[0] as Int
        val y = params[1] as Int
        return@export (x + y)
    }

    call("add", 10, 20) as Int
}

// block the thread until completion, value returned (30 is returned in this example)
val result = scope.await {
    export("add") { params ->
        val x = params[0] as Int
        val y = params[1] as Int
        return@export (x + y)
    }

    call("add", 10, 20) as Int
}
```

## Share values between native and JavaScript

### DataBridge

You can share value between native and JavaScript using `DataBridge`. The example below shows how to set a property on native and use it on JavaScript. It works the same verse-vice.

```kotlin
scope.sync {
    // set a property on native side
    bridge["string"] = "123"
    bridge["int"] = 123
    bridge["bool"] = false

    // use it in JavaScript
    val ret = evaluate(
        """
          let v = DataBridge["int"]
        """.trimIndent()
    )
    assertEquals(123, ret)
}
```


## Memory and thread management

### Memory management

The `JSScope` handles memory allocation and deallocation automatically for anything executed in the scope. It also serializes the execution of tasks passing into it.

```kotlin
var ret: JSObject? = scope.await {
    val jsObject = context.newObject()
    jsObject["a"] = 1
    jsObject
}

// ret is deallocated because it's a JSValue and the scope is ended.
```

In the example above, `ret` is no longer accessible even if the scope returned a value to it, because it is deallocated when the task is ended. To be able to persist a `JSValue` out of scope, the task has to be marked explicitly as `global`


```kotlin
// set global to true
var ret: JSObject? = scope.await(global = true) {
    val jsObject = context.newObject()
    jsObject["a"] = 1
    jsObject
}

// ret is persist and can be accessed out of the scope
```

Even though `JSValue` can be made accessible out of scope, it's highly recommended not to abuse it unless necessary to avoid out of memory exceptions. Instead, convert the `JSValue` to the type you want and return the converted value is the preferable way:

```kotlin
val primitive = scope.await {
    // no conversion needed for primitive types
    return@await evaluate("1 + 2;")
}

val other = scope.await {
    val ret = evaluate("script that returns a JSObject")
    // convert JSObject to kotlin JsonObject
    val jsonObject = JsonElementConverter.read(ret)
    return@await jsonObject
}
// `other` is still accessible because it's not a JSValue, thus won't be released
```

### Thread management

The scope has an internal single thread to serialize task executions. To avoid a task blocking its successors, set the `timeoutInSeconds` to the appropriate value. By default, a task times out in 120s.

```kotlin
// set the timeout to be 20s
val scope = JSScope(timeoutInSeconds = 20) { error ->
    print("javascript error: $error")
}

// schedule task1 to run in background
scope.launch {
    // task 1
}

// dispatch task2 to run and wait for its completion
scope.sync {
    // task2
}

// task2 won't start unless task1 completes
```

In the above example, task2 awaits until task1 completed because of the scope only has a single thread. Even though task1 is scheduled to run in background, it occupies the thread thus preventing task2 to start.

### Nested tasks

The scope also takes care of nested tasks to prevent deadlock. If a nested task is detected, it is lifted up and runs in sequential after it's parent task. However, the parent and nested tasks maintain their own memory scope, that is, variables created in the nested task will be released when the task finished.

```kotlin
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
```

The above example is equivalent to the following, except that any `JSValue` created in each of the task is released on each task completion:

```kotlin
scope.sync {
    val l1
    
    val l2
    Thread.sleep(500L)
    l2 = 1

    l1 = (l2 ?: 0) + 1
    
    assertEquals(2, l1)
}
```

## More Usages

### Loading a Javascript bundle from disk

Load a bundle from disk.  Only accepts input streams.  Any downloading of javascript
bundles must be done upstream by the caller.  A completion block will be executed
when done (if specified).

 ```kotlin
scope.sync {
    loadBundle(bundleStream) { error ->
        if (error != null) {
            print("oh noes, we failed: $error")
        } else {
            success = true
        }
    }
}
 ```

### Evaluate a script

```kotlin
scope.sync {
    evaluate("""var bucketTmp = new Bucket();""")
}
```

### Get/Set things as global variables

```kotlin
        val script = """
            var foo = "Ready to setup";
            DataBridge["foo"] = foo;
        """.trimIndent()
        scope.sync {
            loadBundle(script.byteInputStream())
            // read foo from DataBridge
            assertEquals("Ready to setup", this["DataBridge.foo"])
            // read foo from global
            assertEquals("Ready to setup", this["foo"])

            // update global variable `foo`
            this["foo"] = "Modified"
            // read the updated value
            assertEquals("Modified", this["foo"])
        }
```

### Export functions to JavaScript

```kotlin
        scope.sync {
            export("add") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@export (x + y)
            }

            // use it in native
            var ret = call("add", 10, 20) as Int
            // use it in JavaScript
            ret = evaluate(
                """
                  add(10, 20)
                """.trimIndent()
            )
        }
```

### Export classes and objects to JavaScript

```kotlin
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
            export("Bucket", Bucket::class)
            export(bucket1, "Bucket", "bucketMain")

            // use them in java script
            evaluate("""var bucketTmp = new Bucket();""")
            val test0 = evaluate("bucketMain.isEmpty() && bucketTmp.isEmpty()")
            assertEquals(true, test0)
            val test1 = evaluate("bucketMain.fill(); bucketMain.isEmpty();")
            assertEquals(false, test1)
            val test2 = evaluate("bucketTmp.fill(); bucketTmp.isEmpty();")
            assertEquals(false, test2)
        }
```

### Extend methods on object

```kotlin
        scope.sync {
            // extend non-exist variable
            extend("calculator", "add") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@extend (x + y)
            }

            var ret = call("calculator", "add", 10, 20) as Int

            // now calculator exists. extend when variable exists
            extend("calculator", "minus") { params ->
                val x = params[0] as Int
                val y = params[1] as Int
                return@extend (x - y)
            }

            // use it in native with variable name
            ret = call("calculator", "minus", 20, 10) as Int
            // use it in native with JSObject
            val calculator = this["calculator"]
            ret = call(calculator, "minus", 20, 10) as Int

            // use it in JavaScript
            ret = evaluate("""
                var ret = calculator.minus(20, 10)
                ret
            """.trimIndent()) as Int
        }
```

### More

You can find more example usages in our unit tests [here](https://github.com/segmentio/substrata-kotlin/tree/main/substrata-kotlin/src/androidTest/java/com/segment/analytics/substrata/kotlin)

## License
```
MIT License

Copyright (c) 2022 Twilio Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
