/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ce491.safe_ride

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.ce491.safe_ride.ml.DetectWithMetadata
import java.util.LinkedList
import kotlin.math.max

class OverlayView(context: Context?) : View(context) {

    private var results: MutableList<DetectWithMetadata.DetectionResult> = LinkedList<DetectWithMetadata.DetectionResult>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f

    private var bounds = Rect()

    private var edgeThreshold = 50
    private val lastKnownPositions = mutableMapOf<Int, RectF>()



    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.purple_500)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        var personDetected = false

        for (result in results) {
            if (result.scoreAsFloat > 0.75f && result.categoryAsString == "Person") {
                personDetected = true
                inWay.value = false
                val boundingBox = result.locationAsRectF

                // Scale the bounding box coordinates to match the view size
                val top = boundingBox.top * scaleFactor
                val bottom = boundingBox.bottom * scaleFactor
                val left = boundingBox.left * scaleFactor
                val right = boundingBox.right * scaleFactor

                // Draw bounding box around detected objects
                val drawableRect = RectF(left, top, right, bottom)
                canvas.drawRect(drawableRect, boxPaint)

                // Create text to display alongside detected objects
                val drawableText =
                    result.categoryAsString + " " +
                            String.format("%.2f", result.scoreAsFloat * 100) + "%"

                // Draw rect behind display text
                textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
                val textWidth = bounds.width()
                val textHeight = bounds.height()
                canvas.drawRect(
                    left,
                    top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
                )

                // Draw text for detected object
                canvas.drawText(drawableText, left, top + bounds.height(), textPaint)

                // Check if the person is detected and moving off the screen to the right
                if (right > width - edgeThreshold) {
                    inWay.value = true
                    passCount.intValue++
                }
            }
        }

        if (!personDetected) {
            inWay.value = true
        }
    }

    fun updateResults(
        detectionResults: MutableList<DetectWithMetadata.DetectionResult>,
        imageWidth: Int,
        imageHeight: Int
    ) {
        results = detectionResults

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)

        // Redraw the overlay
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
