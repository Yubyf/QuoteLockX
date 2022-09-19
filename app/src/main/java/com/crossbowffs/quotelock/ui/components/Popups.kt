package com.crossbowffs.quotelock.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.max
import kotlin.math.min

/**
 * Modified from [androidx.compose.material3.DropdownMenu]
 * and [androidx.compose.material3.DropdownMenuContent].
 */

@Composable
fun AnchorPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    anchor: DpOffset = DpOffset(0.dp, 0.dp),
    alignment: Alignment = Alignment.TopStart,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable BoxScope.() -> Unit,
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded
    if (expandedStates.currentState || expandedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider = AnchorPopupPositionProvider(
            anchor,
            alignment,
            density
        ) { parentBounds, popupBounds ->
            transformOriginState.value = calculateTransformOrigin(parentBounds, popupBounds)
        }

        Popup(popupPositionProvider = popupPositionProvider,
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            AnchorPopupContent(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState,
                content = content
            )
        }
    }
}

@Composable
private fun AnchorPopupContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    content: @Composable BoxScope.() -> Unit,
) {
    // Popup open/close animation.
    val transition = updateTransition(expandedStates, "DropDownPopup")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }, label = "ScaleAnimation"
    ) {
        if (it) {
            // Popup is expanded.
            1f
        } else {
            // Popup is dismissed.
            0.8f
        }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }, label = "AlphaAnimation"
    ) {
        if (it) {
            // Popup is expanded.
            1f
        } else {
            // Popup is dismissed.
            0f
        }
    }
    Box(modifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        transformOrigin = transformOriginState.value
    }, content = content)
}

private data class AnchorPopupPositionProvider(
    val anchorPosition: DpOffset,
    val alignment: Alignment,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> },
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        // The anchor position offset specified using the anchor position offset parameter.
        val contentOffsetX = with(density) { anchorPosition.x.roundToPx() } + when (alignment) {
            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> -popupContentSize.width
            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> 0
            else -> -popupContentSize.width / 2
        }
        val contentOffsetY = with(density) { anchorPosition.y.roundToPx() } + when (alignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> -popupContentSize.height
            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> 0
            else -> -popupContentSize.height / 2
        }

        // Compute horizontal position.
        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        // Compute vertical position.
        val toBottom = anchorBounds.top + contentOffsetY
        val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= 0 && it + popupContentSize.height <= windowSize.height
        } ?: toTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}

private fun calculateTransformOrigin(
    parentBounds: IntRect,
    popupBounds: IntRect,
): TransformOrigin {
    val pivotX = when {
        popupBounds.left >= parentBounds.right -> 0f
        popupBounds.right <= parentBounds.left -> 1f
        popupBounds.width == 0 -> 0f
        else -> {
            val intersectionCenter = (max(parentBounds.left, popupBounds.left) +
                    min(parentBounds.right, popupBounds.right)) / 2
            (intersectionCenter - popupBounds.left).toFloat() / popupBounds.width
        }
    }
    val pivotY = when {
        popupBounds.top >= parentBounds.bottom -> 0f
        popupBounds.bottom <= parentBounds.top -> 1f
        popupBounds.height == 0 -> 0f
        else -> {
            val intersectionCenter = (max(parentBounds.top, popupBounds.top) +
                    min(parentBounds.bottom, popupBounds.bottom)) / 2
            (intersectionCenter - popupBounds.top).toFloat() / popupBounds.height
        }
    }
    return TransformOrigin(pivotX, pivotY)
}

// Popup open/close animation.
private const val InTransitionDuration = 120
private const val OutTransitionDuration = 75
