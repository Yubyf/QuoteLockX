package com.crossbowffs.quotelock.app.font

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.WorkerThread
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.toFile
import com.yubyf.truetypeparser.TTFFile
import java.io.File
import java.io.IOException
import java.util.*

/**
 * @author Yubyf
 */
object FontManager {

    private val SYSTEM_CUSTOM_FONT_DIR = File("/system/fonts", "custom")
    private val INTERNAL_CUSTOM_FONT_DIR = File(App.instance.getExternalFilesDir(null), "fonts")
    private val INTERNAL_CUSTOM_FONT_PENDING_DIR = File(INTERNAL_CUSTOM_FONT_DIR, "pending")
    private val INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR =
        File(INTERNAL_CUSTOM_FONT_PENDING_DIR, "import")
    private val INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR =
        File(INTERNAL_CUSTOM_FONT_PENDING_DIR, "remove")
    private val SYSTEM_CUSTOM_FONT_STUB = File(SYSTEM_CUSTOM_FONT_DIR, ".stub")

    private val FONT_INFO_CACHE = HashMap<String, FontInfo>()
    private val TYPEFACE_CACHE = HashMap<String, Typeface>()

    private val TAG = className<FontManager>()

    fun checkSystemCustomFontAvailable(): Boolean {
        return SYSTEM_CUSTOM_FONT_STUB.exists()
    }

    @WorkerThread
    fun importFont(context: Context, fileUri: Uri): String? {
        return runCatching {
            if (!ensureInternalFontDir()) {
                throw IOException("Failed to create font directory")
            }
            val name = context.contentResolver.query(fileUri, null, null,
                null, null)?.use { cursor ->
                // Get the name of the font file
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: fileUri.lastPathSegment ?: throw IOException("Failed to get font name")
            val fontFile = File(INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR, name)
            context.contentResolver.openInputStream(fileUri)?.toFile(fontFile)
            fontFile.absolutePath
        }.onFailure {
            Xlog.e(TAG, "Failed to import font", it)
        }.getOrNull()
    }

    fun deleteActiveSystemFont(fileName: String): Boolean {
        if (!ensureInternalFontDir()) {
            return false
        }
        val fontFile = File(SYSTEM_CUSTOM_FONT_DIR, fileName)
        if (fontFile.exists()) {
            val pendingRemovedFile = File(INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR, fileName)
            pendingRemovedFile.createNewFile()
        }
        return true
    }

    fun deleteInactiveFont(fileName: String): Boolean {
        if (!ensureInternalFontDir()) {
            return false
        }
        val fontFile = File(INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR, fileName)
        if (fontFile.exists()) {
            return fontFile.delete()
        }
        return true
    }

    fun isFontActivated(fileName: String): Boolean {
        val fontFile = File(SYSTEM_CUSTOM_FONT_DIR, fileName)
        return fontFile.exists()
    }

    fun loadActiveFontFilesList(): List<File>? {
        val pendingRemoveFonts = INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.listFiles()
            ?.filter {
                it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true)
            }
        val systemCustomFonts = SYSTEM_CUSTOM_FONT_DIR.listFiles()?.filter {
            (it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true))
                    && pendingRemoveFonts?.find { pending -> pending.name == it.name } == null
        }?.sortedBy { it.lastModified() }
        return systemCustomFonts
    }

    fun loadFontsList(): List<FontInfoWithState>? {
        val allFonts = mutableListOf<FontInfoWithState>()
        val pendingRemoveFonts = INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.listFiles()
            ?.filter {
                it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true)
            }
        val systemCustomFonts = SYSTEM_CUSTOM_FONT_DIR.listFiles()?.filter {
            it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true)
        }?.sortedBy { it.lastModified() }?.map {
            loadFontInfo(it)?.let { fontInfo ->
                FontInfoWithState(fontInfo, systemFont = true,
                    active = pendingRemoveFonts?.find { pending -> pending.name == it.name } == null)
            } ?: return null
        }
        val pendingImportFonts = INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR.listFiles()
            ?.filter {
                it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true)
            }?.sortedBy { it.lastModified() }?.map {
                loadFontInfo(it)?.let { fontInfo ->
                    FontInfoWithState(fontInfo, systemFont = false, active = false)
                } ?: return null
            }
        return allFonts.apply {
            addAll(systemCustomFonts ?: emptyList())
            addAll(pendingImportFonts ?: emptyList())
        }.distinctBy { it.fontInfo.fileName }
    }

    fun loadFontInfo(file: File): FontInfo? {
        return runCatching {
            FONT_INFO_CACHE.getOrPut(file.absolutePath) {
                val ttfFile = TTFFile.open(file)
                FontInfo(ttfFile.getFullName(Locale.getDefault()),
                    file.name,
                    file.absolutePath).apply {
                    descriptionLocale = generateLocaleDescription(ttfFile.fullName)
                }
            }
        }.onFailure {
            Xlog.e(TAG, "Failed to parse font file: ${file.path}", it)
        }.getOrNull()
    }

    fun loadTypeface(fontPath: String): Typeface {
        return TYPEFACE_CACHE.getOrPut(fontPath) {
            Typeface.createFromFile(fontPath)
        }
    }

    private fun generateLocaleDescription(names: Map<String, String>): String {
        return when {
            names.containsKey(Locale.SIMPLIFIED_CHINESE.toLanguageTag()) ->
                FONT_DESCRIPTION_SIMPLIFIED_CHINESE
            names.containsKey(Locale.TRADITIONAL_CHINESE.toLanguageTag()) ->
                FONT_DESCRIPTION_TRADITIONAL_CHINESE
            else -> ""
        }
    }

    private fun ensureInternalFontDir(): Boolean {
        return (INTERNAL_CUSTOM_FONT_DIR.exists() || INTERNAL_CUSTOM_FONT_DIR.mkdirs())
                && (INTERNAL_CUSTOM_FONT_PENDING_DIR.exists()
                || INTERNAL_CUSTOM_FONT_PENDING_DIR.mkdirs())
                && (INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR.exists()
                || INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR.mkdirs())
                && (INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.exists()
                || INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.mkdirs())
    }
}

data class FontInfo(
    val name: String = "",
    val fileName: String = "",
    val path: String = "",
    val descriptionLatin: String = FONT_DESCRIPTION_LATIN,
    var descriptionLocale: String = FONT_DESCRIPTION_SIMPLIFIED_CHINESE,
)

data class FontInfoWithState(
    val fontInfo: FontInfo,
    var systemFont: Boolean = false,
    var active: Boolean = false,
)

private const val FONT_DESCRIPTION_LATIN = "Lorem ipsum dolor sit amet"
private const val FONT_DESCRIPTION_SIMPLIFIED_CHINESE = "落霞与孤鹜齐飞，秋水共长天一色"
private const val FONT_DESCRIPTION_TRADITIONAL_CHINESE = "落霞與孤鶩齊飛，秋水共長天一色"