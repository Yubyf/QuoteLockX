package com.crossbowffs.quotelock.app

import androidx.compose.material3.SnackbarDuration
import com.crossbowffs.quotelock.data.api.AndroidString

/**
 * UI snack bar event.
 */
data class SnackBarEvent(
    val message: AndroidString? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionText: AndroidString? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnackBarEvent

        if (message != other.message) return false
        if (duration != other.duration) return false
        return actionText == other.actionText
    }

    override fun hashCode(): Int {
        var result = message?.hashCode() ?: 0
        result = 31 * result + duration.hashCode()
        result = 31 * result + (actionText?.hashCode() ?: 0)
        return result
    }
}

val emptySnackBarEvent = SnackBarEvent()