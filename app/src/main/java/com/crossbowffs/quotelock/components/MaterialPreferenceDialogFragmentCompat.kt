package com.crossbowffs.quotelock.components

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreferenceDialogFragmentCompat
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.utils.getReflectionField
import com.crossbowffs.quotelock.utils.invokeReflectionMethod
import com.crossbowffs.quotelock.utils.setReflectionField
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private fun PreferenceDialogFragmentCompat.createMaterialDialog(
    savedInstanceState: Bundle?,
    builderBlock: ((AlertDialog.Builder, String?) -> Unit),
): Dialog {
    var title: String? = null
    var icon: BitmapDrawable? = null
    var positiveButtonText: String? = null
    var negativeButtonText: String? = null
    var message: String? = null
    runCatching {
        setReflectionField("mWhichButtonClicked", DialogInterface.BUTTON_NEGATIVE)
        title = getReflectionField<String>("mDialogTitle")
        icon = getReflectionField<BitmapDrawable>("mDialogIcon")
        positiveButtonText = getReflectionField<String>("mPositiveButtonText")
        negativeButtonText = getReflectionField<String>("mNegativeButtonText")
        message = getReflectionField<String>("mDialogMessage")
    }.onFailure {
        return onCreateDialog(savedInstanceState)
    }

    val builder = MaterialAlertDialogBuilder(requireContext())
        .setTitle(title)
        .setIcon(icon)
        .setPositiveButton(positiveButtonText, this)
        .setNegativeButton(negativeButtonText, this)
        .setMessage(message)
        .setBackgroundInsetTop(0)
        .setBackgroundInsetBottom(0)

    builderBlock.invoke(builder, message)

    // Create the dialog
    val dialog = builder.create()
    if (invokeReflectionMethod<Boolean>("needInputMethod") == true) {
        invokeReflectionMethod<Boolean>("requestInputMethod",
            linkedMapOf(Dialog::class.java to dialog))
    }

    return builder.create()
}

/**
 * A [androidx.preference.PreferenceDialogFragmentCompat] that uses a [MaterialAlertDialogBuilder].
 *
 * @author Yubyf
 */
abstract class MaterialPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        createMaterialDialog(savedInstanceState) { builder, message ->
            val contentView = onCreateDialogView(requireContext())
            if (contentView != null) {
                onBindDialogView(contentView)
                builder.setView(contentView)
            } else {
                builder.setMessage(message)
            }

            onPrepareDialogBuilder(builder)
        }
}

/**
 * A [androidx.preference.ListPreferenceDialogFragmentCompat] that uses a [MaterialAlertDialogBuilder].
 *
 * @author Yubyf
 */
class MaterialListPreferenceDialogFragmentCompat : ListPreferenceDialogFragmentCompat() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        createMaterialDialog(savedInstanceState) { builder, message ->
            val contentView = onCreateDialogView(requireContext())
            if (contentView != null) {
                onBindDialogView(contentView)
                builder.setView(contentView)
            } else {
                builder.setMessage(message)
            }

            onPrepareDialogBuilder(builder)
        }

    companion object {
        fun newInstance(key: String?): ListPreferenceDialogFragmentCompat =
            MaterialListPreferenceDialogFragmentCompat().apply {
                arguments = Bundle(1).apply { putString(ARG_KEY, key) }
            }
    }
}

/**
 * A [androidx.preference.MultiSelectListPreferenceDialogFragmentCompat] that uses a [MaterialAlertDialogBuilder].
 *
 * @author Yubyf
 */
class MaterialMultiSelectListPreferenceDialogFragmentCompat :
    MultiSelectListPreferenceDialogFragmentCompat() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        createMaterialDialog(savedInstanceState) { builder, message ->
            val contentView = onCreateDialogView(requireContext())
            if (contentView != null) {
                onBindDialogView(contentView)
                builder.setView(contentView)
            } else {
                builder.setMessage(message)
            }

            onPrepareDialogBuilder(builder)
        }

    companion object {
        fun newInstance(key: String?): MultiSelectListPreferenceDialogFragmentCompat =
            MaterialMultiSelectListPreferenceDialogFragmentCompat().apply {
                arguments = Bundle(1).apply { putString(ARG_KEY, key) }
            }
    }
}