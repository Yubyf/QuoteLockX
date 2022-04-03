package com.crossbowffs.quotelock.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yubyf.quotelockx.R

/**
 * @author Yubyf
 */
@SuppressLint("InflateParams")
class ProgressAlertDialog(
    private val context: Context,
    message: String? = "",
    cancelable: Boolean = true,
    canceledOnTouchOutside: Boolean = true,
) {
    private var dialog: AlertDialog? = null
    private var view: View? = null

    var message = message
        set(value) {
            field = value ?: ""
            view?.findViewById<TextView>(R.id.tv_message)?.text = field
        }
    var cancelable = cancelable
        set(value) {
            field = value
            dialog?.setCancelable(field)
        }
    var canceledOnTouchOutside = canceledOnTouchOutside
        set(value) {
            field = value
            dialog?.setCanceledOnTouchOutside(field)
        }

    init {
        dialog = MaterialAlertDialogBuilder(context)
            .setView(LayoutInflater.from(context).inflate(R.layout.dialog_progress, null).apply {
                view = this
                findViewById<TextView>(R.id.tv_message).text = message
            })
            .setCancelable(cancelable)
            .create().apply {
                setCanceledOnTouchOutside(canceledOnTouchOutside)
            }
    }

    @SuppressLint("InflateParams")
    fun create(): ProgressAlertDialog {
        dialog = MaterialAlertDialogBuilder(context)
            .setView(LayoutInflater.from(context).inflate(R.layout.dialog_progress, null).apply {
                view = this
                findViewById<TextView>(R.id.tv_message).text = message
            })
            .setCancelable(cancelable)
            .create().apply {
                setCanceledOnTouchOutside(canceledOnTouchOutside)
            }
        return this
    }

    fun show() {
        dialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    fun dismiss() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }
}