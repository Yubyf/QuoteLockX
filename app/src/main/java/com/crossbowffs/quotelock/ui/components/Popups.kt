package com.crossbowffs.quotelock.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
    popped: Boolean,
    onDismissRequest: () -> Unit,
    anchor: DpOffset? = DpOffset(0.dp, 0.dp),
    alignment: Alignment = Alignment.TopStart,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable BoxScope.() -> Unit,
) {
    val poppedStates = remember { MutableTransitionState(false) }
    poppedStates.targetState = popped
    if (poppedStates.currentState || poppedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider = AnchorPopupPositionProvider(
            anchor,
            alignment,
            density
        ) { parentBounds, popupBounds ->
            transformOriginState.value = calculateTransformOrigin(parentBounds, popupBounds)
        }

        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            AnchorPopupContent(
                poppedStates = poppedStates,
                transformOriginState = transformOriginState,
                content = content
            )
        }
    }
}

@Composable
private fun AnchorPopupContent(
    poppedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    content: @Composable BoxScope.() -> Unit,
) {
    // Popup open/close animation.
    val transition = updateTransition(poppedStates, "DropDownPopup")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to popped
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Popped to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }, label = "ScaleAnimation"
    ) {
        if (it) {
            // Popup is popped.
            1f
        } else {
            // Popup is dismissed.
            0.8f
        }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to popped
                tween(durationMillis = 30)
            } else {
                // Popped to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }, label = "AlphaAnimation"
    ) {
        if (it) {
            // Popup is popped.
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
    val anchorPosition: DpOffset?,
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
        val contentOffsetX: Int
        val contentOffsetY: Int
        if (anchorPosition != null) {
            contentOffsetX = with(density) { anchorPosition.x.roundToPx() } + when (alignment) {
                Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> -popupContentSize.width
                Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> 0
                else -> -popupContentSize.width / 2
            }
            contentOffsetY = with(density) { anchorPosition.y.roundToPx() } + when (alignment) {
                Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> -popupContentSize.height
                Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> 0
                else -> -popupContentSize.height / 2
            }
        } else {
            // If no anchor position offset is specified, then the popup is positioned with the alignment in the parent.
            var popupPosition = IntOffset(0, 0)

            // Get the aligned point inside the parent
            val parentAlignmentPoint = alignment.align(
                IntSize.Zero,
                IntSize(anchorBounds.width, anchorBounds.height),
                layoutDirection
            )
            // Get the aligned point inside the child
            val relativePopupPos = alignment.align(
                IntSize.Zero,
                IntSize(popupContentSize.width, popupContentSize.height),
                layoutDirection
            )
            // Add the distance between the parent's top left corner and the alignment point
            popupPosition += parentAlignmentPoint

            // Subtract the distance between the children's top left corner and the alignment point
            popupPosition -= IntOffset(relativePopupPos.x, relativePopupPos.y)
            contentOffsetX = popupPosition.x
            contentOffsetY = popupPosition.y
        }

        // Compute horizontal position.
        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight.coerceAtLeast(0),
                toLeft.coerceAtMost(windowSize.width - popupContentSize.width),
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft.coerceAtLeast(0),
                toRight.coerceAtMost(windowSize.width - popupContentSize.width),
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
        val y = sequenceOf(
            when {
                toBottom < 0 -> 0
                toBottom + popupContentSize.height > windowSize.height ->
                    windowSize.height - popupContentSize.height

                else -> toBottom
            },
            when {
                toTop < 0 -> 0
                toTop + popupContentSize.height > windowSize.height ->
                    windowSize.height - popupContentSize.height

                else -> toTop
            },
            toCenter,
            toDisplayBottom
        ).firstOrNull {
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
