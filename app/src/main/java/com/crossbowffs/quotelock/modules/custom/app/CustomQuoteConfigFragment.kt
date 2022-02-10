package com.crossbowffs.quotelock.modules.custom.app

import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.fragment.app.ListFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.modules.custom.provider.CustomQuoteContract

class CustomQuoteConfigFragment : ListFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.layout_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val from = arrayOf(CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE)
        val to = intArrayOf(R.id.listitem_custom_quote_text, R.id.listitem_custom_quote_source)
        listAdapter =
            SimpleCursorAdapter(requireContext(), R.layout.listitem_custom_quote, null, from, to, 0)
        LoaderManager.getInstance(this).initLoader(0, null, this)
        registerForContextMenu(listView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.custom_quote_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.create_quote) {
            showEditQuoteDialog(-1)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = requireActivity().menuInflater
        inflater.inflate(R.menu.custom_quote_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        val rowId = info.id
        val itemId = item.itemId
        if (itemId == R.id.edit_quote) {
            showEditQuoteDialog(rowId)
            return true
        } else if (itemId == R.id.delete_quote) {
            deleteQuote(rowId)
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(
            requireContext(),
            CustomQuoteContract.Quotes.CONTENT_URI,
            CustomQuoteContract.Quotes.ALL,
            null, null, null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        (listAdapter as? SimpleCursorAdapter)?.changeCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        (listAdapter as? SimpleCursorAdapter)?.changeCursor(null)
    }

    private fun queryQuote(rowId: Long): QuoteData? {
        if (rowId < 0) {
            return null
        }
        val uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId)
        val columns = arrayOf(CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE)
        requireContext().contentResolver.query(uri, columns, null, null, null)
            .use {
                return if (it != null && it.moveToFirst()) {
                    QuoteData(it.getString(0), it.getString(1))
                } else {
                    null
                }
            }
    }

    private fun persistQuote(rowId: Long, text: String, source: String) {
        val values = ContentValues(3)
        values.put(CustomQuoteContract.Quotes.TEXT, text)
        values.put(CustomQuoteContract.Quotes.SOURCE, source)
        val resolver = requireContext().contentResolver
        if (rowId >= 0) {
            val uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId)
            values.put(CustomQuoteContract.Quotes.ID, rowId)
            resolver.update(uri, values, null, null)
        } else {
            resolver.insert(CustomQuoteContract.Quotes.CONTENT_URI, values)
        }
        Toast.makeText(requireContext(), R.string.module_custom_saved_quote, Toast.LENGTH_SHORT)
            .show()
    }

    private fun deleteQuote(rowId: Long) {
        val uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId)
        requireContext().contentResolver.delete(uri, null, null)
        Toast.makeText(requireContext(), R.string.module_custom_deleted_quote, Toast.LENGTH_SHORT)
            .show()
    }

    private fun showEditQuoteDialog(rowId: Long) {
        val layoutInflater = layoutInflater
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_quote, null)
        val textEditText = dialogView.findViewById<View>(R.id.dialog_custom_quote_text) as EditText
        val sourceEditText =
            dialogView.findViewById<View>(R.id.dialog_custom_quote_source) as EditText
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.module_custom_enter_quote)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                persistQuote(
                    rowId,
                    textEditText.text.toString(),
                    sourceEditText.text.toString()
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                val canSave = !textEditText.text.isNullOrEmpty()
                button.isEnabled = canSave
            }
        }
        textEditText.addTextChangedListener(watcher)
        sourceEditText.addTextChangedListener(watcher)
        val quote = queryQuote(rowId) ?: QuoteData()
        textEditText.setText(quote.quoteText)
        sourceEditText.setText(quote.quoteSource)
    }
}