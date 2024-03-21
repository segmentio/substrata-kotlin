package com.segment.analytics.substrata.kotlin

class QuickJS {

    companion object {
        init {
            System.loadLibrary("substrata-quickjs")
        }

        const val TYPE_SYMBOL = -8
        const val TYPE_STRING = -7
        const val TYPE_OBJECT = -1
        const val TYPE_INT = 0
        const val TYPE_BOOLEAN = 1
        const val TYPE_NULL = 2
        const val TYPE_UNDEFINED = 3
        const val TYPE_EXCEPTION = 6
        const val TYPE_FLOAT64 = 7

        const val EVAL_TYPE_GLOBAL = 0
        const val EVAL_TYPE_MODULE = 1
        const val EVAL_FLAG_STRICT = 8
        const val EVAL_FLAG_STRIP = 16

        const val EVALUATOR = "Substrata.Runtime"

        fun createJSRuntime(): JSRuntime {
            val ref = newRuntime()
            if (ref == 0L) {
                throw IllegalStateException("Failed to create JSRuntime")
            }
            return JSRuntime(ref)
        }

        external fun freeValue(context: Long, ref: Long)
        external fun freeRuntime(runtime: Long)
        external fun isBool(ref: Long): Boolean
        external fun getBool(ref: Long): Boolean
        external fun newBool(context: Long, value: Boolean): Long
        external fun isNumber(ref: Long): Boolean
        external fun getInt(ref: Long): Int
        external fun newInt(context: Long, i: Int): Long
        external fun getFloat64(context: Long, ref: Long): Double
        external fun newFloat64(context: Long, d: Double): Long
        external fun isString(ref: Long): Boolean
        external fun getString(context: Long, ref: Long): String
        external fun newString(context: Long, s: String): Long
        external fun isArray(context: Long, ref: Long): Boolean
        external fun newArray(context: Long): Long
        external fun getProperty(context: Long, ref: Long, index: Int): Long
        external fun setProperty(context: Long, ref: Long, index: Int, value: Long)
        external fun setProperty(context: Long, ref: Long, key: String, value: Long)
        external fun getProperty(context: Long, ref: Long, key: String): Long
        external fun hasProperty(context: Long, ref: Long, key: String): Boolean
        external fun isObject(ref: Long): Boolean
        external fun newObject(context: Long): Long
        external fun getNull(context: Long): Long
        external fun getUndefined(context: Long): Long
        external fun getType(ref: Long): Int
        external fun getOwnPropertyNames(context: Long, ref: Long): Array<String>
        external fun call(context: Long, function: Long, obj: Long, args: LongArray): Long
        external fun newRuntime(): Long
        external fun newContext(runtime: Long): Long
        external fun freeContext(context: Long)
        external fun getGlobalObject(context: Long): Long
        external fun evaluate(context: Long, script: String, file: String, flags: Int): Long
        external fun isFunction(contextRef: Long, valueRef: Long): Boolean
        external fun newFunction(context: JSContext, contextRef: Long, valueRef: Long, functionName: String, functionId: Int): Long
    }
}