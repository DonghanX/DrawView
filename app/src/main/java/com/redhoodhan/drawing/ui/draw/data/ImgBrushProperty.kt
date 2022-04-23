package com.redhoodhan.drawing.ui.draw.data

import android.graphics.Bitmap

/**
 * Data class that stores the property of image brush.
 * TODO:
 */
data class ImgBrushProperty(
    val discreteLocation: MutableList<Pair<Float, Float>> = mutableListOf(),
    var brushBmp: Bitmap? = null,
)
