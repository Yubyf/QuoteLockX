package com.crossbowffs.quotelock.components

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Yubyf
 */
class ContextMenuRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private var contextMenuInfo: ContextMenuInfo? = null

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View?): Boolean {
        if (originalView == null) {
            return false
        }
        val position = getChildAdapterPosition(originalView)
        if (position < 0) {
            return false
        }
        contextMenuInfo = ContextMenuInfo(position, adapter?.getItemId(position) ?: -1)
        return super.showContextMenuForChild(originalView)
    }

    class ContextMenuInfo(var position: Int, var id: Long) : ContextMenu.ContextMenuInfo

}