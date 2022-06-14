package com.redhoodhan.draw.draw_option

import android.graphics.Paint
import com.redhoodhan.draw.draw_option.data.PaintOption

interface DrawOptionStrategy {

    fun updateAlpha(paintOption: PaintOption)

    fun updateBrushColor(paintOption: PaintOption, color: Int)

    fun updateStrokeStyle(paintOption: PaintOption)

    fun updatePathEffect(paintOption: PaintOption, brushSize: Float? = null)

    fun updateBrushSize(paintOption: PaintOption, brushSize: Float)

    fun updateXferMode(paintOption: PaintOption, brushSize: Float)
}