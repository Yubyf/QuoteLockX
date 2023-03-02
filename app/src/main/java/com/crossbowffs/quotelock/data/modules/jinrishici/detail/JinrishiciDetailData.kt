package com.crossbowffs.quotelock.data.modules.jinrishici.detail

import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.readTlvString
import com.crossbowffs.quotelock.utils.readTlvStringList
import com.crossbowffs.quotelock.utils.writeTlvString
import com.crossbowffs.quotelock.utils.writeTlvStringList
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

data class JinrishiciDetailData(
    val title: String,
    val dynasty: String,
    val author: String,
    val content: List<String>,
    val translate: List<String>?,
    val tags: List<String>?,
) {
    val bytes: ByteArray
        get() = ByteArrayOutputStream().use { stream ->
            stream.run {
                writeTlvString(title)
                writeTlvString(dynasty)
                writeTlvString(author)
                writeTlvStringList(content)
                writeTlvStringList(translate ?: emptyList())
                writeTlvStringList(tags ?: emptyList())
                toByteArray()
            }
        }

    companion object {
        fun fromBytes(bytes: ByteArray): JinrishiciDetailData? =
            ByteBuffer.wrap(bytes).runCatching {
                JinrishiciDetailData(
                    title = readTlvString().orEmpty(),
                    dynasty = readTlvString().orEmpty(),
                    author = readTlvString().orEmpty(),
                    content = readTlvStringList().orEmpty(),
                    translate = readTlvStringList(),
                    tags = readTlvStringList(),
                )
            }.getOrNull()

        fun fromByteString(byteString: String): JinrishiciDetailData? =
            byteString.decodeHex().let(::fromBytes)
    }
}
