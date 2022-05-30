package com.redhoodhan.draw.extension

import android.graphics.Canvas

/**
 * This function is called to prevent the Canvas from being transparent when applying the
 * clear PorterDuff Mode, so that the background of the canvas will not be erased when using eraser.
 *
 * [performDraw] is the callback lambda function to perform the actual drawing process.
 */
fun Canvas.drawWithBlendLayer(
    performDraw: (Canvas) -> Unit,
) {

    val count = saveLayer(
        0F, 0F,
        width.toFloat(), height.toFloat(), null
    )

    performDraw.invoke(this)

    restoreToCount(count)
}