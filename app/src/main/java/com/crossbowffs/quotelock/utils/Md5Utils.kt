@file:JvmName("Md5Utils")

package com.crossbowffs.quotelock.utils

import androidx.annotation.Size
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@Size(16)
@Throws(Exception::class)
private fun File.md5(): ByteArray {
    FileInputStream(this).use {
        val buffer = ByteArray(1024)
        val complete = MessageDigest.getInstance("MD5")
        var numRead: Int
        do {
            numRead = it.read(buffer)
            if (numRead > 0) {
                complete.update(buffer, 0, numRead)
            }
        } while (numRead != -1)
        return complete.digest()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.hexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

@Throws(Exception::class)
fun File.md5String(): String = md5().hexString()

fun String.md5(): String {
    try {
        // Create MD5 Hash
        val digest = MessageDigest
            .getInstance("MD5")
        digest.update(toByteArray())
        val messageDigest = digest.digest()

        // Create Hex String
        val hexString = StringBuilder()
        messageDigest.forEach {
            var h = Integer.toHexString(0xFF and it.toInt())
            while (h.length < 2) {
                h = "0$h"
            }
            hexString.append(h)
        }
        return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}