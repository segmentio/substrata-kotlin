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

    fun jsCallback(functionId: Int, args: LongArray): Long {
        functions[functionId]?.let { f ->
            val params = args.map { return@map context.get<Any>(it) }
            f(params)?.let {
                if (it !is Unit) return it.toJSValue(context).ref
            }
        }

        return context.JSUndefined.ref
    }

    /**
     * Register static methods, static fields, instance methods
     * on the prototype object
     * */
    fun register(jsProtoRef: Long, classId: Int) {
        classes[classId]?.let { clazz ->
            val jsProto: JSObject = context.get(jsProtoRef)
            for ((function, body) in clazz.getMethods(clazz.createPrototype())) {
                jsProto.register(function, body)
            }

            for ((function, body) in clazz.getStaticMethods()) {
                jsProto.register(function, body)
            }

            for ((property, getterSetter) in clazz.getStaticProperties()) {
                jsProto.register(property, getterSetter)
            }
        }
    }

    /**
     * Create an instance object and register instance methods on it
     * */
    fun register(jsObjRef: Long, classId: Int, args: LongArray) {
        classes[classId]?.let { clazz ->
            val jsObj: JSObject = context.get(jsObjRef)
            for ((property, getterSetter) in clazz.getProperties(clazz.createInstance(args))) {
                jsObj.register(property, getterSetter)
            }
        }
    }
}