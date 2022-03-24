package com.crossbowffs.quotelock.app

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.commonDataStore
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.dp2px
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreviewFragment : PreferenceFragmentCompat() {

    private var mPreviewPreference: PreviewPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = commonDataStore
        setPreferencesFromResource(R.xml.preview_preference, rootKey)
        mPreviewPreference = findPreference<Preference>(PREF_PREVIEW) as? PreviewPreference
        observePreferences()
        mPreviewPreference?.run {
            quote = quotesDataStore.getString(PREF_QUOTES_TEXT, "")
            source = quotesDataStore.getString(PREF_QUOTES_SOURCE, "")
            quoteSize = commonDataStore.getString(PREF_COMMON_FONT_SIZE_TEXT,
                PREF_COMMON_FONT_SIZE_TEXT_DEFAULT)!!.toFloat()
            sourceSize = commonDataStore.getString(PREF_COMMON_FONT_SIZE_SOURCE,
                PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT)!!.toFloat()
            // Font properties
            val quoteStyles = commonDataStore.getStringSet(PREF_COMMON_FONT_STYLE_TEXT, null)
            val sourceStyles = commonDataStore.getStringSet(PREF_COMMON_FONT_STYLE_SOURCE, null)
            val quoteStyle = getTypefaceStyle(quoteStyles)
            val sourceStyle = getTypefaceStyle(sourceStyles)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val font = commonDataStore.getString(
                    PREF_COMMON_FONT_FAMILY, PREF_COMMON_FONT_FAMILY_DEFAULT)
                if (PREF_COMMON_FONT_FAMILY_DEFAULT != font) {
                    val fontId = requireContext().resources.getIdentifier(font, "font",
                        requireContext().packageName)
                    val typeface = ResourcesCompat.getFont(requireContext(), fontId)
                    this.quoteTypeface = typeface
                    this.quoteStyle = quoteStyle
                    this.sourceTypeface = typeface
                    this.sourceStyle = sourceStyle
                } else {
                    this.quoteTypeface = null
                    this.quoteStyle = quoteStyle
                    this.sourceTypeface = null
                    this.sourceStyle = sourceStyle
                }
            } else {
                this.quoteTypeface = null
                this.quoteStyle = quoteStyle
                this.sourceTypeface = null
                this.sourceStyle = sourceStyle
            }

            // Layout padding
            paddingTop = commonDataStore.getString(PREF_COMMON_PADDING_TOP,
                PREF_COMMON_PADDING_TOP_DEFAULT)!!.toFloat()
                .dp2px()
                .toInt()
            paddingBottom = commonDataStore.getString(PREF_COMMON_PADDING_BOTTOM,
                PREF_COMMON_PADDING_BOTTOM_DEFAULT)!!.toFloat()
                .dp2px()
                .toInt()
        }
    }

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?,
    ): RecyclerView {
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState).also {
            it.overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun observePreferences() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    quotesDataStore.collectSuspend { preferences, _ ->
                        mPreviewPreference?.quote =
                            preferences[stringPreferencesKey(PREF_QUOTES_TEXT)]
                        mPreviewPreference?.source =
                            preferences[stringPreferencesKey(PREF_QUOTES_SOURCE)]
                    }
                }
                launch {
                    commonDataStore.collectSuspend { preferences, key ->
                        when (key?.name) {
                            PREF_COMMON_FONT_SIZE_TEXT -> mPreviewPreference?.quoteSize =
                                (preferences[stringPreferencesKey(PREF_COMMON_FONT_SIZE_TEXT)]
                                    ?: PREF_COMMON_FONT_SIZE_TEXT_DEFAULT).toFloat()
                            PREF_COMMON_FONT_SIZE_SOURCE -> mPreviewPreference?.sourceSize =
                                (preferences[stringPreferencesKey(PREF_COMMON_FONT_SIZE_SOURCE)]
                                    ?: PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT).toFloat()
                            PREF_COMMON_FONT_FAMILY -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val font =
                                        preferences[stringPreferencesKey(PREF_COMMON_FONT_FAMILY)]
                                            ?: PREF_COMMON_FONT_FAMILY_DEFAULT
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
                                val quoteStyles =
                                    preferences[stringSetPreferencesKey(PREF_COMMON_FONT_STYLE_TEXT)]
                                mPreviewPreference?.quoteStyle = getTypefaceStyle(quoteStyles)
                            }
                            PREF_COMMON_FONT_STYLE_SOURCE -> {
                                val sourceStyles =
                                    preferences[stringSetPreferencesKey(
                                        PREF_COMMON_FONT_STYLE_SOURCE)]
                                mPreviewPreference?.sourceStyle = getTypefaceStyle(sourceStyles)
                            }
                            PREF_COMMON_PADDING_TOP -> mPreviewPreference?.paddingTop =
                                (preferences[stringPreferencesKey(PREF_COMMON_PADDING_TOP)]
                                    ?: PREF_COMMON_PADDING_TOP_DEFAULT).toFloat()
                                    .dp2px()
                                    .toInt()
                            PREF_COMMON_PADDING_BOTTOM -> mPreviewPreference?.paddingBottom =
                                (preferences[stringPreferencesKey(PREF_COMMON_PADDING_BOTTOM)]
                                    ?: PREF_COMMON_PADDING_BOTTOM_DEFAULT).toFloat()
                                    .dp2px()
                                    .toInt()
                            else -> {}
                        }
                    }
                }
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