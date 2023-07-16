package com.crossbowffs.quotelock.ui.components

import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class SecurityTextToolbar(private val view: View) : TextToolbar {

    private var actionMode: ActionMode? = null
    private val textActionModeCallback: SecurityTextActionModeCallback =
        SecurityTextActionModeCallback(
            onActionModeDestroy = {
                actionMode = null
            }
        )

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        textActionModeCallback.rect = rect
        textActionModeCallback.onPasteRequested = onPasteRequested
        textActionModeCallback.onSelectAllRequested = onSelectAllRequested
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = if (Build.VERSION.SDK_INT >= 23) {
                view.startActionMode(
                    FloatingTextActionModeCallback(textActionModeCallback),
                    ActionMode.TYPE_FLOATING
                )
            } else {
                view.startActionMode(
                    PrimaryTextActionModeCallback(textActionModeCallback)
                )
            }
        } else {
            actionMode?.invalidate()
        }
    }
}

@RequiresApi(23)
internal class FloatingTextActionModeCallback(
    private val callback: SecurityTextActionModeCallback,
) : ActionMode.Callback2() {
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return callback.onActionItemClicked(mode, item)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onPrepareActionMode(mode, menu)
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        callback.onDestroyActionMode()
    }

    override fun onGetContentRect(
        mode: ActionMode?,
        view: View?,
        outRect: android.graphics.Rect?,
    ) {
        val rect = callback.rect
        outRect?.set(
            rect.left.toInt(),
            rect.top.toInt(),
            rect.right.toInt(),
            rect.bottom.toInt()
        )
    }
}

class PrimaryTextActionModeCallback(
    private val callback: SecurityTextActionModeCallback,
) : ActionMode.Callback {
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return callback.onActionItemClicked(mode, item)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onPrepareActionMode(mode, menu)
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        callback.onDestroyActionMode()
    }
}

class SecurityTextActionModeCallback(
    internal val onActionModeDestroy: (() -> Unit)? = null,
    internal var rect: Rect = Rect.Zero,
    internal var onPasteRequested: (() -> Unit)? = null,
    internal var onSelectAllRequested: (() -> Unit)? = null,
) {
    fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu)
        requireNotNull(mode)

        onPasteRequested?.let {
            addMenuItem(menu, MenuItemOption.Paste)
        }
        onSelectAllRequested?.let {
            addMenuItem(menu, MenuItemOption.SelectAll)
        }
        return true
    }

    // this method is called to populate new menu items when the actionMode was invalidated
    fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        if (mode == null || menu == null) return false
        updateMenuItems(menu)
        // should return true so that new menu items are populated
        return true
    }

    fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item!!.itemId) {
            MenuItemOption.Paste.id -> onPasteRequested?.invoke()
            MenuItemOption.SelectAll.id -> onSelectAllRequested?.invoke()
            else -> return false
        }
        mode?.finish()
        return true
    }

    fun onDestroyActionMode() {
        onActionModeDestroy?.invoke()
    }

    private fun updateMenuItems(menu: Menu) {
        addOrRemoveMenuItem(menu, MenuItemOption.Paste, onPasteRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.SelectAll, onSelectAllRequested)
    }

    private fun addMenuItem(menu: Menu, item: MenuItemOption) {
        menu.add(0, item.id, item.order, item.titleResource)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    private fun addOrRemoveMenuItem(
        menu: Menu,
        item: MenuItemOption,
        callback: (() -> Unit)?,
    ) {
        when {
            callback != null && menu.findItem(item.id) == null -> addMenuItem(menu, item)
            callback == null && menu.findItem(item.id) != null -> menu.removeItem(item.id)
        }
    }
}

internal enum class MenuItemOption(val id: Int) {
    Copy(0),
    Paste(1),
    Cut(2),
    SelectAll(3);

    val titleResource: Int
        get() = when (this) {
            Copy -> android.R.string.copy
            Paste -> android.R.string.paste
            Cut -> android.R.string.cut
            SelectAll -> android.R.string.selectAll
        }

    /**
     * This item will be shown before all items that have order greater than this value.
     */
    val order = id
}