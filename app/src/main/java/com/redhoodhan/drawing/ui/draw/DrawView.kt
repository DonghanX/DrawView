package com.redhoodhan.drawing.ui.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.redhoodhan.drawing.ui.draw.data.*


private const val DEFAULT_TOUCH_TOLERANCE = 6f
private const val DEFAULT_STROKE_WIDTH = 6f
private const val DEFAULT_PAINT_COLOR = Color.BLACK
private const val DEFAULT_CANVAS_COLOR = Color.WHITE

class DrawView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private var drawPaint = Paint()

    /**
     * Boolean flag that is utilized to determine the background resource type
     */
    private var isCanvasBackgroundImg = false

    private var curLineType: @LineType Int = SOLID_LINE

    private var brushType: @BrushType Int = NORMAL_BRUSH

    private var dashEffect: LineDashPathEffect? = null
        set(value) {
            field = value
            drawPaint.pathEffect = value
        }

    private var _isEraserOn = false
        set(value) {
            field = value
            switchOnEraser(value)
        }

    private var _brushSize = DEFAULT_STROKE_WIDTH
        set(value) {
            field = value
            drawPaint.strokeWidth = value
            // changes the dashed gap when using dashed brush
            if (curLineType == DASHED_LINE) {
                dashEffect = getDashEffectByBrushSize(value)
            }
        }

    init {
        drawPaint.apply {
            color = DEFAULT_PAINT_COLOR
            strokeWidth = DEFAULT_STROKE_WIDTH
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

    }

    var paintColor = DEFAULT_PAINT_COLOR
        set(value) {
            field = value
            drawPaint.color = value
        }

    var brushSize = DEFAULT_STROKE_WIDTH
        set(value) {
            field = value
            _brushSize = value
        }

    var isEraserOn = _isEraserOn
        set(value) {
            field = value
            _isEraserOn = value
        }


    /**
     * Note that the eraser mode is really a NORMAL brush with its XferMode set to
     * [PorterDuff.Mode.CLEAR], so that we can keep track of the drawing paths of the eraser without
     * introducing extra lists rather than [drawPathList] and [drawPropertyList].
     * Hence, when we switch on the eraser mode, the [brushType] should be set to [NORMAL_BRUSH] as well.
     */
    private fun switchOnEraser(isEraserOn: Boolean) {
        if (_isEraserOn) {
            brushType = NORMAL_BRUSH
            drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            drawPaint.xfermode = null
        }
    }

    /**
     * Switches the current brush type to [NORMAL_BRUSH] or [IMAGE_BRUSH].
     * Note that we call this function as we want to draw something with certain type of brush,
     * instead of erasing our previous work. Thus, the eraser mode should be switched off first to
     * reset XferMode of the corresponding [Paint].
     */
    fun switchBrushType(brushType: @BrushType Int) {
        _isEraserOn = false
        // TODO: 4/23/2022
    }

    /**
     * Switches LineType of the drawing brush and implements the necessarily attaching operations,
     * such as changing the dashed gap according to current brushSize as LineType is switched from
     * [SOLID_LINE] to [DASHED_LINE]
     */
    fun switchLineType(@LineType lineType: Int, brushSize: Float) {
        curLineType = lineType

        when (lineType) {
            @LineType SOLID_LINE -> {

            }
            @LineType DASHED_LINE -> {
                _brushSize = brushSize
            }
        }
    }

    private fun getDashEffectByBrushSize(brushSize: Float): LineDashPathEffect {
        dashEffect?.let {
            // reuses the current LineDashEffect object
            if (it.brushSize == brushSize) {
                return it
            }
        }

        return LineDashPathEffect(brushSize)
    }

    /**
     * Color resource of the drawing canvas background
     */
    var canvasBackgroundColor = DEFAULT_CANVAS_COLOR
        set(value) {
            field = value
            isCanvasBackgroundImg = false
            invalidate()
        }
    /**
     * Image resource of the drawing canvas background
     * Note that canvasBackgroundImgRes is mutually exclusive with [canvasBackgroundColor]
     */
    var canvasBackgroundImg: Int? = null
        set(value) {
            field = value
            isCanvasBackgroundImg = true
            invalidate()
        }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}