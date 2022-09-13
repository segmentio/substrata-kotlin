# Substrata-Kotlin
Small, Efficient, Easy.  Javascript Engine for Kotlin.

Using J2V8 has been simplified.  No more
messing with JSValues, type conversion and messy call sites to use Javascript.

### Quick Start

Values are converted automatically to the appropriate types
as needed.  Calls into JavascriptCore are all synchronized on
the same serial dispatch queue.

```kotlin
// get a shared engine instance
val engine: JavascriptEngine = J2V8Engine.shared

// set the error handler we want to use
engine.errorHandler = { error ->
    print("javascript error: $error")
}

// execute some js
val result = engine.execute ("1 + 2;")
if (result == 3.asJSValue()) {
    // success!
}
```

### Loading a Javascript bundle from disk

Load a bundle from disk.  Only accepts input streams.  Any downloading of javascript
bundles must be done upstream by the caller.  A completion block will be executed
when done (if specified).

 ```kotlin
 engine.loadBundle(bundleStream) { error ->
    if (error != null) {
        print("oh noes, we failed: $error")
    } else {
        success = true
    }
}
 ```

## License

MIT License

Copyright (c) 2022 Segment

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

