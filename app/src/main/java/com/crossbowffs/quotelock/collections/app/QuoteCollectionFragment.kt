package com.crossbowffs.quotelock.collections.app

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.collections.database.quoteCollectionDatabase
import com.crossbowffs.quotelock.components.BaseQuoteListFragment
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.components.QuoteListAdapter
import com.crossbowffs.quotelock.consts.PREF_QUOTES_COLLECTION_STATE
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.utils.ioScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class QuoteCollectionFragment : BaseQuoteListFragment<QuoteCollectionEntity>() {

    private var collectionsObserver: Job? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeCollections()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setFragmentResult(REQUEST_KEY_COLLECTION_LIST_PAGE,
            bundleOf(BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE to false))
        return super.onCreateView(inflater, container, savedInstanceState)
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
                    val result = quoteCollectionDatabase.dao().delete(it.id)
                    if (result >= 0) {
                        quotesDataStore.putBoolean(PREF_QUOTES_COLLECTION_STATE, false)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(),
                                R.string.module_custom_deleted_quote,
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun showDetailPage(): Boolean = true

    override fun goToDetailPage() {
        setFragmentResult(REQUEST_KEY_COLLECTION_LIST_PAGE,
            bundleOf(BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE to true))
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeCollections() {
        collectionsObserver?.cancel()
        collectionsObserver = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                quoteCollectionDatabase.dao().getAll().collect {
                    (recyclerView.adapter as? QuoteListAdapter<QuoteCollectionEntity>)?.submitList(
                        it)
                    scrollToPosition()
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_COLLECTION_LIST_PAGE = "collection_list_page"
        const val BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE = "collection_show_detail_page"
    }
}