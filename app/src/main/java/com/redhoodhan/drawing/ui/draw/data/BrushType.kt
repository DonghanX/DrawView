package com.redhoodhan.drawing.ui.draw.data

import androidx.annotation.IntDef

// Note that the NORMAL brush type includes "Eraser", "Solid" and "Dashed"
const val NORMAL_BRUSH = 1000

const val IMAGE_BRUSH = 1001

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPE,
    AnnotationTarget.EXPRESSION
)
@IntDef(NORMAL_BRUSH, IMAGE_BRUSH)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class BrushType