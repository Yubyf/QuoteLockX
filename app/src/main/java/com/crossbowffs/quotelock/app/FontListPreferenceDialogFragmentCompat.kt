package com.crossbowffs.quotelock.app

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.PreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.utils.getFontFromName

/**
 * Show font list with specific font styles
 * based on [androidx.preference.ListPreferenceDialogFragmentCompat].
 *
 * @author Yubyf
 */
class FontListPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var mClickedDialogEntryIndex/* synthetic access */ = 0
    private lateinit var mEntries: Array<CharSequence?>
    private lateinit var mEntryValues: Array<CharSequence>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            check(listPreference.entries != null && listPreference.entryValues != null) {
                "ListPreference requires an entries array and an entryValues array."
            }
            mClickedDialogEntryIndex = listPreference.findIndexOfValue(listPreference.value)
            mEntries = listPreference.entries
            mEntryValues = listPreference.entryValues
        } else {
            mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0)
            mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES) ?: arrayOf()
            mEntryValues =
                savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES) ?: arrayOf()
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
        val adapter = FontStyleCheckedItemAdapter(requireContext(), layout,
            android.R.id.text1, mEntries, mEntryValues)
        builder.setSingleChoiceItems(adapter, mClickedDialogEntryIndex
        ) { dialog, which ->
            mClickedDialogEntryIndex = which

            // Clicking on an item simulates the positive button click, and dismisses
            // the dialog.
            onClick(dialog,
                DialogInterface.BUTTON_POSITIVE)
            dialog.dismiss()
        }
        //endregion

        // The typical interaction for list-based dialogs is to have click-on-an-item dismiss the
        // dialog instead of the user having to press 'Ok'.
        builder.setPositiveButton(null, null)
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

    //region Modified part
    /**
     * Stylize the list item by specific font family.
     */
    private class FontStyleCheckedItemAdapter(
        context: Context, resource: Int, textViewResourceId: Int,
        objects: Array<CharSequence?>, private val values: Array<CharSequence>,
    ) : ArrayAdapter<CharSequence?>(context, resource, textViewResourceId, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            if (view is TextView && values.size > position) {
                val fontName = values[position] as String
                try {
                    view.typeface = context.getFontFromName(fontName)
                } catch (e: NotFoundException) {
                    view.typeface = null
                    e.printStackTrace()
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
    //endregion

    companion object {
        private const val SAVE_STATE_INDEX = "ListPreferenceDialogFragment.index"
        private const val SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries"
        private const val SAVE_STATE_ENTRY_VALUES = "ListPreferenceDialogFragment.entryValues"

        fun newInstance(key: String?): FontListPreferenceDialogFragmentCompat {
            val fragment = FontListPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}