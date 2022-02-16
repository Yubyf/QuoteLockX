package com.crossbowffs.quotelock.app

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.dp2px
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreviewFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    private lateinit var mQuotesPreferences: SharedPreferences
    private var mPreviewPreference: PreviewPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PREF_COMMON
        setPreferencesFromResource(R.xml.preview_preference, rootKey)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        mQuotesPreferences =
            requireContext().getSharedPreferences(PREF_QUOTES, Context.MODE_PRIVATE)
        mQuotesPreferences.registerOnSharedPreferenceChangeListener(this)

        mPreviewPreference = findPreference<Preference>("pref_preview") as? PreviewPreference
        mPreviewPreference?.run {
            quote = mQuotesPreferences.getString(PREF_QUOTES_TEXT, "")
            source = mQuotesPreferences.getString(PREF_QUOTES_SOURCE, "")
            quoteSize = sharedPreferences.getString(PREF_COMMON_FONT_SIZE_TEXT,
                PREF_COMMON_FONT_SIZE_TEXT_DEFAULT)!!.toFloat()
            sourceSize = sharedPreferences.getString(PREF_COMMON_FONT_SIZE_SOURCE,
                PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT)!!.toFloat()
            // Font properties
            val quoteStyles = sharedPreferences.getStringSet(PREF_COMMON_FONT_STYLE_TEXT, null)
            val sourceStyles = sharedPreferences.getStringSet(PREF_COMMON_FONT_STYLE_SOURCE, null)
            val quoteStyle = getTypefaceStyle(quoteStyles)
            val sourceStyle = getTypefaceStyle(sourceStyles)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val font = sharedPreferences.getString(
                    PREF_COMMON_FONT_FAMILY, PREF_COMMON_FONT_FAMILY_DEFAULT)
                if (PREF_COMMON_FONT_FAMILY_DEFAULT != font) {
                    val fontId = requireContext().resources.getIdentifier(font, "font",
                        requireContext().packageName)
                    val typeface = ResourcesCompat.getFont(requireContext(), fontId)
                    quoteTypeface = typeface
                    this.quoteStyle = quoteStyle
                    sourceTypeface = typeface
                    this.sourceStyle = sourceStyle
                } else {
                    quoteTypeface = null
                    this.quoteStyle = quoteStyle
                    sourceTypeface = null
                    this.sourceStyle = sourceStyle
                }
            } else {
                quoteTypeface = null
                this.quoteStyle = quoteStyle
                sourceTypeface = null
                this.sourceStyle = sourceStyle
            }

            // Layout padding
            paddingTop = sharedPreferences.getString(
                PREF_COMMON_PADDING_TOP, PREF_COMMON_PADDING_TOP_DEFAULT)!!.toFloat().dp2px()
                .toInt()
            paddingBottom = sharedPreferences.getString(
                PREF_COMMON_PADDING_BOTTOM, PREF_COMMON_PADDING_BOTTOM_DEFAULT)!!.toFloat().dp2px()
                .toInt()
        }
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        mQuotesPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Xlog.i(TAG, "Preference changed: %s", key)
        if (sharedPreferences == mQuotesPreferences) {
            if (PREF_QUOTES_TEXT == key) {
                (findPreference<Preference>("pref_preview") as? PreviewPreference)?.quote =
                    sharedPreferences.getString(PREF_QUOTES_TEXT, "")
            } else if (PREF_QUOTES_SOURCE == key) {
                (findPreference<Preference>("pref_preview") as? PreviewPreference)?.source =
                    sharedPreferences.getString(PREF_QUOTES_SOURCE, "")
            }
            return
        } else if (sharedPreferences == preferenceManager.sharedPreferences) {
            when (key) {
                PREF_COMMON_FONT_SIZE_TEXT -> mPreviewPreference?.quoteSize =
                    sharedPreferences.getString(PREF_COMMON_FONT_SIZE_TEXT,
                        PREF_COMMON_FONT_SIZE_TEXT_DEFAULT)!!.toFloat()
                PREF_COMMON_FONT_SIZE_SOURCE -> mPreviewPreference?.sourceSize =
                    sharedPreferences.getString(PREF_COMMON_FONT_SIZE_SOURCE,
                        PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT)!!.toFloat()
                PREF_COMMON_FONT_FAMILY -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val font = sharedPreferences.getString(
                            PREF_COMMON_FONT_FAMILY, PREF_COMMON_FONT_FAMILY_DEFAULT)
                        if (PREF_COMMON_FONT_FAMILY_DEFAULT != font) {
                            val fontId = requireContext().resources.getIdentifier(font,
                                "font", requireContext().packageName)
                            mPreviewPreference?.quoteTypeface =
                                ResourcesCompat.getFont(requireContext(), fontId)
                            mPreviewPreference?.sourceTypeface =
                                ResourcesCompat.getFont(requireContext(), fontId)
                        } else {
                            mPreviewPreference?.quoteTypeface = null
                            mPreviewPreference?.sourceTypeface = null
                        }
                    } else {
                        mPreviewPreference?.quoteTypeface = null
                        mPreviewPreference?.sourceTypeface = null
                    }
                }
                PREF_COMMON_FONT_STYLE_TEXT -> {
                    val quoteStyles = sharedPreferences
                        .getStringSet(PREF_COMMON_FONT_STYLE_TEXT, null)
                    mPreviewPreference?.quoteStyle = getTypefaceStyle(quoteStyles)
                }
                PREF_COMMON_FONT_STYLE_SOURCE -> {
                    val sourceStyles = sharedPreferences
                        .getStringSet(PREF_COMMON_FONT_STYLE_SOURCE, null)
                    mPreviewPreference?.sourceStyle = getTypefaceStyle(sourceStyles)
                }
                PREF_COMMON_PADDING_TOP -> mPreviewPreference?.paddingTop =
                    sharedPreferences.getString(
                        PREF_COMMON_PADDING_TOP, PREF_COMMON_PADDING_TOP_DEFAULT)!!.toFloat()
                        .dp2px()
                        .toInt()
                PREF_COMMON_PADDING_BOTTOM -> mPreviewPreference?.paddingBottom =
                    sharedPreferences.getString(
                        PREF_COMMON_PADDING_BOTTOM, PREF_COMMON_PADDING_BOTTOM_DEFAULT)!!.toFloat()
                        .dp2px()
                        .toInt()
                else -> {}
            }
        }
    }

    companion object {
        private val TAG = className<PreviewFragment>()
    }
}

class PreviewPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet,
    defStyleAttr: Int = 0, defStyleRes: Int = 0,
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    class Setter<T>(private var value: T) : ReadWriteProperty<PreviewPreference, T> {
        override fun getValue(thisRef: PreviewPreference, property: KProperty<*>): T {
            return value
        }

        override fun setValue(thisRef: PreviewPreference, property: KProperty<*>, value: T) {
            this.value = value
            if (thisRef.isShown) {
                thisRef.notifyChanged()
            }
        }
    }

    var quote: String? by Setter(null)

    var source: String? by Setter(null)

    var quoteSize: Float by Setter(PREF_COMMON_FONT_SIZE_TEXT_DEFAULT.toFloat())

    var sourceSize: Float by Setter(PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT.toFloat())

    var quoteTypeface: Typeface? by Setter(null)

    var quoteStyle: Int by Setter(Typeface.NORMAL)

    var sourceTypeface: Typeface? by Setter(null)

    var sourceStyle: Int by Setter(Typeface.NORMAL)

    var paddingTop: Int by Setter(PREF_COMMON_PADDING_TOP_DEFAULT.toInt())

    var paddingBottom: Int by Setter(PREF_COMMON_PADDING_BOTTOM_DEFAULT.toInt())

    init {
        layoutResource = R.layout.quote_preview_layout
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val quoteContainer = holder.itemView
        val quoteTextView = holder.findViewById(R.id.quote_textview) as? TextView
        val quoteSourceView = holder.findViewById(R.id.source_textview) as? TextView
        quoteContainer.let {
            it.setPadding(it.paddingStart, paddingTop, it.paddingEnd, paddingBottom)
        }
        quoteTextView?.run {
            text = quote
            setTextSize(TypedValue.COMPLEX_UNIT_SP, quoteSize)
            setTypeface(quoteTypeface, quoteStyle)
        }
        quoteSourceView?.run {
            text = source
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sourceSize)
            setTypeface(sourceTypeface, sourceStyle)
        }
    }
}

fun getTypefaceStyle(styles: Set<String>?): Int {
    var style = Typeface.NORMAL
    if (styles != null) {
        if (styles.contains("bold") && styles.contains("italic")) {
            style = Typeface.BOLD_ITALIC
        } else if (styles.contains("bold")) {
            style = Typeface.BOLD
        } else if (styles.contains("italic")) {
            style = Typeface.ITALIC
        }
    }
    return style
}