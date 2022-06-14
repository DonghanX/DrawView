package com.redhoodhan.draw.draw_option

import com.redhoodhan.draw.draw_option.data.PaintOption
import com.redhoodhan.draw.draw_option.data.LineType
import com.redhoodhan.draw.path_effect.LineDashPathEffect


class DashedLineStrategy(private val baseStrategy: BaseDrawOptionStrategy) :
    DrawOptionStrategy by baseStrategy {

    /**
     * Note that we will need the [brushSize] to specify the intervals and phase of the dash path
     * when we switch to the [LineType.DASH].
     */
    override fun updatePathEffect(paintOption: PaintOption, brushSize: Float?) {
        requireNotNull(brushSize)

        paintOption.paint.pathEffect = LineDashPathEffect(brushSize)
    }

    override fun updateBrushSize(paintOption: PaintOption, brushSize: Float) {
        baseStrategy.updateBrushSize(paintOption, brushSize)

        updatePathEffect(paintOption, brushSize)
    }
}