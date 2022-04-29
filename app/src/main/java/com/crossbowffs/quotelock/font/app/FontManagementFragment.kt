package com.crossbowffs.quotelock.font.app

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.font.FontInfoWithState
import com.crossbowffs.quotelock.font.FontManager
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class FontManagementFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.layout_recycler_list, container, false).apply {
            recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(FontItemDecoration(context))
                adapter = FontManagementAdapter()
                registerForContextMenu(this)

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        FontManager.loadFontsList()
                    }?.let {
                        (recyclerView.adapter as? FontManagementAdapter)?.submitList(it)
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
        inflater.inflate(R.menu.custom_font_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? ContextMenuRecyclerView.ContextMenuInfo
        if (item.itemId == R.id.delete) {
            info ?: return true
            lifecycleScope.launch {
                withContext(Dispatchers.IO) deleteFont@{
                    (recyclerView.adapter as? FontManagementAdapter)?.let { adapter ->
                        if (adapter.currentList.isNullOrEmpty() || adapter.currentList.size <= info.position) {
                            return@deleteFont null
                        }
                        adapter.currentList[info.position]?.let { it ->
                            if (it.active) {
                                if (FontManager.deleteActiveSystemFont(it.fontInfo.fileName)) {
                                    getString(R.string.quote_fonts_management_delete_active_font_successfully)
                                } else {
                                    getString(R.string.quote_fonts_management_delete_font_failed,
                                        it.fontInfo.name)
                                }
                            } else {
                                getString(if (FontManager.deleteInactiveFont(it.fontInfo.fileName)) {
                                    R.string.quote_fonts_management_delete_inactive_font_successfully
                                } else {
                                    R.string.quote_fonts_management_delete_font_failed
                                }, it.fontInfo.name)
                            }
                        }
                    }
                }?.let {
                    Snackbar.make(recyclerView, it, Snackbar.LENGTH_SHORT)
                        .apply { setAnchorView(R.id.fab) }.show()
                }
                onFontListChanged()
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    fun onFontListChanged() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                FontManager.loadFontsList()
            }?.let {
                (recyclerView.adapter as? FontManagementAdapter)?.apply {
                    val sizeChanged = currentList.size - it.size != 0
                    submitList(it) {
                        if (sizeChanged) {
                            recyclerView.smoothScrollToPosition(itemCount - 1)
                        }
                    }
                }
            }
        }
    }
}

class FontManagementAdapter :
    ListAdapter<FontInfoWithState, FontItemViewHolder>(FontItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FontItemViewHolder {
        return FontItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.listitem_custom_font,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FontItemViewHolder, pos: Int) {
        val position = holder.adapterPosition
        val fontInfoWithState = currentList[position]
        holder.apply {
            val systemFont = fontInfoWithState.systemFont
            val active = fontInfoWithState.active
            fontInfoWithState.fontInfo.let {
                val typeface = FontManager.loadTypeface(it.path)
                tvFontName.text = it.name
                tvFontName.typeface = typeface
                tvFontDescriptionLatin.text = it.descriptionLatin
                tvFontDescriptionLatin.typeface = typeface
                if (it.descriptionLocale.isBlank()) {
                    tvFontDescriptionLocale.visibility = View.GONE
                } else {
                    tvFontDescriptionLocale.visibility = View.VISIBLE
                    tvFontDescriptionLocale.text = it.descriptionLocale
                    tvFontDescriptionLocale.typeface = typeface
                }
                if (active) {
                    ivFontActiveHint.visibility = View.GONE
                    tvInactive.visibility = View.GONE
                } else {
                    tvFontName.isEnabled = false
                    tvFontDescriptionLatin.isEnabled = false
                    tvFontDescriptionLocale.isEnabled = false
                    if (systemFont) {
                        ivFontActiveHint.visibility = View.GONE
                    } else {
                        ivFontActiveHint.visibility = View.VISIBLE
                    }
                    tvInactive.visibility = View.VISIBLE
                }
            }
        }
    }
}

private class FontItemDiffCallback : DiffUtil.ItemCallback<FontInfoWithState>() {
    override fun areItemsTheSame(
        oldItem: FontInfoWithState,
        newItem: FontInfoWithState,
    ): Boolean {
        return oldItem.fontInfo == newItem.fontInfo && oldItem.active == newItem.active
    }

    override fun areContentsTheSame(
        oldItem: FontInfoWithState,
        newItem: FontInfoWithState,
    ): Boolean {
        return oldItem == newItem
    }
}

class FontItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val tvInactive: TextView = view.findViewById(R.id.list_item_custom_font_inactive)
    val tvFontName: TextView = view.findViewById(R.id.list_item_custom_font_name)
    val tvFontDescriptionLatin: TextView =
        view.findViewById(R.id.list_item_custom_font_description_latin)
    val tvFontDescriptionLocale: TextView =
        view.findViewById(R.id.list_item_custom_font_description_locale)
    val ivFontActiveHint: ImageView =
        view.findViewById(R.id.list_item_custom_font_active_hint)

    init {
        view.isLongClickable = true
        ivFontActiveHint.setOnClickListener {
            Snackbar.make(it, R.string.quote_fonts_management_activate_tips, Snackbar.LENGTH_SHORT)
                .apply { setAnchorView(R.id.fab) }
                .show()
        }
    }
}