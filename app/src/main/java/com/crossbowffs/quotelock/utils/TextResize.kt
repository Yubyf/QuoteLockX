/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crossbowffs.quotelock.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.transition.Transition
import androidx.transition.TransitionValues
import kotlin.math.roundToInt

/**
 * Transitions a TextView from one font size to another. This does not
 * do any animation of TextView content and if the text changes, this
 * transition will not run.
 *
 *
 * The animation works by capturing a bitmap of the text at the start
 * and end states. It then scales the start bitmap until it reaches
 * a threshold and switches to the scaled end bitmap for the remainder
 * of the animation. This keeps the jump in bitmaps in the middle of
 * the animation, where it is less noticeable than at the beginning
 * or end of the animation. This transition does not work well with
 * cropped text. TextResize also does not work with changes in
 * TextView gravity.
 *
 * Reference: [link](https://github.com/android/animation-samples/blob/main/Unsplash/app/src/main/java/com/example/android/unsplash/transition/TextResize.java)
 */
class TextResize : Transition {
    constructor() {
        addTarget(TextView::class.java)
    }

    /**
     * Constructor used from XML.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        addTarget(TextView::class.java)
    }

    override fun getTransitionProperties(): Array<String> {
        return PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        if (transitionValues.view !is TextView) {
            return
        }
        val view = transitionValues.view as TextView
        val fontSize = view.textSize
        transitionValues.values[FONT_SIZE] = fontSize
        val data = TextResizeData(view)
        transitionValues.values[DATA] = data
    }

    override fun createAnimator(
        sceneRoot: ViewGroup, startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }
        val startData = startValues.values[DATA] as TextResizeData?
        val endData = endValues.values[DATA] as TextResizeData?
        if (startData!!.gravity != endData!!.gravity) {
            return null // Can't deal with changes in gravity
        }
        val textView = endValues.view as TextView
        var startFontSize = startValues.values[FONT_SIZE] as Float
        // Capture the start bitmap -- we need to set the values to the start values first
        setTextViewData(textView, startData, startFontSize)
        val startWidth = textView.paint.measureText(textView.text.toString())
        val startBitmap = captureTextBitmap(textView)
        if (startBitmap == null) {
            startFontSize = 0f
        }
        var endFontSize = endValues.values[FONT_SIZE] as Float

        // Set the values to the end values
        setTextViewData(textView, endData, endFontSize)
        val endWidth = textView.paint.measureText(textView.text.toString())

        // Capture the end bitmap
        val endBitmap = captureTextBitmap(textView)
        if (endBitmap == null) {
            endFontSize = 0f
        }
        if (startFontSize == 0f && endFontSize == 0f) {
            return null // Can't animate null bitmaps
        }

        // Set the colors of the TextView so that nothing is drawn.
        // Only draw the bitmaps in the overlay.
        val textColors = textView.textColors
        val hintColors = textView.hintTextColors
        val highlightColor = textView.highlightColor
        val linkColors = textView.linkTextColors
        textView.setTextColor(Color.TRANSPARENT)
        textView.setHintTextColor(Color.TRANSPARENT)
        textView.highlightColor = Color.TRANSPARENT
        textView.setLinkTextColor(Color.TRANSPARENT)

        // Create the drawable that will be animated in the TextView's overlay.
        // Ensure that it is showing the start state now.
        val drawable = SwitchBitmapDrawable(
            textView, startData.gravity,
            startBitmap, startFontSize, startWidth, endBitmap, endFontSize, endWidth
        )
        textView.overlay.add(drawable)

        // Properties: left, top, font size, text color
        val leftProp = PropertyValuesHolder.ofFloat(
            "left",
            startData.paddingLeft.toFloat(),
            endData.paddingLeft.toFloat()
        )
        val topProp = PropertyValuesHolder.ofFloat(
            "top",
            startData.paddingTop.toFloat(),
            endData.paddingTop.toFloat()
        )
        val rightProp = PropertyValuesHolder.ofFloat(
            "right",
            (startData.width - startData.paddingRight).toFloat(),
            (endData.width - endData.paddingRight).toFloat()
        )
        val bottomProp = PropertyValuesHolder.ofFloat(
            "bottom",
            (startData.height - startData.paddingBottom).toFloat(),
            (endData.height - endData.paddingBottom).toFloat()
        )
        val fontSizeProp = PropertyValuesHolder.ofFloat(
            "fontSize",
            startFontSize, endFontSize
        )
        val animator: ObjectAnimator = if (startData.textColor != endData.textColor) {
            val textColorProp = PropertyValuesHolder.ofObject(
                "textColor",
                ArgbEvaluator(), startData.textColor, endData.textColor
            )
            ObjectAnimator.ofPropertyValuesHolder(
                drawable,
                leftProp, topProp, rightProp, bottomProp, fontSizeProp, textColorProp
            )
        } else {
            ObjectAnimator.ofPropertyValuesHolder(
                drawable,
                leftProp, topProp, rightProp, bottomProp, fontSizeProp
            )
        }
        val finalFontSize = endFontSize
        val listener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                textView.overlay.remove(drawable)
                textView.setTextColor(textColors)
                textView.setHintTextColor(hintColors)
                textView.highlightColor = highlightColor
                textView.setLinkTextColor(linkColors)
            }

            override fun onAnimationPause(animation: Animator) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, drawable.fontSize)
                val paddingLeft = drawable.getLeft().roundToInt()
                val paddingTop = drawable.getTop().roundToInt()
                val fraction = animator.animatedFraction
                val paddingRight = interpolate(
                    startData.paddingRight.toFloat(),
                    endData.paddingRight.toFloat(), fraction
                ).roundToInt()
                val paddingBottom = interpolate(
                    startData.paddingBottom.toFloat(),
                    endData.paddingBottom.toFloat(), fraction
                ).roundToInt()
                textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                textView.setTextColor(drawable.textColor)
            }

            override fun onAnimationResume(animation: Animator) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalFontSize)
                textView.setPadding(
                    endData.paddingLeft, endData.paddingTop,
                    endData.paddingRight, endData.paddingBottom
                )
                textView.setTextColor(endData.textColor)
            }
        }
        animator.addListener(listener)
        animator.addPauseListener(listener)
        return animator
    }

    /**
     * This Drawable is used to scale the start and end bitmaps and switch between them
     * at the appropriate progress.
     */
    private class SwitchBitmapDrawable(
        private val view: TextView, gravity: Int,
        startBitmap: Bitmap?, startFontSize: Float, startWidth: Float,
        endBitmap: Bitmap?, endFontSize: Float, endWidth: Float,
    ) : Drawable() {
        private val horizontalGravity: Int
        private val verticalGravity: Int
        private val startBitmap: Bitmap?
        private val endBitmap: Bitmap?
        private val paint = Paint()
        private val startFontSize: Float
        private val endFontSize: Float
        private val startWidth: Float
        private val endWidth: Float

        /**
         * The font size in pixels of the scaled bitmap text.
         */
        var fontSize = 0f
            set(fontSize) {
                field = fontSize
                invalidateSelf()
            }
        private var left = 0f
        private var top = 0f
        private var right = 0f
        private var bottom = 0f

        /**
         * The color of the text being displayed.
         */
        @Keep
        var textColor = 0
            set(textColor) {
                field = textColor
                colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    textColor, BlendModeCompat.SRC_IN
                )
                invalidateSelf()
            }

        override fun invalidateSelf() {
            super.invalidateSelf()
            view.invalidate()
        }

        /**
         * Sets the left side of the text. This should be the same as the left padding.
         *
         * @param left The left side of the text in pixels.
         */
        fun setLeft(left: Float) {
            this.left = left
            invalidateSelf()
        }

        /**
         * Sets the top of the text. This should be the same as the top padding.
         *
         * @param top The top of the text in pixels.
         */
        fun setTop(top: Float) {
            this.top = top
            invalidateSelf()
        }

        /**
         * Sets the right of the drawable.
         *
         * @param right The right pixel of the drawn area.
         */
        fun setRight(right: Float) {
            this.right = right
            invalidateSelf()
        }

        /**
         * Sets the bottom of the drawable.
         *
         * @param bottom The bottom pixel of the drawn area.
         */
        fun setBottom(bottom: Float) {
            this.bottom = bottom
            invalidateSelf()
        }

        /**
         * @return The left side of the text.
         */
        fun getLeft(): Float {
            return left
        }

        /**
         * @return The top of the text.
         */
        fun getTop(): Float {
            return top
        }

        /**
         * @return The right side of the text.
         */
        fun getRight(): Float {
            return right
        }

        /**
         * @return The bottom of the text.
         */
        fun getBottom(): Float {
            return bottom
        }

        override fun draw(canvas: Canvas) {
            val saveCount = canvas.save()
            // The threshold changes depending on the target font sizes. Because scaled-up
            // fonts look bad, we want to switch when closer to the smaller font size. This
            // algorithm ensures that null bitmaps (font size = 0) are never used.
            val threshold = startFontSize / (startFontSize + endFontSize)
            val fontSize = fontSize
            val progress = (fontSize - startFontSize) / (endFontSize - startFontSize)

            // The drawn text width is a more accurate scale than font size. This avoids
            // jump when switching bitmaps.
            val expectedWidth = interpolate(startWidth, endWidth, progress)
            if (progress < threshold) {
                // draw start bitmap
                val scale = expectedWidth / startWidth
                val tx = getTranslationPoint(
                    horizontalGravity, left, right,
                    startBitmap!!.width.toFloat(), scale
                )
                val ty = getTranslationPoint(
                    verticalGravity, top, bottom,
                    startBitmap.height.toFloat(), scale
                )
                canvas.translate(tx, ty)
                canvas.scale(scale, scale)
                canvas.drawBitmap(startBitmap, 0f, 0f, paint)
            } else {
                // draw end bitmap
                val scale = expectedWidth / endWidth
                val tx = getTranslationPoint(
                    horizontalGravity, left, right,
                    endBitmap!!.width.toFloat(), scale
                )
                val ty = getTranslationPoint(
                    verticalGravity, top, bottom,
                    endBitmap.height.toFloat(), scale
                )
                canvas.translate(tx, ty)
                canvas.scale(scale, scale)
                canvas.drawBitmap(endBitmap, 0f, 0f, paint)
            }
            canvas.restoreToCount(saveCount)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        private fun getTranslationPoint(
            gravity: Int, start: Float, end: Float, dim: Float,
            scale: Float,
        ): Float {
            return when (gravity) {
                Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL -> (start + end - dim * scale) / 2f
                Gravity.RIGHT, Gravity.BOTTOM -> end - dim * scale
                Gravity.LEFT, Gravity.TOP -> start
                else -> start
            }
        }

        init {
            horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
            verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
            this.startBitmap = startBitmap
            this.endBitmap = endBitmap
            this.startFontSize = startFontSize
            this.endFontSize = endFontSize
            this.startWidth = startWidth
            this.endWidth = endWidth
        }
    }

    /**
     * Contains all the non-font-size data used by the TextResize transition.
     * None of these values should trigger the transition, so they are not listed
     * in PROPERTIES. These are captured together to avoid boxing of all the
     * primitives while adding to TransitionValues.
     */
    internal class TextResizeData(textView: TextView) {
        val paddingLeft: Int
        val paddingTop: Int
        val paddingRight: Int
        val paddingBottom: Int
        val width: Int
        val height: Int
        val gravity: Int
        val textColor: Int

        init {
            paddingLeft = textView.paddingLeft
            paddingTop = textView.paddingTop
            paddingRight = textView.paddingRight
            paddingBottom = textView.paddingBottom
            width = textView.width
            height = textView.height
            gravity = textView.gravity
            textColor = textView.currentTextColor
        }
    }

    companion object {
        private const val FONT_SIZE = "TextResize:fontSize"
        private const val DATA = "TextResize:data"
        private val PROPERTIES =
            arrayOf( // We only care about FONT_SIZE. If anything else changes, we don't
                // want this transition to be called to create an Animator.
                FONT_SIZE
            )

        private fun setTextViewData(view: TextView, data: TextResizeData?, fontSize: Float) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            view.setPadding(
                data!!.paddingLeft,
                data.paddingTop,
                data.paddingRight,
                data.paddingBottom
            )
            view.right = view.left + data.width
            view.bottom = view.top + data.height
            view.setTextColor(data.textColor)
            val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            view.layout(view.left, view.top, view.right, view.bottom)
        }

        private fun captureTextBitmap(textView: TextView): Bitmap? {
            val background = textView.background
            textView.background = null
            val width = textView.width - textView.paddingLeft - textView.paddingRight
            val height = textView.height - textView.paddingTop - textView.paddingBottom
            if (width == 0 || height == 0) {
                return null
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.translate(-textView.paddingLeft.toFloat(), -textView.paddingTop.toFloat())
            textView.draw(canvas)
            textView.background = background
            return bitmap
        }

        private fun interpolate(start: Float, end: Float, fraction: Float): Float {
            return start + fraction * (end - start)
        }
    }
}