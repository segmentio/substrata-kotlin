package com.segment.analytics.substrata.kotlin

import com.segment.analytics.substrata.kotlin.JsonElementConverter.toJsonElement
import com.segment.analytics.substrata.kotlin.JsonElementConverter.wrap
import kotlinx.serialization.json.JsonElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.valueParameters

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

    fun register(propertyName: String, property: JSProperty) {
        if (contains(propertyName)) return

        return context.registerProperty(this, propertyName, property)
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
    val clazz: KClass<*>,
    val filter: Set<String> = emptySet()
) {

    open fun createPrototype(): Any {
        try {
            return clazz.createInstance()
        }
        catch (e: Exception) {
            throw Exception("Failed to create prototype for ${clazz.simpleName}. Exported class is required to have a parameterless constructor.")
        }
    }

    open fun createInstance(args: LongArray): Any {
        if (args.isEmpty()) return clazz.createInstance()

        val params = args.map { return@map context.get<Any>(it) }.toTypedArray()
        outer@ for (ctor in clazz.constructors) {
            if (ctor.valueParameters.size == args.size) {
                for (i in ctor.valueParameters.indices) {
                    if (ctor.valueParameters[i].type.classifier != params[i]::class) {
                        continue@outer
                    }
                }
                return ctor.call(*params)
            }
        }

        throw Exception("No matching constructor found for ${clazz.simpleName} with parameters ${params.contentToString()}")
    }

    open fun getStaticMethods() = getMethods(clazz.companionObject, clazz.companionObjectInstance)

    open fun getMethods(obj: Any) = getMethods(clazz, obj)

    open fun getStaticProperties() = getProperties(clazz.companionObject, clazz.companionObjectInstance)

    open fun getProperties(obj: Any) = getProperties(clazz, obj)

    private fun getMethods(clazz: KClass<*>?, obj: Any?): Map<String, JSFunctionBody> {
        val methods = mutableMapOf<String, JSFunctionBody>()

        clazz?.let {
            for (method in it.memberFunctions) {
                if (filter.contains(method.name)) continue
                methods[method.name] = { params ->
                    val methodParams = method.valueParameters
                    if (methodParams.size != params.size) {
                        throw Exception("Arguments does not match to Java method ${method.name}")
                    }

                    for (i in methodParams.indices) {
                        if (methodParams[i].type.classifier != params[i]!!::class) {
                            throw Exception("Wrong argument passed to Java method ${method.name}. Expecting ${methodParams[i]::class.simpleName}, but was ${params[i]!!::class.simpleName}")
                        }
                    }

                    method.call(obj, *params.toTypedArray())
                }
            }
        }

        return methods
    }

    private fun getProperties(clazz: KClass<*>?, obj: Any?): Map<String, JSProperty> {
        val properties = mutableMapOf<String, JSProperty>()

        clazz?.let {
            for (property in it.memberProperties) {
                if (filter.contains(property.name)) continue
                properties[property.name] = JSProperty(
                    getter = {
                        return@JSProperty property.getter.call(obj)
                    },
                    setter = { param ->
                        if (property is KMutableProperty<*>) {
                            property.setter.call(obj, param)
                        } else throw Exception("Property ${property.name} does not have a setter")
                    }
                )
            }
        }

        return properties
    }

//    private fun getFields(obj: Any?, condition: (Field) -> Boolean) : Map<String, JSConvertible> {
//        val fields = mutableMapOf<String, JSConvertible>()
//
//        for (field in clazz.fields) {
//            if (condition(field)) {
//                fields[field.name] = field.get(obj)?.toJSValue(context) ?: context.JSNull
//            }
//        }
//
//        return fields
//    }

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
data class JSProperty(
    val getter: () -> Any?,
    val setter: (Any?) -> Unit
)

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