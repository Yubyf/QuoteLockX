@file:Suppress("UNCHECKED_CAST")

package com.crossbowffs.quotelock.utils

/** Utility functions for reflection-related operations. */

/**
 * Returns the value of the specified field of the specified object.
 *
 * @param field The field name to get the value of.
 * @return The value of the specified field of the specified object.
 * @throws NoSuchFieldException If the specified field does not exist in current class and its superclasses.
 */
@Throws(NoSuchFieldException::class)
fun <T> Any.getReflectionField(field: String): T? {
    return getReflectionField<T>(javaClass, field)
}

/**
 * Sets the value of the specified field of the specified object.
 *
 * @param field The field name to get the value of.
 * @throws NoSuchFieldException If the specified field does not exist in current class and its superclasses.
 */
@Throws(NoSuchFieldException::class)
fun <T> Any.setReflectionField(field: String, value: T) {
    setReflectionField(javaClass, field, value)
}

/**
 * Invokes the specified method of the specified object.
 *
 * @param method The method name to invoke.
 * @return The value returned by the invoked method.
 * @throws NoSuchMethodException If the specified method does not exist in current class and its superclasses.
 */
@Throws(NoSuchMethodException::class)
fun <T> Any.invokeReflectionMethod(
    method: String,
    args: LinkedHashMap<Class<*>, Any?> = linkedMapOf(),
): T? {
    return invokeReflectionMethod<T>(javaClass, method, args)
}

@Throws(NoSuchFieldException::class)
private tailrec fun <T> Any.getReflectionField(
    target: Class<*>,
    field: String,
): T? {
    val result = runCatching {
        target.getDeclaredField(field).let {
            it.isAccessible = true
            it[this] as? T
        }
    }
    return when {
        result.isSuccess -> result.getOrNull()
        target.superclass == null ->
            throw NoSuchFieldException("Field $field not found in current class and its superclasses")

        else -> getReflectionField(target.superclass, field)
    }
}

@Throws(NoSuchFieldException::class)
private tailrec fun <T> Any.setReflectionField(
    target: Class<*>,
    field: String,
    value: T,
) {
    val result = runCatching {
        target.getDeclaredField(field).let {
            it.isAccessible = true
            it.set(this, value)
        }
    }
    if (result.isFailure) {
        if (target.superclass == null) {
            throw NoSuchFieldException("Field $field not found in current class and its superclasses")
        } else {
            setReflectionField(target.superclass, field, value)
        }
    }
}

@Throws(NoSuchMethodException::class)
private tailrec fun <T> Any.invokeReflectionMethod(
    target: Class<*>,
    method: String,
    args: LinkedHashMap<Class<*>, Any?> = linkedMapOf(),
): T? {
    val result = runCatching {
        target.getDeclaredMethod(method, *args.keys.toTypedArray()).let {
            it.isAccessible = true
            it.invoke(this, *args.values.toTypedArray()) as? T
        }
    }
    return when {
        result.isSuccess -> result.getOrNull()
        target.superclass == null ->
            throw NoSuchMethodException("Method $method not found in current class and its superclasses")

        else -> invokeReflectionMethod<T>(target.superclass, method, args)
    }
}