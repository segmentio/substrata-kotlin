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

    var functions = ConcurrentHashMap<Int, JSFunctionBody>()
        private set
    var classes = ConcurrentHashMap<Int, JSClass>()
        private set
    var properties = ConcurrentHashMap<Int, JSProperty>()
        private set

    fun jsCallback(instance: Any?, functionId: Int, args: LongArray): Long {
        functions[functionId]?.let { f ->
            val params = args.map { return@map context.get<Any>(it) }
            try {
                f(instance, params)?.let {
                    if (it !is Unit) return it.toJSValue(context).ref
                }
            }
            catch (e: Exception) {
                return context.JSUndefined.ref
            }
        }

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
            for ((function, body) in clazz.getMethods(clazz.createPrototype())) {
                jsProto.register(function, body)
                // TODO: call quickjs, register typedInstanceMethod with functionId and functionName
                // use `this` instance from JS_NewCFunctionMagic
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