package com.segment.analytics.substrata.kotlin

class JSContext(
    val ref: Long
): Releasable {
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

    fun executeScript(script: String): Any? {
        val ret = QuickJS.evaluate(ref, script, QuickJS.EVALUATOR, QuickJS.EVAL_TYPE_GLOBAL)
        return getAny(ret)
    }

//    fun executeFunction(function: String, params: JSArray?): Any {
//        val ret = QuickJS.call(ref, ref, obj.ref, refs)
//        return context.getAny(ret)
//    }

    fun getGlobalObject(): JSObject {
        val globalContext = QuickJS.getGlobalObject(ref)
        return get(globalContext)
    }

    fun notifyReferenceReleased(reference: JSValue) {
        for (handler in referenceHandlers) {
            handler.onReleased(reference)
        }
    }
    internal inline fun <reified T> unwrap(jsValue: JSValue) : T {
        val result = when(T::class) {
            String::class -> QuickJS.getString(jsValue.context.ref, jsValue.ref)
            Boolean::class -> QuickJS.getBool(jsValue.ref)
            Int::class -> QuickJS.getInt(jsValue.ref)
            Double::class -> QuickJS.getFloat64(jsValue.context.ref, jsValue.ref)
            JSArray::class -> getValueAsJSArray(jsValue)
            JSObject::class -> getValueAsJSObject(jsValue)
            else -> null
        }
        QuickJS.freeValue(ref, jsValue.ref)
        return result as T
    }

    internal inline fun <reified T> isTypeOf(jsValue: JSValue) = when(T::class) {
        String::class -> QuickJS.isString(jsValue.ref)
        Boolean::class -> QuickJS.isBool(jsValue.ref)
        Int::class -> QuickJS.isNumber(jsValue.ref)
        Double::class -> QuickJS.isNumber(jsValue.ref)
        JSArray::class -> QuickJS.isArray(jsValue.context.ref, jsValue.ref)
        JSObject::class -> QuickJS.isObject(jsValue.ref)
        else -> false
    }

    inline fun <reified T> getProperty(jsValue: JSValue, name: String): T {
        val propertyRef = QuickJS.getProperty(this.ref, jsValue.ref, name)
        return get(propertyRef)
    }

    inline fun <reified T> getProperty(jsValue: JSValue, index: Int): T {
        val propertyRef = QuickJS.getProperty(this.ref, jsValue.ref, index)
        return get(propertyRef)
    }

    inline fun <reified T> get(ref: Long): T {
        val value = JSValue(ref, this)
        val result = when(T::class) {
            String::class -> value.asString()
            Boolean::class -> value.asBoolean()
            Int::class -> value.asInt()
            Double::class -> value.asDouble()
            JSValue::class -> value
            else -> getAny(ref)
        }
        return result as T
    }

    fun getAny(ref: Long): Any? {
        val type = QuickJS.getType(ref)
        val value = JSValue(ref, this)
        return when (type) {
            QuickJS.TYPE_STRING -> value.asString()
            QuickJS.TYPE_BOOLEAN -> value.asBoolean()
            QuickJS.TYPE_INT -> value.asInt()
            QuickJS.TYPE_FLOAT64 -> value.asDouble()
            QuickJS.TYPE_OBJECT -> value.asJSObject()
            else -> value.asJSObject()
        }
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

    private fun getValueAsJSObject(jsValue: JSValue) = JSObject(jsValue)

    inline fun <reified T> newJSValue(value: T?): JSValue {
        val valueRef = when(value) {
            is String -> QuickJS.newString(ref, value)
            is Boolean -> QuickJS.newBool(ref, value)
            is Int -> QuickJS.newInt(ref, value)
            is Double -> QuickJS.newFloat64(ref,value)
            is JSArray -> QuickJS.newArray(ref)
            is JSObject -> QuickJS.newObject(ref)
            is JSNull -> QuickJS.getNull(ref)
            is JSUndefined -> QuickJS.getUndefined(ref)
            else -> QuickJS.newObject(ref)
        }
        return JSValue(valueRef, this)
    }

    override fun release() {
        QuickJS.freeContext(ref)
    }

    fun release(valueRef: Long) = QuickJS.freeValue(ref, valueRef)

    fun isBool(valueRef: Long) = QuickJS.isBool(valueRef)

    fun isBool(value: JSValue) = isBool(value.ref)

    fun getBool(valueRef: Long) = QuickJS.getBool(valueRef)

    fun getBool(value: JSValue) = getBool(value.ref)

    fun newBool(value: Boolean): JSValue {
        val v = QuickJS.newBool(ref, value)
        return JSValue(v, this)
    }

    fun isNumber(valueRef: Long) = QuickJS.isNumber(valueRef)

    fun isNumber(value: JSValue) = isNumber(value.ref)

    fun getInt(valueRef: Long) = QuickJS.getInt(valueRef)

    fun getInt(value: JSValue) = getInt(value.ref)

    fun newInt(value: Int): JSValue {
        val v = QuickJS.newInt(ref, value)
        return JSValue(v, this)
    }

    fun getDouble(valueRef: Long) = QuickJS.getFloat64(ref, valueRef)

    fun getDouble(value: JSValue) = getDouble(value.ref)

    fun newDouble(value: Double): JSValue {
        val v = QuickJS.newFloat64(ref, value)
        return JSValue(v, this)
    }

    fun isString(valueRef: Long) = QuickJS.isString(valueRef)

    fun isString(value: JSValue) = isString(value.ref)

    fun getString(valueRef: Long) = QuickJS.getString(ref, valueRef)

    fun getString(value: JSValue) = getString(value.ref)

    fun newString(value: String): JSValue {
        val v = QuickJS.newString(ref, value)
        return JSValue(v, this)
    }

    fun isArray(valueRef: Long) = QuickJS.isArray(ref, valueRef)

    fun isArray(value: JSValue) = isArray(value.ref)

    fun newArray(): JSValue {
        val v = QuickJS.newArray(ref)
        return JSValue(v, this)
    }

    fun isObject(valueRef: Long) = QuickJS.isObject(valueRef)

    fun isObject(value: JSValue) = isObject(value.ref)

    fun newObject(): JSValue {
        val v = QuickJS.newObject(ref)
        return JSValue(v, this)
    }

    fun getNull(): JSValue {
        val v = QuickJS.getNull(ref)
        return JSValue(v, this)
    }

    fun getUndefined(): JSValue {
        val v = QuickJS.getUndefined(ref)
        return JSValue(v, this)
    }

    fun getType(valueRef: Long) = QuickJS.getType(valueRef)

    fun getType(value: JSValue) = getType(value.ref)

    fun getPropertyNames(valueRef: Long): Array<String> = QuickJS.getOwnPropertyNames(ref, valueRef)

    fun getPropertyNames(value: JSValue): Array<String> = getPropertyNames(value.ref)
}