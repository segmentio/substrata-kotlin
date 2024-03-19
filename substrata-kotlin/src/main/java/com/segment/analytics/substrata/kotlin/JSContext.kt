package com.segment.analytics.substrata.kotlin

class JSContext(
    val contextRef: Long
): Releasable {
    val referenceHandlers = mutableSetOf<ReferenceHandler>()

    val JSNull = getNull()

    val JSUndefined = getUndefined()

    fun addReferenceHandler(handler: ReferenceHandler) {
        referenceHandlers.add(handler)
    }

    fun removeReferenceHandler(handler: ReferenceHandler) {
        referenceHandlers.remove(handler)
    }

    fun notifyReferenceCreated(reference: JSConvertible) {
        for (handler in referenceHandlers) {
            handler.onCreated(reference)
        }
    }

    fun executeScript(script: String): Any? {
        val ret = QuickJS.evaluate(contextRef, script, QuickJS.EVALUATOR, QuickJS.EVAL_TYPE_GLOBAL)
        return getAny(ret)
    }

//    fun executeFunction(function: String, params: JSArray?): Any {
//        val ret = QuickJS.call(ref, ref, obj.ref, refs)
//        return context.getAny(ret)
//    }

    fun getGlobalObject(): JSObject {
        val globalContext = QuickJS.getGlobalObject(contextRef)
        return get(globalContext)
    }

    fun notifyReferenceReleased(reference: JSConvertible) {
        for (handler in referenceHandlers) {
            handler.onReleased(reference)
        }
    }

    inline fun <reified T> getProperty(jsValue: JSConvertible, name: String): T {
        val propertyRef = QuickJS.getProperty(this.contextRef, jsValue.ref, name)
        return get(propertyRef)
    }

    inline fun <reified T> getProperty(jsValue: JSConvertible, index: Int): T {
        val propertyRef = QuickJS.getProperty(this.contextRef, jsValue.ref, index)
        return get(propertyRef)
    }

    inline fun <reified T> get(ref: Long): T {
        val value = JSValue(ref, this)
        val result = when(T::class) {
            String::class -> value.asString()
            Boolean::class -> value.asBoolean()
            Int::class -> value.asInt()
            Double::class -> value.asDouble()
            JSObject::class -> value.asJSObject()
            JSArray::class -> value.asJSArray()
            JSValue::class -> value
            JSConvertible::class ->value
            Any::class -> getAny(value)
            else -> throw Exception("Property cannot be casted to the type specified")
        }
        return result as T
    }

    fun getAny(ref: Long) = getAny(JSValue(ref, this))

    fun getAny(value: JSValue): Any? {
        val type = QuickJS.getType(value.ref)
        return when (type) {
            QuickJS.TYPE_STRING -> value.asString()
            QuickJS.TYPE_BOOLEAN -> value.asBoolean()
            QuickJS.TYPE_INT -> value.asInt()
            QuickJS.TYPE_FLOAT64 -> value.asDouble()
            QuickJS.TYPE_OBJECT -> {
                if (isArray(value)) {
                    value.asJSArray()
                }
                else {
                    value.asJSObject()
                }
            }
            QuickJS.TYPE_NULL -> JSNull
            QuickJS.TYPE_UNDEFINED -> JSUndefined
//            QuickJS.TYPE_EXCEPTION -> getExecption()
            else -> throw Exception("Property type is undefined")
        }
    }

    fun getProperties(jsValue: JSConvertible): MutableMap<String, Any> {
        val names = QuickJS.getOwnPropertyNames(this.contextRef, jsValue.ref)
        val result = mutableMapOf<String, Any>()
        for (name in names) {
            val value: Any = getProperty(jsValue, name)
            result[name] = value
        }
        return result
    }

    fun setProperty(obj: JSConvertible, index: Int, value: JSConvertible) {
        QuickJS.setProperty(contextRef, obj.ref, index, value.ref)
    }

    fun setProperty(obj: JSConvertible, name: String, value: JSConvertible) {
        QuickJS.setProperty(contextRef, obj.ref, name, value.ref)
    }

    private fun getValueAsJSArray(jsValue: JSConvertible): JSArray {
        val size: Int = getProperty(jsValue, "length")
        val result = MutableList(size) { i ->
            val value: Any = getProperty(jsValue, i)
            value
        }

        return JSArray(-1, this)
    }

    inline fun <reified T> newJSValue(value: T?): JSConvertible {
        val valueRef = when(value) {
            is String -> QuickJS.newString(contextRef, value)
            is Boolean -> QuickJS.newBool(contextRef, value)
            is Int -> QuickJS.newInt(contextRef, value)
            is Double -> QuickJS.newFloat64(contextRef,value)
            is JSArray -> QuickJS.newArray(contextRef)
            is JSObject -> QuickJS.newObject(contextRef)
            else -> QuickJS.newObject(contextRef)
        }
        return JSValue(valueRef, this)
    }

    fun getJSArray(value: JSConvertible): JSArray {
        val size: Int = getProperty(value, "length")
        val ret = if (value is JSValue) {
            JSArray(value)
        }
        else {
            JSArray(value.ref, this)
        }
        ret.size = size
        return ret
    }

    fun getJSObject(value: JSConvertible): JSObject {
        return if (value is JSValue) {
            JSObject(value)
        }
        else {
            JSObject(value.ref, this)
        }
    }

    override fun release() {
        QuickJS.freeContext(contextRef)
    }

    fun release(valueRef: Long) = QuickJS.freeValue(contextRef, valueRef)

    fun isBool(valueRef: Long) = QuickJS.isBool(valueRef)

    fun isBool(value: JSConvertible) = isBool(value.ref)

    fun getBool(valueRef: Long) = QuickJS.getBool(valueRef)

    fun getBool(value: JSConvertible) = getBool(value.ref)

    fun newBool(value: Boolean): JSConvertible {
        val v = QuickJS.newBool(contextRef, value)
        return JSValue(v, this)
    }

    fun isNumber(valueRef: Long) = QuickJS.isNumber(valueRef)

    fun isNumber(value: JSConvertible) = isNumber(value.ref)

    fun getInt(valueRef: Long) = QuickJS.getInt(valueRef)

    fun getInt(value: JSConvertible) = getInt(value.ref)

    fun newInt(value: Int): JSConvertible {
        val v = QuickJS.newInt(contextRef, value)
        return JSValue(v, this)
    }

    fun getDouble(valueRef: Long) = QuickJS.getFloat64(contextRef, valueRef)

    fun getDouble(value: JSConvertible) = getDouble(value.ref)

    fun newDouble(value: Double): JSConvertible {
        val v = QuickJS.newFloat64(contextRef, value)
        return JSValue(v, this)
    }

    fun isString(valueRef: Long) = QuickJS.isString(valueRef)

    fun isString(value: JSConvertible) = isString(value.ref)

    fun getString(valueRef: Long) = QuickJS.getString(contextRef, valueRef)

    fun getString(value: JSConvertible) = getString(value.ref)

    fun newString(value: String): JSConvertible {
        val v = QuickJS.newString(contextRef, value)
        return JSValue(v, this)
    }

    fun isArray(valueRef: Long) = QuickJS.isArray(contextRef, valueRef)

    fun isArray(value: JSConvertible) = isArray(value.ref)

    fun newArray(): JSArray {
        val v = QuickJS.newArray(contextRef)
        return JSArray(v, this)
    }

    fun isObject(valueRef: Long) = QuickJS.isObject(valueRef)

    fun isObject(value: JSConvertible) = isObject(value.ref)

    fun newObject(): JSObject {
        val v = QuickJS.newObject(contextRef)
        return JSObject(v, this)
    }

    private fun getNull(): JSConvertible {
        val v = QuickJS.getNull(contextRef)
        return JSValue(v, this)
    }

    private fun getUndefined(): JSConvertible {
        val v = QuickJS.getUndefined(contextRef)
        return JSValue(v, this)
    }

    fun getType(valueRef: Long) = QuickJS.getType(valueRef)

    fun getType(value: JSConvertible) = getType(value.ref)

    fun getPropertyNames(valueRef: Long): Array<String> = QuickJS.getOwnPropertyNames(contextRef, valueRef)

    fun getPropertyNames(value: JSConvertible): Array<String> = getPropertyNames(value.ref)
}