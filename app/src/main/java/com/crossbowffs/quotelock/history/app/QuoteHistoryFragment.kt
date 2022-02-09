package com.crossbowffs.quotelock.history.app

import android.content.ContentUris
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.fragment.app.ListFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.history.provider.QuoteHistoryContract

/**
 * @author Yubyf
 */
class QuoteHistoryFragment : ListFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val from =
            arrayOf(QuoteHistoryContract.Histories.TEXT, QuoteHistoryContract.Histories.SOURCE)
        val to = intArrayOf(R.id.listitem_custom_quote_text, R.id.listitem_custom_quote_source)
        listAdapter =
            SimpleCursorAdapter(requireContext(), R.layout.listitem_custom_quote, null, from, to, 0)
        val loaderManager = LoaderManager.getInstance(this)
        loaderManager.initLoader(0, null, this)
        registerForContextMenu(listView)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = requireActivity().menuInflater
        inflater.inflate(R.menu.custom_quote_context, menu)
        menu.findItem(R.id.edit_quote).isVisible = false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        val rowId = info.id
        if (item.itemId == R.id.delete_quote) {
            deleteQuote(rowId)
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(
            requireContext(),
            QuoteHistoryContract.Histories.CONTENT_URI,
            QuoteHistoryContract.Histories.ALL,
            null, null, null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        (listAdapter as? SimpleCursorAdapter)?.changeCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        (listAdapter as? SimpleCursorAdapter)?.changeCursor(null)
    }

    private fun deleteQuote(rowId: Long) {
        val uri = ContentUris.withAppendedId(QuoteHistoryContract.Histories.CONTENT_URI, rowId)
        requireContext().contentResolver.delete(uri, null, null)
        Toast.makeText(requireContext(), R.string.module_custom_deleted_quote, Toast.LENGTH_SHORT)
            .show()
    }

    fun reloadData() {
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }
}