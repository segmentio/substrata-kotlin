package com.segment.analytics.substrata.kotlin

class QuickJS {

    companion object {
        const val TYPE_SYMBOL = -8
        const val TYPE_STRING = -7
        const val TYPE_OBJECT = -1
        const val TYPE_INT = 0
        const val TYPE_BOOLEAN = 1
        const val TYPE_NULL = 2
        const val TYPE_UNDEFINED = 3
        const val TYPE_EXCEPTION = 6
        const val TYPE_FLOAT64 = 7

        external fun freeValue(ref: Long, ref1: Long)
        external fun isBool(ref: Long): Boolean
        external fun getBool(ref: Long): Boolean
        external fun newBool(context: Long, value: Boolean): Long
        external fun isNumber(ref: Long): Boolean
        external fun getInt(ref: Long): Int
        external fun newInt(context: Long, i: Int): Long
        external fun getFloat64(ref: Long): Double
        external fun newFloat64(context: Long, d: Double): Long
        external fun isString(ref: Long): Boolean
        external fun getString(ref: Long): String
        external fun newString(context: Long, s: String): Long
        external fun isArray(ref: Long): Boolean
        external fun newArray(context: Long): Long
        external fun getProperty(context: Long, ref: Long, index: Int): Long
        external fun setProperty(context: Long, ref: Long, index: Int, value: Long)
        external fun setProperty(context: Long, ref: Long, key: String, value: Long)
        external fun getProperty(context: Long, ref: Long, name: String): Long
        external fun isObject(ref: Long): Boolean
        external fun newObject(context: Long): Long
        external fun getType(ref: Long): Int
        external fun getOwnPropertyNames(context: Long, ref: Long): Array<String>
        external fun call(context: Long, function: Long, obj: Long, args: Array<Long>): Long
    }
}