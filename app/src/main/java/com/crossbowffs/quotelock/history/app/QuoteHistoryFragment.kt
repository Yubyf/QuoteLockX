package com.crossbowffs.quotelock.history.app

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.components.BaseQuoteListFragment
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.components.QuoteListAdapter
import com.crossbowffs.quotelock.history.database.QuoteHistoryEntity
import com.crossbowffs.quotelock.history.database.quoteHistoryDatabase
import com.crossbowffs.quotelock.utils.ioScope
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class QuoteHistoryFragment : BaseQuoteListFragment<QuoteHistoryEntity>() {

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setFragmentResult(REQUEST_KEY_HISTORY_LIST_PAGE,
            bundleOf(BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE to false))
        return super.onCreateView(inflater, container, savedInstanceState).also {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    quoteHistoryDatabase.dao().getAll().collect {
                        (recyclerView.adapter as? QuoteListAdapter<QuoteHistoryEntity>)?.submitList(
                            it)
                        scrollToPosition()
                    }
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
                ioScope.launch {
                    quoteHistoryDatabase.dao().delete(it.id)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(requireView(),
                            R.string.module_custom_deleted_quote,
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
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

    companion object {
        const val REQUEST_KEY_HISTORY_LIST_PAGE = "history_list_page"
        const val BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE = "history_show_detail_page"
    }
}