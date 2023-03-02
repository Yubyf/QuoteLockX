package com.crossbowffs.quotelock.utils

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private const val TYPE_STRING = 1
private const val TYPE_STRING_LIST = 2

fun ByteBuffer.readTlvString(): String? {
    val type = get().toInt()
    val length = int
    if (type != TYPE_STRING) {
        position(position() + length)
        return null
    }
    if (length < 0) {
        return null
    }
    return ByteArray(length).apply {
        get(this)
    }.toString(Charsets.UTF_8)
}

fun ByteBuffer.readLvString(): String? {
    val length = int
    if (length < 0) {
        return null
    }
    return ByteArray(length).apply {
        get(this)
    }.toString(Charsets.UTF_8)
}

fun ByteBuffer.readTlvStringList(): List<String>? {
    val type = get().toInt()
    val length = int
    if (type != TYPE_STRING_LIST) {
        position(position() + length)
        return null
    }
    if (length < 0) {
        return null
    }
    return (0 until length).mapNotNull { readTlvString() }
}

fun ByteBuffer.readLvBytes(): ByteArray? {
    val length = int
    if (length < 0) {
        return null
    }
    return ByteArray(length).apply {
        get(this)
    }
}

fun ByteArrayOutputStream.writeTlvString(string: String) {
    write(TYPE_STRING)
    val stringBytes = string.toByteArray()
    write(stringBytes.size.toBytes())
    write(stringBytes)
}

fun ByteArrayOutputStream.writeLvString(string: String) {
    val stringBytes = string.toByteArray()
    write(stringBytes.size.toBytes())
    write(stringBytes)
}

fun ByteArrayOutputStream.writeTlvStringList(list: List<String>) {
    write(TYPE_STRING_LIST)
    write(list.size.toBytes())
    list.forEach(::writeTlvString)
}

fun ByteArrayOutputStream.writeLvBytes(bytes: ByteArray) {
    write(bytes.size.toBytes())
    write(bytes)
}

private fun Int.toBytes(): ByteArray = byteArrayOf(
    (this ushr 24).toByte(),
    (this ushr 16).toByte(),
    (this ushr 8).toByte(),
    this.toByte()
)

private fun ByteArray.toInt() = (this[0].toInt() shl 24) or
        (this[1].toInt() shl 16) or
        (this[2].toInt() shl 8) or
        this[3].toInt()