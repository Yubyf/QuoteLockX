package com.crossbowffs.quotelock.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteDataTest {

    private val quote = QuoteData(
        "落霞与孤鹜齐飞，秋水共长天一色",
        "《滕王阁序》",
        "王勃",
        "jinrishici",
        "**************",
        byteArrayOf(0x00, 0x00, 0x00, 0x2d)
    )

    private val byteString =
        "0000002de890bde99c9ee4b88ee5ada4e9b99ce9bd90e9a39eefbc8ce7a78be6b0b4e585b1e995bfe5a4" +
                "a9e4b880e889b200000012e3808ae6bb95e78e8be99881e5ba8fe3808b00000006e78e8be58b83" +
                "0000000a6a696e726973686963690000000e2a2a2a2a2a2a2a2a2a2a2a2a2a2a000000040000002d"

    @Test
    fun testByteString() {
        assertEquals(byteString, quote.byteString)
    }

    @Test
    fun testFromByteString() {
        assertEquals(quote, QuoteData.fromByteString(byteString))
    }
}