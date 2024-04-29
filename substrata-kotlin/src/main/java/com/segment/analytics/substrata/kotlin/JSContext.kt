package com.segment.analytics.substrata.kotlin

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JSContext(
    val contextRef: Long
): Releasable {
    val referenceHandlers = mutableSetOf<ReferenceHandler>()

    private val memoryManager = MemoryManager(this)

    internal val globalReferences = ConcurrentHashMap<Long, MutableSet<JSConvertible>>()

    val JSNull = getNull()

    val JSUndefined = getUndefined()

    val registry = JSRegistry(this)

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

    fun evaluate(script: String): Any? {
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
            JSFunction::class -> value.asJSFunction()
            JSValue::class -> value
            JSException::class -> throw Exception(value.asJSException()!!.getException())
            JSConvertible::class ->value
            Any::class -> getAny(value)
            else -> throw Exception("Property cannot be casted to the type ${T::class.javaClass.name}")
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
                if (isFunction(value)) {
                    value.asJSFunction()
                }
                else if (isArray(value)) {
                    value.asJSArray()
                }
                else {
                    value.asJSObject()
                }
            }
            QuickJS.TYPE_EXCEPTION -> value.asJSException()
            QuickJS.TYPE_NULL -> null
            QuickJS.TYPE_UNDEFINED -> JSUndefined
//            QuickJS.TYPE_EXCEPTION -> getExecption()
            else -> throw Exception("Property type is undefined")
        }
    }

    fun getProperties(jsValue: JSConvertible): MutableMap<String, Any?> {
        val names = QuickJS.getOwnPropertyNames(this.contextRef, jsValue.ref)
        val result = mutableMapOf<String, Any?>()
        for (name in names) {
            val value: Any? = getProperty(jsValue, name)
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

    fun hasProperty(obj: JSConvertible, name: String) = QuickJS.hasProperty(contextRef, obj.ref, name)

    private fun getValueAsJSArray(jsValue: JSConvertible): JSArray {
        val size: Int = getProperty(jsValue, "length")
        val result = MutableList(size) { i ->
            val value: Any = getProperty(jsValue, i)
            value
        }

        return JSArray(-1, this)
    }

    fun newJSValue(value: Any?): JSConvertible {
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

    fun getJSFunction(value: JSConvertible): JSFunction {
        return if (value is JSValue) {
            JSFunction(value)
        }
        else {
            JSFunction(value.ref, this)
        }
    }

    override fun release() {
        // first clear all the reference from the global table, so memory manager can release them
        globalReferences.clear()
        memoryManager.release()
        QuickJS.freeContext(contextRef)
    }

    fun release(valueRef: Long) {
        try {
            QuickJS.freeValue(contextRef, valueRef)
        }
        catch (_: Exception) {

        }
    }

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

    fun isFunction(valueRef: Long) = QuickJS.isFunction(contextRef, valueRef)

    fun isFunction(value: JSConvertible) = isFunction(value.ref)

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

    fun call(funcRef: Long, objRef: Long, paramsRef: LongArray): Long = QuickJS.call(contextRef, funcRef, objRef, paramsRef)

    fun call(func: JSConvertible, obj: JSConvertible, vararg params: JSConvertible): Long {
        val refs = params.map { it.ref }.toLongArray()
        return call(func.ref, obj.ref, refs)
    }

    fun registerFunction(valueRef: Long, functionName: String, body: JSFunctionBody) = registerFunction(valueRef, functionName, body as Function<Any?>)

    fun registerFunction(jsValue: JSConvertible, functionName: String, body: JSFunctionBody) = registerFunction(jsValue.ref, functionName, body as Function<Any?>)

    fun registerFunction(jsValue: JSConvertible, functionName: String, body: Function<Any?>) = registerFunction(jsValue.ref, functionName, body)

    internal fun registerFunction(valueRef: Long, functionName: String, body: Function<Any?>): JSFunction {
        val functionId = registry.nextFunctionId
        registry.functions[functionId] = mutableListOf(body)
        val ret = QuickJS.newFunction(this, contextRef, valueRef, functionName, functionId)
        return get(ret)
    }

    fun registerFunction(jsValue: JSConvertible, functionName: String,  overloads: MutableList<Function<Any?>>) = registerFunction(jsValue.ref, functionName, overloads)

    fun registerFunction(valueRef: Long, functionName: String,  overloads: MutableList<Function<Any?>>): JSFunction {
        val functionId = registry.nextFunctionId
        registry.functions[functionId] = overloads
        val ret = QuickJS.newFunction(this, contextRef, valueRef, functionName, functionId)
        return get(ret)
    }

    fun registerClass(valueRef: Long, clazzName: String, clazz: JSClass): JSClass {
        val clazzId = registry.nextClassId
        registry.classes[clazzId] = clazz
        QuickJS.newClass(this, contextRef, valueRef, clazzName, clazzId)

        return clazz
    }

    fun registerClass(jsValue: JSConvertible, clazzName: String, clazz: JSClass) = registerClass(jsValue.ref, clazzName, clazz)

    fun findClass(clazz: KClass<*>) = registry.classes.values.find { it.clazz == clazz }

    fun registerProperty(valueRef: Long, propertyName: String, property: JSProperty) {
        val getterId = registry.nextFunctionId
        val getter: JSInstanceFunctionBody = { instance, params ->
            property.getter(instance)
        }
        registry.functions[getterId] = mutableListOf(getter)

        val setterId = registry.nextFunctionId
        val setter: JSInstanceFunctionBody = { instance, params ->
            property.setter(instance, params[0])
        }
        registry.functions[setterId] = mutableListOf(setter)

        QuickJS.newProperty(this, contextRef, valueRef, propertyName, getterId, setterId)
    }

    fun registerProperty(jsValue: JSConvertible, propertyName: String, property: JSProperty) = registerProperty(jsValue.ref, propertyName, property)

    fun getJSException() = QuickJS.getException(contextRef)
}