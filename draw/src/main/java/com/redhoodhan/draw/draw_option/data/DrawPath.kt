package com.redhoodhan.draw.draw_option.data

import android.graphics.Path

typealias CoordPair = Pair<Float, Float>

data class DrawPath(
    val brushType: BrushType = BrushType.NORMAL,
    var coordPair: CoordPair? = null
): Path()