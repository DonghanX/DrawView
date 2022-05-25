package com.redhoodhan.drawing.ui.draw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.redhoodhan.drawing.ui.draw.data.draw_option.BrushType
import com.redhoodhan.drawing.ui.draw.data.draw_option.DrawPath
import com.redhoodhan.drawing.ui.draw.data.draw_option.LineType
import com.redhoodhan.drawing.ui.draw.data.draw_option.PaintOption
import com.redhoodhan.drawing.ui.draw.data.path_effect.LineDashPathEffect
import com.redhoodhan.drawing.ui.draw.extension.drawWithBlendLayer


private const val DEFAULT_TOUCH_TOLERANCE = 6f
private const val DEFAULT_STROKE_WIDTH = 20f
private const val DEFAULT_PAINT_COLOR = Color.BLACK
private const val DEFAULT_CANVAS_COLOR = Color.WHITE

private const val TAG = "DrawView"
class DrawView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    // Path of the current draw
    private var drawPath = DrawPath()

    // Paint options for the current draw
    private var drawPaint = PaintOption()

    // Coordinate x for the last move event
    private var curX = 0f

    // Coordinate y for the last move event
    private var curY = 0f

    // Boolean flag to determine the background resource type
    private var isCanvasBackgroundImg = false

    private var _lineType: LineType = LineType.SOLID
        set(value) {
            field = value

            // Resets the corresponding brushType
            brushType = value.getBrushType()

            // Obtains corresponding pathEffect object
            pathEffect = allocatePathEffect(pathEffect, _lineType, _brushSize)
        }

    private var brushType: BrushType = BrushType.NORMAL

    private var pathEffect: PathEffect? = null
        set(value) {
            field = value
            drawPaint.pathEffect = value
        }

    private var _isEraserOn = false
        set(value) {
            field = value
            toggleEraser(value)
        }

    private var _brushSize = DEFAULT_STROKE_WIDTH
        set(value) {
            field = value
            drawPaint.strokeWidth = value
            // Checks if we need to retrieve the pathEffect that is size-variant
            if (_lineType.isSizeVariant()) {
                pathEffect = allocatePathEffect(pathEffect, _lineType, _brushSize)
            }
        }

    private var _drawState: DrawViewState? = null
        set(value) {
            field = value
            field?.stateActionCallback = {
                onStateNotified()
            }

            invalidate()
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

        // Sets the LayerType as LAYER_TYPE_SOFTWARE so that we can use PorterDuff.Xfermode effects
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        _drawState = DrawViewState()
    }

    val drawStateRef
        get() = _drawState!!

    var brushColor = DEFAULT_PAINT_COLOR
        set(value) {
            field = value
            drawPaint.color = value
        }

    var lineType: LineType = _lineType
        set(value) {
            field = value
            _lineType = value
        }
        get() = _lineType

    var brushSize = _brushSize
        set(value) {
            field = value
            _brushSize = value
        }
        get() = _brushSize

    var isEraserOn = _isEraserOn
        set(value) {
            field = value
            _isEraserOn = value
        }
        get() = _isEraserOn

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
     *
     * Note that canvasBackgroundImgRes is mutually exclusive with [canvasBackgroundColor]
     */
    var canvasBackgroundImg: Int? = null
        set(value) {
            field = value
            isCanvasBackgroundImg = (value != null)
            invalidate()
        }

    /**
     * This callback is triggered when the [DrawView] is pressed.
     */
    var drawViewPressCallback: (() -> Unit)? = null

    /**
     * This callback is triggered when there is a state action happened, in order to notify other
     * components whether the undo function is available.
     *
     */
    var undoStateCallback: ((isAvailable: Boolean) -> Unit)? = null

    /**
     * This callback is triggered when there is a state action happened, in order to notify other
     * components whether the redo function is available.
     */
    var redoStateCallback: ((isAvailable: Boolean) -> Unit)? = null

    /**
     * Switches the current brush type to [BrushType.NORMAL] or [BrushType.IMAGE].
     *
     * Note that we call this function as we want to draw something with certain type of brush,
     * instead of erasing our previous work. Thus, the eraser mode should be switched off first to
     * reset XferMode of the corresponding [Paint].
     */
    fun switchBrushType(brushType: BrushType) {
        _isEraserOn = false
        // TODO: Complete BrushType switching logic
        //      Note that this should be done after the actual drawing function is completed
    }

    fun undo() {
        drawStateRef.undo()
        invalidate()
    }

    fun redo() {
        drawStateRef.redo()
        invalidate()
    }

    fun clearCallback() {
        drawViewPressCallback = null
        undoStateCallback = null
        redoStateCallback = null

        drawStateRef.clearCallback()
    }

    /**
     * This function is called when the [DrawViewState.stateActionCallback] is triggered and triggers
     * the callbacks that passes the Boolean flag of corresponding state, by accessing the attribute
     * in the [DrawViewState].
     *
     * The callbacks to be triggered are as follows:
     * 1. [undoStateCallback]
     * 2. [redoStateCallback]
     */
    private fun onStateNotified() {
        undoStateCallback?.invoke(drawStateRef.isUndoAvailable)

        redoStateCallback?.invoke(drawStateRef.isRedoAvailable)
    }

    /**
     * Allocates an existing pathEffect or generates a new pathEffect to assign to [pathEffect]
     * property. [lineType] is utilized to determine which type of pathEffect is needed. [brushSize]
     * serves to recalculate certain properties, like the phase and intervals between lines of
     * [DashPathEffect], so that the final outcome won't be too strange when the [brushSize] is
     * too large.
     *
     * Note that we call this function as we want to draw something with certain type of line,
     * instead of erasing our previous work. Thus, the eraser mode should be switched off first to
     * reset XFerMode of the corresponding [Paint].
     */
    private fun allocatePathEffect(
        pathEffect: PathEffect?,
        lineType: LineType,
        brushSize: Float
    ): PathEffect? {
        _isEraserOn = false

        return when (lineType) {
            LineType.SOLID -> null
            LineType.DASH -> getDashEffectByBrushSize(pathEffect, brushSize)
        }
    }

    private fun getDashEffectByBrushSize(
        pathEffect: PathEffect?,
        brushSize: Float
    ): LineDashPathEffect {
        // Avoids unnecessary creation of DashPathEffect objects by checking if the brushSize
        // remains the same
        pathEffect?.let {
            if (it is LineDashPathEffect && it.brushSize == brushSize) {
                return it
            }
        }

        return LineDashPathEffect(brushSize)
    }

    /**
     * Note that the eraser mode is really just a NORMAL and SOLID brush with its XferMode set to
     * [PorterDuff.Mode.CLEAR], so that we can keep track of the drawing paths of the eraser without
     * introducing extra lists rather than the previous lists we defined in the [DrawViewState].
     */
    private fun toggleEraser(isEraserOn: Boolean) {
        if (isEraserOn) {
            brushType = BrushType.NORMAL
            _lineType = LineType.SOLID

            drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            drawPaint.xfermode = null
        }
    }

    override fun onDraw(canvas: Canvas) {
        setBackground(canvas)

        canvas.drawWithBlendLayer {
            drawPrevious(it)

            drawCurrent(it)
        }

        // Updates flags in DrawState

        super.onDraw(canvas)
    }

    private fun setBackground(canvas: Canvas) {
        if (isCanvasBackgroundImg) {
            canvasBackgroundImg?.let {
                setBackgroundResource(it)
            }
        } else {
            // Removes previous background
            setBackgroundResource(0)
            // draw the pure color background
            canvas.drawColor(canvasBackgroundColor)
        }
    }

    private fun drawCurrent(canvas: Canvas) {
        // TODO: add new features about drawing with ImageBrush using Canvas.drawBitmap function
        canvas.drawPath(drawPath, drawPaint)
    }

    private fun drawPrevious(canvas: Canvas) {
        if (drawStateRef.prevSize == 0) {
            return
        }

        for (index in 0 until drawStateRef.prevSize) {
            canvas.drawPath(
                drawStateRef.getPrevPathWithIndexed(index),
                drawStateRef.getPrevPaintWithIndexed(index)
            )
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawViewPressCallback?.invoke()
                performActionDown(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                performActionMove(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                performActionUp()
            }
            else -> return false
        }

        // Performs drawing process by calling View.draw function
        invalidate()

        return true
    }

    /**
     * Resets the DrawPath object and records the starting coordinates of the following "trail".
     *
     * Note that this function is called when [MotionEvent.ACTION_DOWN] is intercepted
     */
    private fun performActionDown(touchX: Float, touchY: Float) {
        drawPath.reset()
        drawPath.moveTo(touchX, touchY)

        curX = touchX
        curY = touchY
    }

    /**
     * Quads the [drawPath] based on the coordinates of the last move and the coordinates of the
     * current move.
     *
     * Note that this function is called when [MotionEvent.ACTION_MOVE] is intercepted.
     */
    private fun performActionMove(touchX: Float, touchY: Float) {
        drawPath.quadTo(
            curX,
            curY,
            (curX + touchX) / 2,
            (curY + touchY) / 2
        )
        // Updates the coordinates of the last move
        curX = touchX
        curY = touchY
    }

    /**
     * Adds the [drawPath] and the [drawPaint] to the stored lists respectively when the current
     * drawing process is ended.
     *
     * The lists will be used to retrieve the previous [drawPath] and the [drawPaint] when the
     * function [drawPrevious] is called.
     *
     * Note that this function is called when [MotionEvent.ACTION_UP] is intercepted.
     */
    private fun performActionUp() {
        drawStateRef.addToPrev(drawPath, Paint(drawPaint) )

        // Prepares for next draw. Also prevents the drawCurrent function from drawing the same
        // path again and again when the onDraw function is called.
        drawPath = DrawPath()
    }




}