package com.crossbowffs.quotelock.app

import androidx.compose.material3.SnackbarDuration

/**
 * UI snack bar event.
 */
data class SnackBarEvent(
    val message: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionText: String? = null,
)

val emptySnackBarEvent = SnackBarEvent()