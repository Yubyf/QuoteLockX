/*
 * Modified by Yubyf on 2022:
 * Copyright (c) 2022 Yubyf <lpy19920505@gmail.com>
 *
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.crossbowffs.quotelock.font.parser

import java.io.*
import java.nio.charset.Charset

/**
 * Reads a TrueType font file into a byte array and provides file like functions for array access.
 */
class FontFileReader : Closeable {
    /**
     * The size of the file.
     */
    var fileSize = 0
        private set

    /**
     * Current position in file
     */
    var currentPos = 0
        private set

    private lateinit var fontStream: InputStream

    /**
     * Constructor
     *
     * @param inputStream InputStream to read from
     * @throws IOException In case of an I/O problem
     */
    constructor(inputStream: InputStream) {
        init(inputStream)
    }

    /**
     * Constructor
     *
     * @param path file to read
     * @throws IOException In case of an I/O problem
     */
    constructor(path: String?) {
        init(FileInputStream(path))
    }

    /**
     * Initializes class and reads stream. Init does not close stream.
     *
     * @param inputStream InputStream to read from new array with size + inc
     * @throws IOException In case of an I/O problem
     */
    @Throws(IOException::class)
    private fun init(inputStream: InputStream) {
        fontStream = inputStream
        fileSize = inputStream.available()
        if (fontStream.markSupported()) {
            fontStream.mark(-1)
        }
        currentPos = 0
    }

    /**
     * Read 1 byte.
     *
     * @return One byte
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    private fun read(): Byte {
        if (currentPos >= fileSize) {
            throw EOFException("Reached EOF, file size=$fileSize")
        }
        currentPos++
        return fontStream.read().toByte()
    }

    @Throws(IOException::class)
    fun read(bytes: ByteArray): Int {
        if (fileSize - currentPos <= bytes.size) {
            throw EOFException("Reached EOF, file size=$fileSize")
        }
        val result = fontStream.read(bytes)
        currentPos += bytes.size
        return result
    }

    /**
     * Read 1 signed byte.
     *
     * @return One byte
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFByte(): Byte {
        return read()
    }

    /**
     * Read 4 bytes.
     *
     * @return One signed integer
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFLong(): Int {
        var ret = readTTFUByte().toLong() // << 8;
        ret = (ret shl 8) + readTTFUByte()
        ret = (ret shl 8) + readTTFUByte()
        ret = (ret shl 8) + readTTFUByte()
        return ret.toInt()
    }

    /**
     * Read an ISO-8859-1 string of len bytes.
     *
     * @param len The length of the string to read
     * @return A String
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFString(len: Int): String {
        if ((len + currentPos) > fileSize) {
            throw EOFException("Reached EOF, file size=$fileSize")
        }
        val tmp = ByteArray(len)
        fontStream.read(tmp, 0, len)
        currentPos += len
        val encoding: Charset = if (tmp.isNotEmpty() && tmp[0] == 0.toByte()) {
            Charsets.UTF_16BE
        } else {
            Charsets.ISO_8859_1
        }
        return String(tmp, encoding)
    }

    /**
     * Read an ISO-8859-1 string of len bytes.
     *
     * @param len        The length of the string to read
     * @param encodingID the string encoding id (presently ignored; always uses UTF-16BE)
     * @return A String
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFString(len: Int, encodingID: Int): String {
        if ((len + currentPos) > fileSize) {
            throw EOFException("Reached EOF, file size=$fileSize")
        }
        val tmp = ByteArray(len)
        fontStream.read(tmp, 0, len)
        currentPos += len
        // Use this for all known encoding IDs for now
        val encoding = Charsets.UTF_16BE
        return String(tmp, encoding)
    }

    /**
     * Read 1 unsigned byte.
     *
     * @return One unsigned byte
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFUByte(): Int {
        val buf = read()
        return if (buf < 0) {
            256 + buf
        } else {
            buf.toInt()
        }
    }

    /**
     * Read 4 bytes.
     *
     * @return One unsigned integer
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFULong(): Long {
        var ret = readTTFUByte().toLong()
        ret = (ret shl 8) + readTTFUByte()
        ret = (ret shl 8) + readTTFUByte()
        ret = (ret shl 8) + readTTFUByte()
        return ret
    }

    /**
     * Read 2 bytes unsigned.
     *
     * @return One unsigned short
     * @throws IOException If EOF is reached
     */
    @Throws(IOException::class)
    fun readTTFUShort(): Int {
        return (readTTFUByte() shl 8) + readTTFUByte()
    }

    /**
     * Set current file position to offset
     *
     * @param offset The new offset to set
     * @throws IOException In case of an I/O problem
     */
    @Throws(IOException::class)
    fun seekSet(offset: Long) {
        if (offset > fileSize || offset < 0) {
            throw EOFException("Reached EOF, file size=$fileSize offset=$offset")
        }
        if (offset > currentPos) {
            currentPos += fontStream.skip(offset - currentPos).toInt()
        } else if (fontStream.markSupported()) {
            fontStream.reset()
            currentPos = fontStream.skip(offset).toInt()
        }
    }

    /**
     * Skip a given number of bytes.
     *
     * @param add The number of bytes to advance
     * @throws IOException In case of an I/O problem
     */
    @Throws(IOException::class)
    fun skip(add: Long) {
        if ((add + currentPos) > fileSize || add < 0) {
            throw EOFException("Reached EOF, file size=$fileSize offset=$add")
        }
        fontStream.skip(add)
        currentPos += add.toInt()
    }

    override fun close() {
        fontStream.close()
    }
}