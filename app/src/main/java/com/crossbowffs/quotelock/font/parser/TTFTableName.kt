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
/* $Id: TTFTableName.java 1357883 2012-07-05 20:29:53Z gadams $ */
package com.crossbowffs.quotelock.font.parser

/**
 * Represents table names as found in a TrueType font's Table Directory. TrueType fonts may have
 * custom tables so we cannot use an enum.
 */
class TTFTableName private constructor(
    /**
     * The name of the table as it should be in the Directory Table.
     */
    val name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is TTFTableName) {
            return false
        }
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }

    companion object {
        /** The first table in a TrueType font file containing metadata about other tables.  */
        val TABLE_DIRECTORY = TTFTableName("tableDirectory")

        /** Naming table.  */
        val NAME = TTFTableName("name")

        /** OS/2 and Windows specific metrics.  */
        val OS2 = TTFTableName("OS/2")

        /**
         * Returns an instance of this class corresponding to the given string representation.
         *
         * @param tableName
         * table name as in the Table Directory
         * @return TTFTableName
         */
        fun getValue(tableName: String?): TTFTableName {
            if (tableName != null) {
                return TTFTableName(tableName)
            }
            throw IllegalArgumentException("A TrueType font table name must not be null")
        }
    }
}