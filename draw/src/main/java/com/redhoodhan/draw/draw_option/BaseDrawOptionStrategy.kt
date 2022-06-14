package com.redhoodhan.draw.draw_option

import android.graphics.Paint
import com.redhoodhan.draw.draw_option.data.DrawConst
import com.redhoodhan.draw.draw_option.data.PaintOption
import com.redhoodhan.draw.draw_option.data.LineType

/**
 * This base strategy class implements the default actions of updating the property in the
 * [PaintOption], following the actions of [LineType.SOLID]
 */
class BaseDrawOptionStrategy : DrawOptionStrategy {
    override fun updateAlpha(paintOption: PaintOption) {
        paintOption.paint.alpha = DrawConst.MAX_ALPHA
    }

    override fun updateBrushColor(paintOption: PaintOption, color: Int) {
        paintOption.paint.color = color
    }

    override fun updateStrokeStyle(paintOption: PaintOption) {
        paintOption.paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    }

    override fun updateBrushSize(paintOption: PaintOption, brushSize: Float) {
        paintOption.paint.strokeWidth = brushSize
    }

    override fun updatePathEffect(paintOption: PaintOption, brushSize: Float?) {
        paintOption.paint.pathEffect = null
    }

    override fun updateXferMode(paintOption: PaintOption, brushSize: Float) {
        paintOption.paint.xfermode = null
        updateBrushSize(paintOption, brushSize)
    }
}