package com.crossbowffs.quotelock.app.settings

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.datastore.PreferenceDataStoreAdapter
import com.crossbowffs.quotelock.di.CommonDataStore
import com.crossbowffs.quotelock.utils.className
import com.google.android.material.card.MaterialCardView
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@AndroidEntryPoint
class PreviewFragment : PreferenceFragmentCompat() {

    @Inject
    @CommonDataStore
    lateinit var commonDataStore: PreferenceDataStoreAdapter

    private val viewModel: PreviewViewModel by viewModels()
    private var mPreviewPreference: PreviewPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preview_preference, rootKey)
        mPreviewPreference = findPreference<Preference>(PREF_PREVIEW) as? PreviewPreference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            observePreferences()
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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.uiState.collect {
                        mPreviewPreference?.apply {
                            quote = it.quoteViewData.text
                            source = it.quoteViewData.source
                            quoteSize = it.quoteStyle.quoteSize
                            sourceSize = it.quoteStyle.sourceSize
                            // Font properties
                            quoteTypeface = it.quoteStyle.quoteTypeface
                            quoteStyle = it.quoteStyle.quoteStyle
                            sourceTypeface = it.quoteStyle.sourceTypeface
                            sourceStyle = it.quoteStyle.sourceStyle
                            // Quote spacing
                            quoteSpacing = it.quoteStyle.quoteSpacing
                            // Layout padding
                            paddingTop = it.quoteStyle.paddingTop
                            paddingBottom = it.quoteStyle.paddingBottom
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

    var quoteSpacing: Int by Setter(0)

    var paddingTop: Int by Setter(PREF_COMMON_PADDING_TOP_DEFAULT.toInt())

    var paddingBottom: Int by Setter(PREF_COMMON_PADDING_BOTTOM_DEFAULT.toInt())

    init {
        layoutResource = R.layout.quote_preview_layout
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val quoteContainer = holder.itemView as MaterialCardView
        val quoteTextView = holder.findViewById(R.id.quote_textview) as? TextView
        val quoteSourceView = holder.findViewById(R.id.source_textview) as? TextView
        quoteContainer.apply {
            setContentPadding(contentPaddingLeft, this@PreviewPreference.paddingTop,
                contentPaddingRight, this@PreviewPreference.paddingBottom)
        }
        quoteTextView?.run {
            text = quote
            setTextSize(TypedValue.COMPLEX_UNIT_SP, quoteSize)
            setTypeface(quoteTypeface, quoteStyle)
        }
        quoteSourceView?.run {
            if (source.isNullOrBlank()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = source
                setTextSize(TypedValue.COMPLEX_UNIT_SP, sourceSize)
                setTypeface(sourceTypeface, sourceStyle)
            }
            (layoutParams as LinearLayout.LayoutParams).topMargin = quoteSpacing
        }
    }
}