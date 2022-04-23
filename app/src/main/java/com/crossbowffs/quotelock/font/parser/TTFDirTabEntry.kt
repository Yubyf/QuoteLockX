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
/* $Id: TTFDirTabEntry.java 1357883 2012-07-05 20:29:53Z gadams $ */
package com.crossbowffs.quotelock.font.parser

import java.io.IOException
import java.io.UnsupportedEncodingException

/**
 * This class represents an entry to a TrueType font's Dir Tab.
 */
class TTFDirTabEntry {
    /**
     * Returns the tag bytes.
     *
     * @return byte[]
     */
    val tag = ByteArray(4)

    /**
     * Returns the offset.
     *
     * @return long
     */
    var offset: Long = 0
        private set

    /**
     * Returns the length.
     *
     * @return long
     */
    var length: Long = 0
        private set

    internal constructor()
    constructor(offset: Long, length: Long) {
        this.offset = offset
        this.length = length
    }

    /**
     * Returns the tag bytes.
     *
     * @return byte[]
     */
    val tagString: String
        get() = try {
            String(tag, Charsets.ISO_8859_1)
        } catch (e: UnsupportedEncodingException) {
            // Should never happen.
            toString()
        }

    /**
     * Read Dir Tab.
     *
     * @param reader font file reader
     * @return tag name
     * @throws IOException upon I/O exception
     */
    @Throws(IOException::class)
    fun read(reader: FontFileReader): String {
        tag[0] = reader.readTTFByte()
        tag[1] = reader.readTTFByte()
        tag[2] = reader.readTTFByte()
        tag[3] = reader.readTTFByte()
        reader.skip(4) // Skip checksum
        offset = reader.readTTFULong()
        length = reader.readTTFULong()
        return String(tag, Charsets.ISO_8859_1)
    }
}