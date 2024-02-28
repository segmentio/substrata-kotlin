package com.segment.analytics.substrata.kotlin

class JSContext(
    val ref: Long
) {
    val referenceHandlers = mutableSetOf<ReferenceHandler>()

    fun addReferenceHandler(handler: ReferenceHandler) {
        referenceHandlers.add(handler)
    }

    fun removeReferenceHandler(handler: ReferenceHandler) {
        referenceHandlers.remove(handler)
    }

    fun notifyReferenceCreated(reference: JSValue) {
        for (handler in referenceHandlers) {
            handler.onCreated(reference)
        }
    }

    fun notifyReferenceReleased(reference: JSValue) {
        for (handler in referenceHandlers) {
            handler.onReleased(reference)
        }
    }
    internal inline fun <reified T> unwrap(jsValue: JSValue) : T {
        val result = when(T::class) {
            String::class -> QuickJS.getString(jsValue.ref)
            Boolean::class -> QuickJS.getBool(jsValue.ref)
            Int::class -> QuickJS.getInt(jsValue.ref)
            Double::class -> QuickJS.getFloat64(jsValue.ref)
            JSArray::class -> getValueAsJSArray(jsValue)
            JSObject::class -> getValueAsJSObject(jsValue)
            else -> null
        }
        return result as T
    }

    internal inline fun <reified T> isTypeOf(jsValue: JSValue) = when(T::class) {
        String::class -> QuickJS.isString(jsValue.ref)
        Boolean::class -> QuickJS.isBool(jsValue.ref)
        Int::class -> QuickJS.isNumber(jsValue.ref)
        Double::class -> QuickJS.isNumber(jsValue.ref)
        JSArray::class -> QuickJS.isArray(jsValue.ref)
        JSObject::class -> QuickJS.isObject(jsValue.ref)
        else -> false
    }

    inline fun <reified T> getProperty(jsValue: JSValue, name: String): T {
        val propertyRef = QuickJS.getProperty(this.ref, jsValue.ref, name)
        return get(this, propertyRef)
    }

    inline fun <reified T> getProperty(jsValue: JSValue, index: Int): T {
        val propertyRef = QuickJS.getProperty(this.ref, jsValue.ref, index)
        return get(this, propertyRef)
    }

    fun getProperties(jsValue: JSValue): MutableMap<String, Any> {
        val names = QuickJS.getOwnPropertyNames(this.ref, jsValue.ref)
        val result = mutableMapOf<String, Any>()
        for (name in names) {
            val value: Any = getProperty(jsValue, name)
            result[name] = value
        }
        return result
    }

    fun setProperty(obj: JSValue, index: Int, value: JSValue) {
        QuickJS.setProperty(ref, obj.ref, index, value.ref)
    }

    fun setProperty(obj: JSValue, name: String, value: JSValue) {
        QuickJS.setProperty(ref, obj.ref, name, value.ref)
    }

    private fun getValueAsJSArray(jsValue: JSValue): JSArray {
        val size: Int = getProperty(jsValue, "length")
        val result = MutableList(size) { i ->
            val value: Any = getProperty(jsValue, i)
            value
        }

        return JSArray(result)
    }

    private fun getValueAsJSObject(jsValue: JSValue) = JSObject(getProperties(jsValue))

    inline fun <reified T> newJSValue(value: T): JSValue {
        val valueRef = when(value) {
            is String -> QuickJS.newString(ref, value)
            is Boolean -> QuickJS.newBool(ref, value)
            is Int -> QuickJS.newInt(ref, value)
            is Double -> QuickJS.newFloat64(ref,value)
            is JSArray -> QuickJS.newArray(ref)
            is JSObject -> QuickJS.newObject(ref)
            is JSNull -> QuickJS.getNull(ref)
            else -> QuickJS.newObject(ref)
        }
        return JSValue(valueRef, this)
    }
}