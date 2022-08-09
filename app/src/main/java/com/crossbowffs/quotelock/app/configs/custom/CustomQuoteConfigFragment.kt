package com.crossbowffs.quotelock.app.configs.custom

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.components.BaseQuoteListFragment
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.components.QuoteListAdapter
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * @author Yubyf
 */
@AndroidEntryPoint
class CustomQuoteConfigFragment : BaseQuoteListFragment<CustomQuoteEntity>(), MenuProvider {

    private val viewModel: CustomQuoteViewModel by viewModels()

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return super.onCreateView(inflater, container, savedInstanceState).also {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiListState.onEach {
                        (recyclerView.adapter as? QuoteListAdapter<CustomQuoteEntity>)?.submitList(
                            it.items)
                        scrollToPosition()
                    }.launchIn(this)
                    viewModel.uiEvent.onEach {
                        it.message?.let { message ->
                            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
                        }
                    }.launchIn(this)
                }
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.custom_quote_context, menu)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.custom_quote_options, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.create_quote) {
            showEditQuoteDialog(-1)
        }
        return true
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
                viewModel.delete(rowId)
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun showEditQuoteDialog(rowId: Long) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_quote, null)
        val textEditText =
            dialogView.findViewById<TextInputEditText>(R.id.dialog_custom_quote_text)
        val sourceEditText =
            dialogView.findViewById<TextInputEditText>(R.id.dialog_custom_quote_source)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.module_custom_enter_quote)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.persistQuote(
                    rowId,
                    textEditText.text.toString(),
                    sourceEditText.text.toString()
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .setBackgroundInsetTop(0)
            .setBackgroundInsetBottom(0)
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
        val quote = viewModel.queryQuote(rowId)
        textEditText.setText(quote.quoteText)
        sourceEditText.setText(quote.quoteSource)
    }

    override fun showDetailPage(): Boolean = false

    override fun goToDetailPage() = Unit /* no-op */
}