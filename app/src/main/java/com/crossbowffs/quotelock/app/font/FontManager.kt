package com.crossbowffs.quotelock.app.font

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.toFile
import com.yubyf.truetypeparser.TTFFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Yubyf
 */
object FontManager {

    private val SYSTEM_CUSTOM_FONT_DIR = File("/system/fonts", "custom")
    private val INTERNAL_CUSTOM_FONT_DIR = File(App.instance.getExternalFilesDir(null), "fonts")
    private val INTERNAL_CUSTOM_FONT_PENDING_DIR = File(INTERNAL_CUSTOM_FONT_DIR, "pending")
    internal val INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR =
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

    fun loadActiveFontsList(): List<FontInfo>? {
        val pendingRemoveFonts = INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.listFiles()
            ?.filter {
                it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true)
            }
        val systemCustomFonts = SYSTEM_CUSTOM_FONT_DIR.listFiles()?.filter {
            (it.name.endsWith(".ttf", true) || it.name.endsWith(".otf", true))
                    && pendingRemoveFonts?.find { pending -> pending.name == it.name } == null
        }?.sortedBy { it.lastModified() }?.map {
            FontInfo(fileName = it.nameWithoutExtension, path = it.absolutePath)
        }
        return systemCustomFonts
    }

    suspend fun loadFontsList(): List<FontInfoWithState>? {
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

    suspend fun loadFontInfo(file: File): FontInfo? = withContext(Dispatchers.IO) {
        runCatching {
            FONT_INFO_CACHE.getOrPut(file.absolutePath) {
                val ttfFile = TTFFile.open(file)
                FontInfo(ttfFile.fullName.toMap(),
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

    internal fun ensureInternalFontDir(): Boolean {
        return (INTERNAL_CUSTOM_FONT_DIR.exists() || INTERNAL_CUSTOM_FONT_DIR.mkdirs())
                && (INTERNAL_CUSTOM_FONT_PENDING_DIR.exists()
                || INTERNAL_CUSTOM_FONT_PENDING_DIR.mkdirs())
                && (INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR.exists()
                || INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR.mkdirs())
                && (INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.exists()
                || INTERNAL_CUSTOM_FONT_PENDING_REMOVE_DIR.mkdirs())
    }
}

@Singleton
class FontImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun importFont(fileUri: Uri): String? = withContext(dispatcher) {
        runCatching {
            if (!FontManager.ensureInternalFontDir()) {
                throw IOException("Failed to create font directory")
            }
            val name = context.contentResolver.query(fileUri, null, null,
                null, null)?.use { cursor ->
                // Get the name of the font file
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: fileUri.lastPathSegment ?: throw IOException("Failed to get font name")
            val fontFile = File(FontManager.INTERNAL_CUSTOM_FONT_PENDING_IMPORT_DIR, name)
            context.contentResolver.openInputStream(fileUri)?.toFile(fontFile)
            fontFile.absolutePath
        }.onFailure {
            Xlog.e(TAG, "Failed to import font", it)
        }.getOrNull()
    }

    companion object {
        private val TAG = className<FontImporter>()
    }
}

data class FontInfo(
    val name: Map<String, String> = emptyMap(),
    val fileName: String = "",
    val path: String = "",
    val descriptionLatin: String = FONT_DESCRIPTION_LATIN,
    var descriptionLocale: String = FONT_DESCRIPTION_SIMPLIFIED_CHINESE,
) {
    fun typeface() = FontManager.loadTypeface(path)

    val Configuration.localeName: String
        get() = getValueOrFallbackByLocale(name,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0] else locale)
}

/**
 * Copy from [TTFFile] lib.
 * TODO: To delete
 */
private fun getValueOrFallbackByLocale(map: Map<String, String>, locale: Locale): String =
    if (map.isEmpty()) ""
    else map[locale.toLanguageTag()]
        ?: map.entries.firstOrNull { entry ->
            Locale.forLanguageTag(entry.key).language == locale.language
        }?.value
        ?: map[Locale.US.toLanguageTag()]
        ?: map.entries.firstOrNull { entry ->
            Locale.forLanguageTag(entry.key).language == Locale.ENGLISH.language
        }?.value
        ?: map[Locale.ROOT.toLanguageTag()]
        ?: map.values.firstOrNull()
        ?: ""

data class FontInfoWithState(
    val fontInfo: FontInfo,
    var systemFont: Boolean = false,
    var active: Boolean = false,
)

private const val FONT_DESCRIPTION_LATIN = "Lorem ipsum dolor sit amet"
private const val FONT_DESCRIPTION_SIMPLIFIED_CHINESE = "落霞与孤鹜齐飞，秋水共长天一色"
private const val FONT_DESCRIPTION_TRADITIONAL_CHINESE = "落霞與孤鶩齊飛，秋水共長天一色"