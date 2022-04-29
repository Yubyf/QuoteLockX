package com.crossbowffs.quotelock.font.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class FontItemDecoration(context: Context) : DividerItemDecoration(context, VERTICAL) {

    private val bounds = Rect()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }
        drawable?.let {
            drawVertical(c, it, parent)
        }
    }

    private fun drawVertical(canvas: Canvas, drawable: Drawable, parent: RecyclerView) {
        // Codes from DividerItemDecoration
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,
                parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }
        val childCount = parent.childCount
        // Block the divider after the last item
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - drawable.intrinsicHeight
            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
        canvas.restore()
    }
}