package com.redhoodhan.draw.draw_option

import android.util.Log
import com.redhoodhan.draw.draw_option.data.LineType
import com.redhoodhan.draw.draw_option.data.PaintOption

class DrawOptionContext() {

    private var baseStrategy = BaseDrawOptionStrategy()
    private var strategy: DrawOptionStrategy

    init {
        strategy = baseStrategy
    }

    fun changeStrategyByLineType(lineType: LineType) {
        strategy = when (lineType) {
            LineType.DASH -> DashedLineStrategy(baseStrategy)
            LineType.CHISEL -> ChiselTipLineStrategy(baseStrategy)
            LineType.ERASER -> EraserLineStrategy(baseStrategy)
            else -> baseStrategy
        }
    }

    fun updateAlpha(paintOption: PaintOption) {
        strategy.updateAlpha(paintOption)
    }

    fun updateBrushColor(paintOption: PaintOption, color: Int) {
        strategy.updateBrushColor(paintOption, color)
    }

    fun updateStrokeStyle(paintOption: PaintOption) {
        strategy.updateStrokeStyle(paintOption)
    }

    fun updatePathEffect(paintOption: PaintOption, brushSize: Float? = null) {
        strategy.updatePathEffect(paintOption, brushSize)
    }

    fun updateBrushSize(paintOption: PaintOption, brushSize: Float) {
        strategy.updateBrushSize(paintOption, brushSize)
    }

    fun updateXferMode(paintOption: PaintOption, brushSize: Float) {
        strategy.updateXferMode(paintOption, brushSize)
    }

    fun updateOptionWhenSwitchingLineType(paintOption: PaintOption, brushSize: Float) {
        updateXferMode(paintOption, brushSize)

        updatePathEffect(paintOption, brushSize)

        updateAlpha(paintOption)

        updateStrokeStyle(paintOption)
    }

}