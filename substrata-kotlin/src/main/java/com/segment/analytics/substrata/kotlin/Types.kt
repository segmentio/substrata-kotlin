package com.segment.analytics.substrata.kotlin

import com.segment.analytics.substrata.kotlin.JsonElementConverter.toJsonElement
import com.segment.analytics.substrata.kotlin.JsonElementConverter.wrap
import kotlinx.serialization.json.JsonElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

interface Releasable {
    fun release()
}

/**
 * Unlike other JSConvertible by default does not register to memory management.
 * To make it memory manageable see the implementation of JSValue.
 * */
interface JSConvertible: Releasable {
    val ref: Long
    val context: JSContext
}

/**
 * Unlike other JSConvertible, JSValue auto registers itself to memory management.
 * It is released once the scope is end
 * */
class JSValue(
    override val ref: Long,
    override val context: JSContext) : JSConvertible, Releasable {

    init {
        context.notifyReferenceCreated(this)
    }

    override fun release() {
        releasePointer()
        releaseReference()
    }

    /**
     * Release the pointer in QuickJS
     * */
    fun releasePointer() {
        context.release(ref)
    }

    /**
     * Release the reference hold by its observers
     * */
    fun releaseReference() {
        context.notifyReferenceReleased(this)
    }
}

/**
 * JSArray uses Delegation pattern instead of inheritance to
 * avoid registered to memory management multiple times for the
 * same JSValue.
 * */
class JSArray(jsValue: JSValue): JSConvertible by jsValue {

    var size: Int = 0
        internal set

    constructor(ref: Long, context: JSContext) : this(JSValue(ref, context))

    fun add(value: Boolean) = with(context) {
        setProperty(this@JSArray, size++, newBool(value))
    }

    fun add(value: Int) = with(context) {
        setProperty(this@JSArray, size++, newInt(value))
    }

    fun add(value: Double) = with(context) {
        setProperty(this@JSArray, size++, newDouble(value))
    }

    fun add(value: JSObject) = with(context) {
        setProperty(this@JSArray, size++, value)
    }

    fun add(value: JSArray) = with(context) {
        setProperty(this@JSArray, size++, value)
    }

    fun add(value: String) = with(context) {
        setProperty(this@JSArray, size++, newString(value))
    }

    fun add(value: JsonElement) = with(context) {
        setProperty(this@JSArray, size++, value.wrap(this))
    }

    fun <T: JSConvertible> add(value: T, converter: JSConverter<T>) = with(context) {
        setProperty(this@JSArray, size++, value.toJSValue(this))
    }

    fun getBoolean(index: Int): Boolean = context.getProperty(this, index)

    fun getInt(index: Int): Int = context.getProperty(this, index)

    fun getDouble(index: Int): Double = context.getProperty(this, index)

    fun getString(index: Int): String = context.getProperty(this, index)

    fun getJSObject(index: Int): JSObject = context.getProperty(this, index)

    fun getJSArray(index: Int): JSArray = context.getProperty(this, index)

    fun getJsonElement(index: Int): JsonElement {
        val property: Any = context.getProperty(this, index)
        return property.toJsonElement()
    }

    operator fun get(index: Int): Any = context.getProperty(this, index)

    fun <T> getJSConvertible(index: Int, converter: JSConverter<T>) : T {
        val jsArray =  context.getProperty<JSArray>(this, index)
        return converter.read(jsArray)
    }
}

/**
 * JSObject uses Delegation pattern instead of inheritance to
 * avoid registered to memory management multiple times for the
 * same JSValue.
 * */
class JSObject(
    jsValue: JSValue
): JSConvertible by jsValue, KeyValueObject {

    constructor(ref: Long, context: JSContext): this(JSValue(ref, context))

    override fun set(key: String, value: Int) = with(context) {
        setProperty(this@JSObject, key, newInt(value))
    }

    override fun set(key: String, value: Boolean) = with(context) {
        setProperty(this@JSObject, key, newBool(value))
    }

    override fun set(key: String, value: Double) = with(context) {
        setProperty(this@JSObject, key, newDouble(value))
    }

    override fun set(key: String, value: String) = with(context) {
        setProperty(this@JSObject, key, newString(value))
    }

    override fun set(key: String, value: JSObject) = with(context) {
        setProperty(this@JSObject, key, value)
    }

    override fun set(key: String, value: JSArray) = with(context){
        setProperty(this@JSObject, key, value)
    }

    override fun set(key: String, value: JsonElement) = with(context) {
        val v = value.wrap(this)
        setProperty(this@JSObject, key, v)
    }

    override fun set(key: String, value: JSConvertible) {
        context.setProperty(this, key, value)
    }

    override fun getBoolean(key: String): Boolean = context.getProperty(this, key)

    override fun getInt(key: String): Int {
        val property = context.getProperty<JSValue>(this, key)
        return context.getInt(property)
    }

    override fun getDouble(key: String): Double = context.getProperty(this, key)

    override fun getString(key: String): String = context.getProperty(this, key)

    override fun getJSObject(key: String): JSObject = context.getProperty(this@JSObject, key)

    override fun getJSArray(key: String): JSArray = context.getProperty(this, key)

    override fun getJsonElement(key: String): JsonElement {
        val property: Any =  context.getProperty(this, key)
        return property.toJsonElement()
    }

    override fun getJSFunction(key: String): JSFunction = context.getProperty(this, key)

    override fun getJSConvertible(key: String): JSConvertible = context.getProperty(this, key)

    override fun get(key: String): Any = context.getProperty(this, key)

    override fun contains(key: String) = context.hasProperty(this, key)

    fun register(function: String, body: JSFunctionBody): JSFunction {
        if (contains(function)) {
            return getJSFunction(function)
        }

        return context.registerFunction(this, function, body)
    }

    fun register(clazzName: String, clazz: JSClass) {
        if (contains(clazzName)) return

        context.registerClass(this, clazzName, clazz)
    }
}

class JSFunction(jsValue: JSValue) : JSConvertible by jsValue {
    constructor(ref: Long, context: JSContext): this(JSValue(ref, context))
    inline operator fun <reified T> invoke(obj: JSConvertible, vararg params: Any): T {
        val p = params.map { it.toJSValue(context).ref }.toLongArray()
        val ret = context.call(this@JSFunction.ref, obj.ref, p)
        return context.get(ret)
    }
}

open class JSClass(
    val context: JSContext,
    val clazz: Class<*>,
    val include: Set<String>? = null
) {

    open fun createPrototype(): Any {
        try {
            return clazz.newInstance()
        }
        catch (e: Exception) {
            throw Exception("Failed to create prototype for ${clazz.name}. Exported class is required to have a parameterless constructor.")
        }
    }

    open fun createInstance(args: LongArray): Any {
        if (args.isEmpty()) return clazz.newInstance()

        val params = args.map { return@map context.get<Any>(it) }.toTypedArray()
        outer@ for (ctor in clazz.constructors) {
            if (ctor.parameterTypes.size == args.size) {
                for (type in ctor.parameterTypes) {
                    if (!type.isInstance(params[0])) {
                        continue@outer
                    }
                }
                return ctor.newInstance(*params)
            }
        }

        throw Exception("No matching constructor found for ${clazz.name} with parameters ${params.contentToString()}")
    }

    open fun getStaticMethods() = getMethods(null) { method ->
        if (include == null) Modifier.isStatic(method.modifiers)
        else Modifier.isStatic(method.modifiers) && include.contains(method.name)
    }

    open fun getMethods(obj: Any) = getMethods(obj) { method ->
        if (include == null) !Modifier.isStatic(method.modifiers)
        else !Modifier.isStatic(method.modifiers) && include.contains(method.name)
    }

    open fun getStaticFields() = getFields(null) { field ->
        if (include == null) Modifier.isStatic(field.modifiers)
        else Modifier.isStatic(field.modifiers) && include.contains(field.name)
    }

    open fun getFields(obj: Any) = getFields(obj) { field ->
        if (include == null) !Modifier.isStatic(field.modifiers)
        else !Modifier.isStatic(field.modifiers) && include.contains(field.name)
    }

    private fun getFields(obj: Any?, condition: (Field) -> Boolean) : Map<String, JSConvertible> {
        val fields = mutableMapOf<String, JSConvertible>()

        for (field in clazz.fields) {
            if (condition(field)) {
                fields[field.name] = field.get(obj)?.toJSValue(context) ?: context.JSNull
            }
        }

        return fields
    }

    private fun getMethods(obj: Any?, condition: (Method) -> Boolean) : Map<String, JSFunctionBody> {
        val methods = mutableMapOf<String, JSFunctionBody>()

        for (method in clazz.methods) {
            if (condition(method)) {
                methods[method.name] = { params ->
                    val paramsTypes = method.parameterTypes
                    if (paramsTypes.size != params.size) {
                        throw Exception("Arguments does not match to Java method ${method.name}")
                    }

                    for (i in paramsTypes.indices) {
                        if (paramsTypes[i] == params[i]?.javaClass) {
                            throw Exception("Wrong argument passed to Java method ${method.name}. Expecting ${paramsTypes[i].name}, but was ${params[i]?.javaClass?.name}")
                        }
                    }

                    method(obj, *params.toTypedArray())
                }
            }
        }

        return methods
    }

}


class JSException private constructor(
    val isError: Boolean,
    private val exception: String,
    stack: String
) {
    /**
     * The stack trace.
     */
    val stack: String?

    init {
        this.stack = stack
    }

    /**
     * The exception message.
     */
    fun getException(): String {
        return exception
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (!isError) {
            sb.append("Throw: ")
        }
        sb.append(exception).append("\n")
        if (stack != null) {
            sb.append(stack)
        }
        return sb.toString()
    }
}


typealias JSFunctionBody = (List<Any?>) -> Any?

interface KeyValueObject {
    operator fun set(key: String, value: Int)

    operator fun set(key: String, value: Boolean)

    operator fun set(key: String, value: Double)

    operator fun set(key: String, value: String)

    operator fun set(key: String, value: JSObject)

    operator fun set(key: String, value: JSArray)

    operator fun set(key: String, value: JsonElement)

    operator fun set(key: String, value: JSConvertible)

    fun getBoolean(key: String): Boolean

    fun getInt(key: String): Int

    fun getDouble(key: String): Double

    fun getString(key: String): String

    fun getJSObject(key: String): JSObject

    fun getJSArray(key: String): JSArray

    fun getJsonElement(key: String): JsonElement

    fun getJSFunction(key: String): JSFunction

    fun getJSConvertible(key: String): JSConvertible

    operator fun get(key: String): Any?

    fun contains(key: String): Boolean
}