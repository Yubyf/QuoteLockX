package com.crossbowffs.quotelock.app.font

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.getFontVariationSettings
import com.crossbowffs.quotelock.utils.loadComposeFont
import com.crossbowffs.quotelock.utils.toFile
import com.yubyf.quotelockx.R
import com.yubyf.truetypeparser.TTFFile
import com.yubyf.truetypeparser.get
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * @author Yubyf
 */
object FontManager {

    private val FONT_INFO_CACHE = HashMap<String, FontInfo>()
    private val TYPEFACE_CACHE = HashMap<String, Typeface>()

    private val TAG = className<FontManager>()

    private fun Array<File>.filterFontFiles() = filter { it.isFontFile() }

    private fun File.isFontFile(): Boolean {
        val name = name.lowercase(Locale.ROOT)
        return name.endsWith(".ttf") || name.endsWith(".otf")
    }

    fun loadAllFontsList(): List<FontInfo> {
        val pendingRemoveSystemFontFiles =
            INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR.listFiles()
                ?.filterFontFiles()
        val systemFontFiles = SYSTEM_CUSTOM_FONT_DIR.listFiles()?.filter {
            it.isFontFile() && pendingRemoveSystemFontFiles
                ?.find { pending -> pending.name == it.name } == null
        } ?: emptyList()
        val inAppFontFiles =
            INTERNAL_CUSTOM_FONT_DIR.listFiles()?.filterFontFiles() ?: emptyList()
        return (systemFontFiles + inAppFontFiles).sortedBy { it.lastModified() }.distinctBy {
            it.name to it.length()
        }.map {
            FONT_INFO_CACHE[it.absolutePath] ?: FontInfo(fileName = it.nameWithoutExtension,
                path = it.absolutePath)
        }
    }

    //region In-app font

    internal val INTERNAL_CUSTOM_FONT_DIR = File(App.instance.getExternalFilesDir(null), "fonts")

    internal fun ensureInAppFontDir(): Boolean =
        INTERNAL_CUSTOM_FONT_DIR.exists() || INTERNAL_CUSTOM_FONT_DIR.mkdirs()

    suspend fun loadInAppFontsList(): List<FontInfo>? =
        INTERNAL_CUSTOM_FONT_DIR.listFiles()?.filter { it.isFontFile() }
            ?.sortedBy { it.lastModified() }?.mapNotNull {
                loadFontInfo(it) ?: return null
            }?.distinctBy { it.fileName }

    fun deleteInAppFont(fileName: String): Boolean =
        File(INTERNAL_CUSTOM_FONT_DIR, fileName).let {
            if (it.exists()) it.delete() else true
        }
    //endregion

    //region System font
    private val SYSTEM_CUSTOM_FONT_DIR = File("/system/fonts", "custom")
    private val INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_DIR = File(INTERNAL_CUSTOM_FONT_DIR, "pending")
    internal val INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_IMPORT_DIR =
        File(INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_DIR, "import")
    private val INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR =
        File(INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_DIR, "remove")
    private val SYSTEM_CUSTOM_FONT_STUB = File(SYSTEM_CUSTOM_FONT_DIR, ".stub")

    fun checkSystemCustomFontAvailable(): Boolean {
        return SYSTEM_CUSTOM_FONT_STUB.exists()
    }

    internal fun ensureInternalFontDir(): Boolean =
        ensureInAppFontDir()
                && (INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_DIR.exists()
                || INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_DIR.mkdirs())
                && (INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_IMPORT_DIR.exists()
                || INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_IMPORT_DIR.mkdirs())
                && (INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR.exists()
                || INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR.mkdirs())

    fun deleteActiveSystemFont(fileName: String): Boolean {
        if (!ensureInternalFontDir()) {
            return false
        }
        val fontFile = File(SYSTEM_CUSTOM_FONT_DIR, fileName)
        if (fontFile.exists()) {
            val pendingRemovedFile = File(INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR, fileName)
            pendingRemovedFile.createNewFile()
        }
        return true
    }

    fun deleteInactiveSystemFont(fileName: String): Boolean {
        if (!ensureInternalFontDir()) {
            return false
        }
        val fontFile = File(INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_IMPORT_DIR, fileName)
        if (fontFile.exists()) {
            return fontFile.delete()
        }
        return true
    }

    fun isSystemFontActivated(fileName: String): Boolean =
        File(SYSTEM_CUSTOM_FONT_DIR, fileName).exists()

    fun loadActiveSystemFontsList(): List<FontInfo>? {
        val pendingRemoveFonts = INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR.listFiles()
            ?.filterFontFiles()
        return SYSTEM_CUSTOM_FONT_DIR.listFiles()?.filter {
            it.isFontFile() && pendingRemoveFonts?.find { pending -> pending.name == it.name } == null
        }?.sortedBy { it.lastModified() }?.map {
            FONT_INFO_CACHE[it.absolutePath] ?: FontInfo(fileName = it.nameWithoutExtension,
                path = it.absolutePath)
        }
    }

    suspend fun loadSystemFontsList(): List<FontInfoWithState>? {
        val allFonts = mutableListOf<FontInfoWithState>()
        val pendingRemoveFonts = INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_REMOVE_DIR.listFiles()
            ?.filterFontFiles()
        val systemCustomFonts =
            SYSTEM_CUSTOM_FONT_DIR.listFiles()?.filterFontFiles()?.sortedBy { it.lastModified() }
                ?.map {
                    loadFontInfo(it)?.let { fontInfo ->
                        FontInfoWithState(fontInfo, systemFont = true,
                            active = pendingRemoveFonts?.find { pending -> pending.name == it.name } == null)
                    } ?: return null
                }
        val pendingImportFonts = INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_IMPORT_DIR.listFiles()
            ?.filterFontFiles()?.sortedBy { it.lastModified() }?.map {
                loadFontInfo(it)?.let { fontInfo ->
                    FontInfoWithState(fontInfo, systemFont = false, active = false)
                } ?: return null
            }
        return allFonts.apply {
            addAll(systemCustomFonts ?: emptyList())
            addAll(pendingImportFonts ?: emptyList())
        }.distinctBy { it.fontInfo.fileName }
    }
    //endregion

    suspend fun loadFontInfo(file: File): FontInfo? = withContext(Dispatchers.IO) {
        runCatching {
            FONT_INFO_CACHE.getOrPut(file.absolutePath) {
                val ttfFile = TTFFile.open(file)
                FontInfo(families = ttfFile.families,
                    fileName = file.name,
                    path = file.absolutePath,
                    variable = ttfFile.variable,
                    variableWeight = ttfFile.variationAxes.firstOrNull { it.tag == "wght" }?.let {
                        FontInfo.VariableWeight(
                            it.minValue.roundToInt()..it.maxValue.roundToInt(),
                            it.defaultValue.roundToInt()
                        )
                    },
                    variableItalic = ttfFile.variationAxes.firstOrNull { it.tag == "ital" }?.let {
                        FontInfo.VariableItalic(
                            it.minValue..it.maxValue,
                            it.defaultValue
                        )
                    }
                ).apply {
                    descriptionLocale = generateLocaleDescription(families)
                }
            }
        }.onFailure {
            Xlog.e(TAG, "Failed to parse font file: ${file.path}", it)
        }.getOrNull()
    }

    fun loadTypeface(
        fontPath: String,
        weight: FontWeight = FontWeight.Normal,
        italic: Float = FontStyle.Normal.value.toFloat(),
    ): Typeface = runCatching {
        TYPEFACE_CACHE.getOrPut("$fontPath&${weight.weight}&$italic") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Typeface.Builder(File(fontPath))
                    .setFontVariationSettings(getFontVariationSettings(weight, italic))
                    .build()
            } else {
                Typeface.createFromFile(fontPath)
            }
        }
    }.onFailure {
        Xlog.e(TAG, "Failed to load typeface: $fontPath", it)
    }.getOrNull() ?: Typeface.DEFAULT

    private fun generateLocaleDescription(names: Map<String, String>): String {
        return when {
            names.containsKey(Locale.SIMPLIFIED_CHINESE.toLanguageTag()) ->
                FONT_DESCRIPTION_SIMPLIFIED_CHINESE
            names.containsKey(Locale.TRADITIONAL_CHINESE.toLanguageTag()) ->
                FONT_DESCRIPTION_TRADITIONAL_CHINESE
            else -> ""
        }
    }
}

@Singleton
class FontImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private fun Context.getFontNameFromUri(fileUri: Uri) =
        contentResolver.query(fileUri, null, null,
            null, null)?.use { cursor ->
            // Get the name of the font file
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: fileUri.lastPathSegment

    suspend fun importFontInApp(fileUri: Uri): AsyncResult<String> = withContext(dispatcher) {
        runCatching {
            if (!FontManager.ensureInAppFontDir()) {
                throw IOException("Failed to create font directory")
            }
            val name =
                context.getFontNameFromUri(fileUri) ?: throw IOException("Failed to get font name")
            val fontFile = File(FontManager.INTERNAL_CUSTOM_FONT_DIR, name)
            if (fontFile.exists()) {
                AsyncResult.Error(Exception(context.getString(
                    R.string.quote_fonts_management_font_already_exists, name)))
            } else {
                context.contentResolver.openInputStream(fileUri)?.toFile(fontFile)
                AsyncResult.Success(fontFile.absolutePath)
            }
        }.onFailure {
            Xlog.e(TAG, "Failed to import font", it)
        }.getOrDefault(AsyncResult.Error(Exception(context.getString(
            R.string.quote_fonts_management_import_failed))))
    }

    suspend fun importFontToSystem(fileUri: Uri): AsyncResult<String> = withContext(dispatcher) {
        runCatching {
            if (!FontManager.ensureInternalFontDir()) {
                throw IOException("Failed to create font directory")
            }
            val name =
                context.getFontNameFromUri(fileUri) ?: throw IOException("Failed to get font name")
            val fontFile = File(FontManager.INTERNAL_SYSTEM_CUSTOM_FONT_PENDING_IMPORT_DIR, name)
            context.contentResolver.openInputStream(fileUri)?.toFile(fontFile)
            AsyncResult.Success(fontFile.absolutePath)
        }.onFailure {
            Xlog.e(TAG, "Failed to import font", it)
        }.getOrDefault(AsyncResult.Error(Exception(context.getString(
            R.string.quote_fonts_management_import_failed))))
    }

    companion object {
        private val TAG = className<FontImporter>()
    }
}

data class FontInfo(
    val families: Map<String, String> = emptyMap(),
    val fileName: String = "",
    val path: String = "",
    val variable: Boolean = false,
    val variableWeight: VariableWeight? = null,
    val variableItalic: VariableItalic? = null,
    val descriptionLatin: String = FONT_DESCRIPTION_LATIN,
    var descriptionLocale: String = FONT_DESCRIPTION_SIMPLIFIED_CHINESE,
) {
    val cjk: Boolean
        get() = when {
            families.containsKey(Locale.SIMPLIFIED_CHINESE.toLanguageTag())
                    || families.containsKey(Locale.TRADITIONAL_CHINESE.toLanguageTag())
                    || families.containsKey(Locale.CHINESE.toLanguageTag())
                    || families.containsKey(Locale.KOREAN.toLanguageTag())
                    || families.containsKey(Locale.JAPANESE.toLanguageTag()) ->
                true

            else -> false
        }

    val supportVariableWeight: Boolean
        get() = variable && variableWeight != null

    val supportVariableItalic: Boolean
        get() = variable && variableItalic != null

    fun composeFontInStyle(
        weight: FontWeight = FontWeight.Normal,
        style: FontStyle = FontStyle.Normal,
    ) = loadComposeFont(path, weight, style)

    @Suppress("DEPRECATION")
    val Configuration.localeName: String
        get() = families[if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0] else locale]

    data class VariableWeight(
        val range: IntRange,
        val default: Int,
    )

    data class VariableItalic(
        val range: ClosedFloatingPointRange<Float>,
        val default: Float,
    )
}

data class FontInfoWithState(
    val fontInfo: FontInfo,
    var systemFont: Boolean = false,
    var active: Boolean = false,
)

private const val FONT_DESCRIPTION_LATIN = "Lorem ipsum dolor sit amet"
private const val FONT_DESCRIPTION_SIMPLIFIED_CHINESE = "落霞与孤鹜齐飞，秋水共长天一色"
private const val FONT_DESCRIPTION_TRADITIONAL_CHINESE = "落霞與孤鶩齊飛，秋水共長天一色"