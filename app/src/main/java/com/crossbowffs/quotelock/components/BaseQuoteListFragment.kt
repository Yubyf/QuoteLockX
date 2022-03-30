package com.crossbowffs.quotelock.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.*
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.app.QuoteDetailFragment
import com.crossbowffs.quotelock.database.QuoteEntity
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Yubyf
 */
abstract class BaseQuoteListFragment<T : QuoteEntity> : Fragment() {
    protected lateinit var recyclerView: RecyclerView
    private var lastSelectPosition = -1
    private var lastScrollPosition = 0
    private var lastScrollOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.layout_recycler_list, container, false).apply {
            recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = if (showDetailPage()) {
                    QuoteListAdapter<T> { holder, quote ->
                        // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
                        // instead of fading out with the rest to prevent an overlapping animation of fade and move).
                        (exitTransition as TransitionSet).excludeTarget(holder.view, true)
                        lastSelectPosition = holder.adapterPosition
                        lastScrollPosition =
                            (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                                ?: 0
                        lastScrollOffset = getChildAt(childCount - 1)?.top ?: 0
                        parentFragmentManager.commit {
                            setReorderingAllowed(true)
                            addSharedElement(holder.quoteTextView,
                                holder.quoteTextView.transitionName)
                            addSharedElement(holder.quoteSourceView,
                                holder.quoteSourceView.transitionName)
                            replace(R.id.content_frame,
                                QuoteDetailFragment.newInstance(
                                    quote.text,
                                    quote.source,
                                    holder.quoteTextView.transitionName,
                                    holder.quoteSourceView.transitionName))
                            addToBackStack(null)
                            goToDetailPage()
                        }
                    }.apply {
                        lastSelectPosition = this@BaseQuoteListFragment.lastSelectPosition
                        onSelectedItemLoaded = ::startPostponedEnterTransition
                    }
                } else {
                    QuoteListAdapter<T>()
                }
                registerForContextMenu(this)
            }
            exitTransition = TransitionInflater.from(context)
                .inflateTransition(R.transition.quote_list_exit_transition)
            if (lastSelectPosition >= 0) {
                postponeEnterTransition()
            }
        }
    }

    override fun onDestroyView() {
        unregisterForContextMenu(recyclerView)
        super.onDestroyView()
    }

    protected abstract fun showDetailPage(): Boolean

    protected abstract fun goToDetailPage()

    /**
     * Scrolls the recycler view to show the last viewed item in the list.
     */
    protected fun scrollToPosition() {
        if (lastSelectPosition < 0) {
            return
        }
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int,
            ) {
                recyclerView.removeOnLayoutChangeListener(this)
                (recyclerView.layoutManager as? LinearLayoutManager)?.apply {
                    // Scroll to position if the view for the current position is null (not currently part of
                    // layout manager children), or it's not completely visible.
                    recyclerView.post {
                        scrollToPositionWithOffset(lastScrollPosition, lastScrollOffset)
                        lastSelectPosition = -1
                        lastScrollPosition = 0
                        lastScrollOffset = 0
                    }
                }
            }
        })
    }
}

class QuoteListAdapter<T : QuoteEntity>(
    private var viewHolderListener: ((QuoteItemViewHolder, T: QuoteEntity) -> Unit)? = null,
) : ListAdapter<T, QuoteItemViewHolder>(QuoteItemDiffCallback()) {

    private val enterTransitionStarted = AtomicBoolean()
    var lastSelectPosition = -1
    var onSelectedItemLoaded: (() -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): QuoteItemViewHolder {
        return QuoteItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.listitem_custom_quote,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QuoteItemViewHolder, pos: Int) {
        val position = holder.adapterPosition
        val quote = currentList[position]
        holder.apply {
            quoteTextView.apply {
                text = quote.text
                transitionName = "quote_#$position"
            }
            quoteSourceView.apply {
                text = quote.source
                transitionName = "source_#$position"
            }
            view.setOnClickListener { viewHolderListener?.invoke(holder, quote) }
        }

        // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
        if (lastSelectPosition == position) {
            if (!enterTransitionStarted.getAndSet(true)) {
                onSelectedItemLoaded?.invoke()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].id?.toLong() ?: -1
    }

    private class QuoteItemDiffCallback<T : QuoteEntity> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(
            oldItem: T,
            newItem: T,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: T,
            newItem: T,
        ): Boolean {
            return oldItem == newItem
        }
    }
}

class QuoteItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val quoteTextView: TextView = view.findViewById(R.id.listitem_custom_quote_text)
    val quoteSourceView: TextView = view.findViewById(R.id.listitem_custom_quote_source)

    init {
        view.isLongClickable = true
    }
}
