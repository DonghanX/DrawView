package com.redhoodhan.drawing.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

/**
 * TODO: Add click animation (expand)
 */
private const val DEFAULT_RADIUS_FACTOR = 0.35F
private const val SELECTED_RADIUS_FACTOR = 0.5F
private const val DEFAULT_STROKE_WIDTH_FACTOR = 0.1F

class DrawCircleView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private var mPaint = Paint()

    private var mSelectPaint = Paint()

    private var drawRadiusFactor: Float = DEFAULT_RADIUS_FACTOR

    var isCurSelected: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value

            changeCircleRadius(value)
        }

    var drawColorResId: Int = Color.BLACK
        set(value) {
            if (field == value) {
                return
            }
            field = value

            changePaintColor(value)
        }

    private fun changePaintColor(colorResId: Int) {
        ResourcesCompat.getColor(resources, colorResId, null).let {
            mPaint.color = it

            mSelectPaint.color = it
        }
        invalidate()
    }

    private fun changeCircleRadius(isCurSelected: Boolean) {
        drawRadiusFactor = if (isCurSelected) {
            SELECTED_RADIUS_FACTOR
        } else {
            DEFAULT_RADIUS_FACTOR
        }
        invalidate()
    }

    init {
        mPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.BLACK
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            width * drawRadiusFactor,
            mPaint
        )
        super.onDraw(canvas)
    }
}