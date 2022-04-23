package com.redhoodhan.drawing.ui.draw.data

import android.graphics.DashPathEffect
import java.util.*

/**
 * Note that we changed the dashed gap according to current brush size
 */
class LineDashPathEffect(val brushSize: Float) :
    DashPathEffect(floatArrayOf(brushSize, brushSize * 2, brushSize, brushSize * 3), 0f) {

    override fun equals(other: Any?): Boolean {
        return if (this === other) {
            true
        } else {
            (other is LineDashPathEffect) && (other.brushSize == brushSize)
        }
    }

    override fun hashCode(): Int = Objects.hash(brushSize)

}