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
/* $Id: TTFFile.java 1395925 2012-10-09 09:13:18Z jeremias $ */
package com.crossbowffs.quotelock.font.parser

import java.io.*
import java.util.*

/**
 * Reads a TrueType file or a TrueType Collection. The TrueType spec can be found at the Microsoft.
 * Typography site: http://www.microsoft.com/truetype/
 */
class TTFFile internal constructor() {

    /**
     * The font family names of the font.
     */
    val familyNames: MutableSet<String> = mutableSetOf()

    /**
     * The prefer font family names of the font.
     */
    var preferFamilyName: String = ""
        private set

    /**
     * Table directory
     */
    private val dirTabs: MutableMap<TTFTableName, TTFDirTabEntry?> = mutableMapOf()

    /**
     * The PostScript name of the font.
     */
    private var postScriptName = ""

    /**
     * The full name of the font.
     */
    val fullNames: MutableMap<String, String> = mutableMapOf()

    var notice = ""
        private set

    /**
     * Returns the font sub family name of the font.
     *
     * @return String The sub family name
     */
    var subFamilyName = ""
        private set

    /**
     * The weight class of this font. Valid values are 100, 200....,800, 900
     * or 0 if there was no OS/2 table in the font
     */
    var weightClass = 0
        private set

    /**
     * Read Table Directory from the current position in the FontFileReader and fill the global
     * HashMap dirTabs with the table name (String) as key and a TTFDirTabEntry as value.
     *
     * @throws IOException in case of an I/O problem
     */
    @Throws(IOException::class)
    private fun readDirTabs(reader: FontFileReader) {
        reader.readTTFLong() // TTF_FIXED_SIZE (4 bytes)
        val ntabs = reader.readTTFUShort()
        reader.skip(6) // 3xTTF_USHORT_SIZE
        val pd = arrayOfNulls<TTFDirTabEntry>(ntabs)
        for (i in 0 until ntabs) {
            pd[i] = TTFDirTabEntry()
            val tableName = pd[i]!!.read(reader)
            dirTabs[TTFTableName.getValue(tableName)] = pd[i]
        }
        dirTabs[TTFTableName.TABLE_DIRECTORY] = TTFDirTabEntry(0L, reader.currentPos
            .toLong())
    }

    /**
     * Reads the font using a FontFileReader.
     *
     * @param reader The FontFileReader to use
     * @throws IOException In case of an I/O problem
     */
    @Throws(IOException::class)
    fun readFont(reader: FontFileReader?) {
        reader?.use {
            readDirTabs(reader)
            val nameEntry = dirTabs[TTFTableName.NAME]
            val os2Entry = dirTabs[TTFTableName.OS2]
            when {
                nameEntry != null && os2Entry != null -> {
                    if (nameEntry.offset > os2Entry.offset) {
                        readWeight(reader)
                        readName(reader)
                    } else {
                        readName(reader)
                        readWeight(reader)
                    }
                }
                nameEntry != null -> {
                    readName(reader)
                }
                os2Entry != null -> {
                    readWeight(reader)
                }
            }
        } ?: throw IOException("FontFileReader is null")
    }

    /**
     * Read weight form the "OS/2" table.
     * https://developer.apple.com/fonts/TrueType-Reference-Manual/RM06/Chap6name.html
     *
     * @throws IOException In case of a I/O problem
     */
    @Throws(IOException::class)
    private fun readWeight(reader: FontFileReader) {
        seekTab(reader, TTFTableName.OS2, 0)
        reader.readTTFUShort()
        reader.skip(2) //xAvgCharWidth
        weightClass = reader.readTTFUShort()
    }

    /**
     * Read the "name" table.
     * https://developer.apple.com/fonts/TrueType-Reference-Manual/RM06/Chap6name.html
     *
     * @throws IOException In case of a I/O problem
     */
    @Throws(IOException::class)
    private fun readName(reader: FontFileReader) {
        seekTab(reader, TTFTableName.NAME, 2)
        val nameCount = reader.readTTFUShort()
        val stringOffset = reader.readTTFUShort()
        val table = ByteArray(dirTabs[TTFTableName.NAME]!!.length.toInt())
        reader.read(table)
        val nameReader = FontFileReader(ByteArrayInputStream(table))
        for (j in 0 until nameCount) {
            nameReader.seekSet(j * 6 * 2L)
            val platformId = nameReader.readTTFUShort()
            val encodingId = nameReader.readTTFUShort()
            val languageId = nameReader.readTTFUShort()
            val nameId = nameReader.readTTFUShort()
            val nameStringLength = nameReader.readTTFUShort()
            val nameStringOffset = nameReader.readTTFUShort()
            if ((platformId == 1 || platformId == 3) && (encodingId == 0 || encodingId == 1)) {
                nameReader.seekSet((stringOffset - 2 * 3 + nameStringOffset).toLong())
                val txt: String = if (platformId == 3) {
                    nameReader.readTTFString(nameStringLength, encodingId)
                } else {
                    nameReader.readTTFString(nameStringLength)
                }
                when (nameId) {
                    0 -> if (notice.isEmpty()) {
                        notice = txt
                    }
                    // Font Family Name
                    1 -> familyNames.add(txt)
                    2 -> if (subFamilyName.isEmpty()) {
                        subFamilyName = txt
                    }
                    4 -> if (platformId == 3 && languageId == 1033 || (platformId == 1 && languageId == 0)) {
                        fullNames[Locale.ENGLISH.language] = txt
                    } else if (platformId == 3 && languageId == 2052 || (platformId == 1 && languageId == 33)
                        || (platformId == 3 && languageId == 1028) || (platformId == 1 && languageId == 19)
                    ) {
                        fullNames[Locale.CHINESE.language] = txt
                    } else if (fullNames.isEmpty()) {
                        fullNames[Locale.ENGLISH.language] = txt
                    }
                    6 -> if (postScriptName.isEmpty()) {
                        postScriptName = txt
                    }
                    // Preferred Family
                    16 -> preferFamilyName = txt
                    else -> {}
                }
            }
        }
    }

    /**
     * Position [InputStream] to position indicated in the dirtab offset + offset
     *
     * @param reader        font file reader
     * @param tableName (tag) of table
     * @param offset    from start of table
     * @return true if seek succeeded
     * @throws IOException if I/O exception occurs during seek
     */
    @Throws(IOException::class)
    private fun seekTab(reader: FontFileReader, tableName: TTFTableName, offset: Long): Boolean {
        val dt = dirTabs[tableName]
        if (dt == null) {
            return false
        } else {
            reader.seekSet(dt.offset + offset)
        }
        return true
    }

    companion object {
        /**
         * Reads a TTF file
         *
         * @param file The font file
         * @return The TrueType file
         * @throws IOException if an IO error occurs
         */
        @Throws(IOException::class)
        fun open(file: File): TTFFile {
            return open(FileInputStream(file))
        }

        /**
         * Reads a TTF file from an InputStream
         *
         * @param inputStream InputStream to read from
         * @return The TrueType file
         * @throws IOException if an IO error occurs
         */
        @Throws(IOException::class)
        fun open(inputStream: InputStream): TTFFile {
            val ttfFile = TTFFile()
            ttfFile.readFont(FontFileReader(inputStream))
            return ttfFile
        }
    }
}