package com.segment.analytics.substrata.kotlin

import kotlin.reflect.KClass
import kotlin.reflect.KType


fun KType.defaultValue(): Any? {
    val classifier = this.classifier as? KClass<*>

    // For nullable types, decide whether to return null or a default value
    val shouldReturnNull = this.isMarkedNullable && when (classifier) {
        String::class -> false  // Return "" even for String?
        // Add other types where you want defaults instead of null
        Int::class, Long::class, Double::class, Float::class, Boolean::class -> false
        else -> true  // Return null for other nullable types
    }

    if (shouldReturnNull) return null

    return when (classifier) {
        // Primitives
        String::class -> ""
        Int::class -> 0
        Long::class -> 0L
        Double::class -> 0.0
        Float::class -> 0.0f
        Boolean::class -> false
        Byte::class -> 0.toByte()
        Short::class -> 0.toShort()
        Char::class -> '\u0000'

        // Collections
        List::class -> emptyList<Any>()
        Set::class -> emptySet<Any>()
        Map::class -> emptyMap<Any, Any>()
        Array::class -> emptyArray<Any>()

        // Try to create instance with default constructor
        else -> try {
            classifier?.constructors?.firstOrNull {
                it.parameters.all { param -> param.isOptional }
            }?.callBy(emptyMap())
        } catch (e: Exception) {
            null
        }
    }
}

fun typesCompatible(parameterType: KType, actualValue: Any?): Boolean {
    // Handle null values
    if (actualValue == null) {
        return parameterType.isMarkedNullable
    }

    val paramClassifier = parameterType.classifier as? KClass<*> ?: return false
    val actualClass = actualValue::class

    return when {
        // Direct match
        paramClassifier == actualClass -> true

        // Check inheritance/interface implementation
        paramClassifier.isInstance(actualValue) -> true

        // Handle primitive boxing
        isPrimitiveCompatible(paramClassifier, actualClass) -> true

        // Handle generics (basic check)
        paramClassifier.java.isAssignableFrom(actualClass.java) -> true

        else -> false
    }
}

private fun isPrimitiveCompatible(expected: KClass<*>, actual: KClass<*>): Boolean {
    val primitiveMap = mapOf(
        Int::class to setOf(java.lang.Integer::class, Int::class),
        Long::class to setOf(java.lang.Long::class, Long::class),
        Double::class to setOf(java.lang.Double::class, Double::class),
        Float::class to setOf(java.lang.Float::class, Float::class),
        Boolean::class to setOf(java.lang.Boolean::class, Boolean::class),
        Byte::class to setOf(java.lang.Byte::class, Byte::class),
        Short::class to setOf(java.lang.Short::class, Short::class),
        Char::class to setOf(java.lang.Character::class, Char::class)
    )

    return primitiveMap[expected]?.contains(actual) == true ||
            primitiveMap[actual]?.contains(expected) == true
}
