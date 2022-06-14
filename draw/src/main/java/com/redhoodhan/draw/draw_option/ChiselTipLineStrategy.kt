package com.redhoodhan.draw.draw_option

import android.graphics.Paint
import com.redhoodhan.draw.draw_option.data.DrawConst
import com.redhoodhan.draw.draw_option.data.PaintOption

class ChiselTipLineStrategy(private val baseStrategy: BaseDrawOptionStrategy) :
    DrawOptionStrategy by baseStrategy {

    override fun updateAlpha(paintOption: PaintOption) {
        paintOption.paint.alpha = DrawConst.CHISEL_ALPHA
    }

    override fun updateStrokeStyle(paintOption: PaintOption) {
        paintOption.paint.apply {
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.BEVEL
        }
    }

    override fun updateBrushColor(paintOption: PaintOption, color: Int) {
        baseStrategy.updateBrushColor(paintOption, color)
        // Retrieves the alpha value overridden by setting color
        updateAlpha(paintOption)
    }
}