package com.crossbowffs.quotelock.font

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import com.crossbowffs.quotelock.components.MaterialPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT
import com.crossbowffs.quotelock.font.app.FontManagementActivity
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Show font list with specific font styles
 * based on [androidx.preference.ListPreferenceDialogFragmentCompat].
 *
 * @author Yubyf
 */
class FontListPreferenceDialogFragmentCompat : MaterialPreferenceDialogFragmentCompat() {
    private var mClickedDialogEntryIndex/* synthetic access */ = 0
    private lateinit var mEntries: Array<CharSequence>
    private lateinit var mEntryValues: Array<CharSequence>
    private val mFontList = mutableListOf<FontInfo>()
    private var mAdapter: FontStyleCheckedItemAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            check(listPreference.entries != null && listPreference.entryValues != null) {
                "ListPreference requires an entries array and an entryValues array."
            }
            mClickedDialogEntryIndex = listPreference.findIndexOfValue(listPreference.value)
            mEntries = listPreference.entries
            mEntryValues = listPreference.entryValues
            mEntries.forEachIndexed { index, charSequence ->
                val fontInfo = FontInfo(name = charSequence.toString(),
                    mEntryValues[index].toString())
                mFontList.add(fontInfo)
            }
        } else {
            mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0)
            mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES) ?: arrayOf()
            mEntryValues =
                savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES) ?: arrayOf()
            mEntries.forEachIndexed { index, charSequence ->
                val fontInfo = FontInfo(name = charSequence.toString(),
                    mEntryValues[index].toString())
                mFontList.add(fontInfo)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_INDEX, mClickedDialogEntryIndex)
        outState.putCharSequenceArray(SAVE_STATE_ENTRIES, mEntries)
        outState.putCharSequenceArray(SAVE_STATE_ENTRY_VALUES, mEntryValues)
    }

    private val listPreference: ListPreference
        get() = preference as ListPreference

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        //region Modified part
        val a = requireContext().obtainStyledAttributes(null, R.styleable.AlertDialog,
            R.attr.alertDialogStyle, 0)
        val layout =
            a.getResourceId(androidx.appcompat.R.styleable.AlertDialog_singleChoiceItemLayout, 0)
        a.recycle()
        mAdapter = FontStyleCheckedItemAdapter(requireContext(), layout,
            android.R.id.text1, mFontList)
        builder.setSingleChoiceItems(mAdapter, mClickedDialogEntryIndex
        ) { dialog, which ->
            mClickedDialogEntryIndex = which

            // Clicking on an item simulates the positive button click, and dismisses
            // the dialog.
            onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dialog.dismiss()
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                mFontList.forEach {
                    if (it.path == PREF_COMMON_FONT_FAMILY_DEFAULT) {
                        return@forEach
                    }
                    FontManager.loadFontInfo(File(it.path))?.let { fontInfo ->
                        it.name = fontInfo.name
                        withContext(Dispatchers.Main) { mAdapter?.notifyDataSetChanged() }
                    }
                }
            }
        }
        //endregion

        // The typical interaction for list-based dialogs is to have click-on-an-item dismiss the
        // dialog instead of the user having to press 'Ok'.
        builder.setPositiveButton(null, null)

        // Start fonts management page
        builder.setNeutralButton(R.string.pref_font_family_custom) { _, _ ->
            startActivity(Intent(requireContext(), FontManagementActivity::class.java))
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && mClickedDialogEntryIndex >= 0) {
            val value = mEntryValues[mClickedDialogEntryIndex].toString()
            val preference = listPreference
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
        }
    }

    /**
     * Stylize the list item by specific font family.
     */
    private class FontStyleCheckedItemAdapter(
        context: Context, resource: Int, textViewResourceId: Int, objects: List<FontInfo>,
    ) : ArrayAdapter<FontInfo>(context, resource, textViewResourceId, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            (view as? TextView)?.apply {
                getItem(position)?.let { font ->
                    text = font.name
                    typeface = if (PREF_COMMON_FONT_FAMILY_DEFAULT == font.path) {
                        null
                    } else {
                        runCatching {
                            FontManager.loadTypeface(font.path)
                        }.onFailure {
                            Xlog.e(TAG, "Failed to get font from name: $font")
                        }.getOrNull()
                    }
                }
            }
            return view
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }

    companion object {
        val TAG = className<FontListPreferenceDialogFragmentCompat>()

        private const val SAVE_STATE_INDEX = "ListPreferenceDialogFragment.index"
        private const val SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries"
        private const val SAVE_STATE_ENTRY_VALUES = "ListPreferenceDialogFragment.entryValues"

        fun newInstance(key: String?): FontListPreferenceDialogFragmentCompat =
            FontListPreferenceDialogFragmentCompat().apply {
                arguments = Bundle(1).apply { putString(ARG_KEY, key) }
            }
    }

    private data class FontInfo(var name: String, var path: String)
}