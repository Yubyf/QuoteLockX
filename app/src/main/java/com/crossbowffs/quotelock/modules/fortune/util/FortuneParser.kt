//package com.crossbowffs.quotelock.modules.fortune.util
//
//import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteEntity
//import com.crossbowffs.quotelock.modules.fortune.database.FortuneQuoteEntity
//import com.crossbowffs.quotelock.modules.fortune.database.fortuneQuoteDatabase
//import com.crossbowffs.quotelock.utils.Xlog
//import com.crossbowffs.quotelock.utils.md5
//import java.io.File
//import java.io.IOException
//import java.io.InputStream
//
///**
// * Parses the fortune cookies from txt files to a database.
// * For custom import.
// *
// * @author Yubyf
// */
//
//private const val TAG = "FortuneParser"
//private const val FORTUNE_DELIMITER_TEMPLATE = "^\\s*%\\s*\$"
//private const val FORTUNE_FOOTER_TEMPLATE = "^\\s*%%\\s*\$"
//private const val FORTUNE_SOURCE_TEMPLATE = "^\\t{2,}-{2,}\\s*(.+)$"
//
//@Throws(IOException::class)
//internal suspend fun importFortune(file: File) =
//    importFortune(file.inputStream(), file.nameWithoutExtension)
//
//@Throws(IOException::class)
//internal suspend fun importFortune(stream: InputStream, category: String) =
//    stream.bufferedReader().useLines { lines ->
//        val footerRegex = FORTUNE_FOOTER_TEMPLATE.toRegex()
//        val delimiterRegex = FORTUNE_DELIMITER_TEMPLATE.toRegex()
//        val sourceRegex = FORTUNE_SOURCE_TEMPLATE.toRegex()
//        val text = StringBuilder()
//        val source = StringBuilder()
//        lines.forEach { line ->
//            when {
//                footerRegex.matches(line) -> {
//                    Xlog.d(TAG, "Found fortune footer")
//                }
//                delimiterRegex.matches(line) -> {
//                    Xlog.d(TAG, "Found fortune\nlength ${text.length}\n$text\n ---------- $source")
//                    fortuneQuoteDatabase.dao().insert(
//                        FortuneQuoteEntity(
//                        text = text.toString(),
//                        source = source.toString(),
//                        md5 = ("$text$source").md5(),
//                        author = "",
//                        category = category,
//                    )
//                    )
//                    // Reset
//                    text.clear()
//                    source.clear()
//                }
//                else -> {
//                    sourceRegex.find(line)?.let { match ->
//                        match.groupValues[1].let { group ->
//                            if (group.isNotBlank()) {
//                                source.clear().append(group.trim())
//                            }
//                        }
//                    } ?: line.let {
//                        if (it.isNotBlank()) {
//                            text.apply { if (isNotBlank()) append(" ") }.append(it.trim())
//                        }
//                    }
//                }
//            }
//        }
//        if (text.isNotBlank()) {
//            Xlog.d(TAG,
//                "Found fortune\nlength ${text.length}\n$text\n ---------- $source")
//            fortuneQuoteDatabase.dao().insert(FortuneQuoteEntity(
//                text = text.toString(),
//                source = source.toString(),
//                md5 = ("$text$source").md5(),
//                author = "",
//                category = category,
//            ))
//            // Reset
//            text.clear()
//            source.clear()
//        }
//    }