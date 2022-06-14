package com.redhoodhan.draw.draw_option

import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.redhoodhan.draw.DrawViewState
import com.redhoodhan.draw.draw_option.data.PaintOption

/**
 * Note that the eraser mode is really just a NORMAL and SOLID brush with its XferMode set to
 * [PorterDuff.Mode.CLEAR], so that we can keep track of the drawing paths of the eraser without
 * introducing extra lists rather than the previous lists we defined in the [DrawViewState].
 */
class EraserLineStrategy(private val baseStrategy: BaseDrawOptionStrategy) :
    DrawOptionStrategy by baseStrategy {

    override fun updateXferMode(paintOption: PaintOption, brushSize: Float) {
        baseStrategy.updateBrushSize(paintOption, brushSize)

        paintOption.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
}