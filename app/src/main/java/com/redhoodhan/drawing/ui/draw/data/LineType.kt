package com.redhoodhan.drawing.ui.draw.data

import androidx.annotation.IntDef

const val SOLID_LINE = 2001
const val DASHED_LINE = 2002

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.EXPRESSION,
    AnnotationTarget.TYPE
)
@IntDef(SOLID_LINE, DASHED_LINE)
annotation class LineType