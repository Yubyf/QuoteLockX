package com.crossbowffs.quotelock.modules.custom.app

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.components.BaseQuoteListFragment
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.components.QuoteListAdapter
import com.crossbowffs.quotelock.modules.custom.database.CustomQuoteEntity
import com.crossbowffs.quotelock.modules.custom.database.customQuoteDatabase
import com.crossbowffs.quotelock.utils.ioScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class CustomQuoteConfigFragment : BaseQuoteListFragment<CustomQuoteEntity>() {

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                customQuoteDatabase.dao().getAll().collect {
                    (recyclerView.adapter as? QuoteListAdapter<CustomQuoteEntity>)?.submitList(it)
                    scrollToPosition()
                }
            }
        }
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

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.custom_quote_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? ContextMenuRecyclerView.ContextMenuInfo
        info?.let {
            val rowId = it.id
            val itemId = item.itemId
            if (itemId == R.id.edit_quote) {
                showEditQuoteDialog(rowId)
                return true
            } else if (itemId == R.id.delete_quote) {
                deleteQuote(rowId)
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun queryQuote(rowId: Long): QuoteData? {
        if (rowId < 0) {
            return null
        }
        return runBlocking {
            customQuoteDatabase.dao().getById(rowId).firstOrNull()?.let {
                QuoteData(it.text, it.source)
            }
        }
    }

    private fun persistQuote(rowId: Long, text: String, source: String) {
        ioScope.launch {
            if (rowId >= 0) {
                customQuoteDatabase.dao()
                    .update(CustomQuoteEntity(rowId.toInt(), text, source))
            } else {
                customQuoteDatabase.dao()
                    .insert(CustomQuoteEntity(text = text, source = source))
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(),
                    R.string.module_custom_saved_quote,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun deleteQuote(rowId: Long) {
        ioScope.launch {
            customQuoteDatabase.dao().delete(rowId)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(),
                    R.string.module_custom_deleted_quote,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
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

    override fun showDetailPage(): Boolean = false

    override fun goToDetailPage() = Unit /* no-op */
}