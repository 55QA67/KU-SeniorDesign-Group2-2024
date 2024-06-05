package com.ce491.safe_ride

import android.graphics.RectF

class ObjectTracker {
    private var passCount = 0
    private val previousBoxes = mutableListOf<TrackedObject>()

    fun trackObjects(detections: List<RectF>) {
        for (detection in detections) {
            val previousBox = findPreviousBox(detection)
            if (previousBox != null) {
                if (movedFromRightToMiddle(previousBox.box, detection)) {
                    passCount++
                } else if (movedFromMiddleToRight(previousBox.box, detection)) {
                    passCount--
                }
                previousBox.updateBox(detection)
            } else {
                previousBoxes.add(TrackedObject(detection))
            }
        }

        // Update previous boxes: remove old ones no longer detected
        previousBoxes.retainAll { it.isTracked }
    }

    private fun findPreviousBox(current: RectF): TrackedObject? {
        // Iterate over each previously tracked object
        for (previousBox in previousBoxes) {
            // Check if the current detection represents the same object as the previously tracked object
            if (previousBox.isSameObject(current)) {
                // If they represent the same object, return the previously tracked object
                return previousBox
            }
        }

        // If no match is found, return null
        return null
    }

    private fun movedFromRightToMiddle(previous: RectF, current: RectF): Boolean {
        return getCenterX(previous) > MIDDLE_THRESHOLD && getCenterX(current) <= MIDDLE_THRESHOLD
    }

    private fun movedFromMiddleToRight(previous: RectF, current: RectF): Boolean {
        return getCenterX(previous) <= MIDDLE_THRESHOLD && getCenterX(current) > MIDDLE_THRESHOLD
    }

    private fun getCenterX(box: RectF): Float {
        return (box.left + box.right) / 2
    }

    fun getPassCount(): Int {
        return passCount
    }

    companion object {
        const val MIDDLE_THRESHOLD = 0.5f // Middle threshold as a proportion of the screen width
    }

    private class TrackedObject(var box: RectF) {
        var isTracked: Boolean = true

        fun updateBox(newBox: RectF) {
            box = newBox
            isTracked = true
        }

        fun isSameObject(newBox: RectF): Boolean {
            // Calculate the intersection of the current box and the new box
            val intersection = RectF()
            val isIntersect = intersection.setIntersect(this.box, newBox)

            // If the boxes do not intersect, they cannot be the same object
            if (!isIntersect) {
                return false
            }

            // Calculate the area of the intersection
            val intersectionArea = intersection.width() * intersection.height()

            // Calculate the area of both boxes
            val boxArea = this.box.width() * this.box.height()
            val newBoxArea = newBox.width() * newBox.height()

            // Calculate the IoU
            val iou = intersectionArea / (boxArea + newBoxArea - intersectionArea)

            // If the IoU is above a certain threshold, consider the boxes as the same object
            return iou > IOU_THRESHOLD
        }

        private fun calculateIoU(boxA: RectF, boxB: RectF): Float {
            val intersection = RectF()
            if (intersection.setIntersect(boxA, boxB)) {
                val intersectionArea = intersection.width() * intersection.height()
                val boxAArea = boxA.width() * boxA.height()
                val boxBArea = boxB.width() * boxB.height()
                return intersectionArea / (boxAArea + boxBArea - intersectionArea)
            }
            return 0f
        }

        companion object {
            const val IOU_THRESHOLD = 0.5f // Threshold for considering two boxes as the same object
        }
    }
}
