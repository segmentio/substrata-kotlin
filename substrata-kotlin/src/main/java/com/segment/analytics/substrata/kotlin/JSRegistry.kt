package com.segment.analytics.substrata.kotlin

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class JSRegistry (val context: JSContext) {
    private var _nextFunctionId = AtomicInteger(0)
    private var _nextClassId = AtomicInteger(0)
    private var _nextPropertyId = AtomicInteger(0)

    val nextFunctionId: Int
        get() {
            return _nextFunctionId.getAndIncrement()
        }
    val nextClassId: Int
        get() {
            return _nextClassId.getAndIncrement()
        }
    val nextPropertyId: Int
        get() {
            return _nextPropertyId.getAndIncrement()
        }

    /**
     * bind the overload functions to the same id, so we don't have to handle it in jni
     * */
    var functions = ConcurrentHashMap<Int, MutableList<Function<Any?>>>()
        private set
    var classes = ConcurrentHashMap<Int, JSClass>()
        private set
    var properties = ConcurrentHashMap<Int, JSProperty>()
        private set

    fun jsCallback(instance: Any?, functionId: Int, args: LongArray): Long {
        val params = args.map { return@map context.get<Any>(it) }
        var exception: Exception? = null
        functions[functionId]?.forEach { f ->
            try {
                if (f is Function1<*, *>) {
                    val f1 = f as Function1<List<Any?>, Any?>
                    f1(params).let {
                        if (it !is Unit) return it.toJSValue(context).ref
                        else return context.JSUndefined.ref
                    }
                } else if (f is Function2<*, *, *>) {
                    val f2 = f as Function2<Any?, List<Any?>, Any?>
                    f2(instance, params).let {
                        if (it !is Unit) return it.toJSValue(context).ref
                        else return context.JSUndefined.ref
                    }
                }
            }
            catch (e : JSCallbackInvalidParametersException) {
                exception = e
            }
        }

        // if a matching function is found and executed with no errors, the value is already returned.
        // if this line is reached, it means no matching function found
        exception?.let { throw it }
        return context.JSUndefined.ref
    }

    /**
     * Register static methods and static properties on constructor
     * */
    fun registerConstructor(jsCtorRef: Long, classId: Int) {
        classes[classId]?.let { clazz ->
            val jsProto: JSObject = context.get(jsCtorRef)

            for ((function, body) in clazz.getStaticMethods()) {
                jsProto.register(function, body)
            }

            for ((property, getterSetter) in clazz.getStaticProperties()) {
                jsProto.register(property, getterSetter)
            }
        }
    }

    /**
     * Register instance methods on prototype object
     * */
    fun registerPrototype(jsProtoRef: Long, classId: Int) {
        classes[classId]?.let { clazz ->
            val jsProto: JSObject = context.get(jsProtoRef)
            for ((function, overloads) in clazz.getMethods(clazz.createPrototype())) {
                jsProto.register(function, overloads)
            }
        }
    }

    /**
     * Create an instance object and register instance properties on it
     * */
    fun registerInstance(jsObjRef: Long, classId: Int, args: LongArray): Any? {
        classes[classId]?.let { clazz ->
            val jsObj: JSObject = context.get(jsObjRef)
            val instance = clazz.instance ?: clazz.createInstance(args)
            for ((property, getterSetter) in clazz.getProperties(instance)) {
                jsObj.register(property, getterSetter)
            }
            return instance
        }

        return null
    }
}