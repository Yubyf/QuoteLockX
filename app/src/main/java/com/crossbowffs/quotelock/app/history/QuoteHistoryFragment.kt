package com.crossbowffs.quotelock.app.history

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.components.BaseQuoteListFragment
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.components.QuoteListAdapter
import com.crossbowffs.quotelock.data.history.QuoteHistoryEntity
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * @author Yubyf
 */
@AndroidEntryPoint
class QuoteHistoryFragment : BaseQuoteListFragment<QuoteHistoryEntity>(), MenuProvider {

    private val viewModel: QuoteHistoryViewModel by viewModels()
    private var menu: Menu? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setFragmentResult(REQUEST_KEY_HISTORY_LIST_PAGE,
            bundleOf(BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE to false))
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return super.onCreateView(inflater, container, savedInstanceState).also {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiListState.onEach { (items, showClearMenu) ->
                        (recyclerView.adapter as? QuoteListAdapter<QuoteHistoryEntity>)?.submitList(
                            items)
                        scrollToPosition()
                        if (showClearMenu) {
                            menu?.findItem(R.id.clear)?.apply {
                                isVisible = true
                                isEnabled = true
                            }
                        } else {
                            menu?.findItem(R.id.clear)?.apply {
                                isVisible = false
                                isEnabled = true
                            }
                        }

                    }.launchIn(this)
                    viewModel.uiEvent.onEach {
                        it.message?.let { message ->
                            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
                                .show()
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
        val inflater = requireActivity().menuInflater
        inflater.inflate(R.menu.custom_quote_context, menu)
        menu.findItem(R.id.edit_quote).isVisible = false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? ContextMenuRecyclerView.ContextMenuInfo
        if (item.itemId == R.id.delete_quote) {
            info?.let {
                viewModel.delete(it.id)
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun showDetailPage(): Boolean = true

    override fun goToDetailPage() {
        setFragmentResult(REQUEST_KEY_HISTORY_LIST_PAGE,
            bundleOf(BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE to true))
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.histories_options, menu)
        menu.findItem(R.id.clear)?.isVisible = viewModel.uiListState.value.showClearMenu
        this.menu = menu
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.clear) {
            clearQuoteHistories()
        }
        return true
    }

    private fun clearQuoteHistories() {
        menu?.findItem(R.id.clear)?.isEnabled = false
        viewModel.clear()
    }

    companion object {
        const val REQUEST_KEY_HISTORY_LIST_PAGE = "history_list_page"
        const val BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE = "history_show_detail_page"
    }
}