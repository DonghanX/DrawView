package com.redhoodhan.draw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.redhoodhan.draw.data.draw_option.BrushType
import com.redhoodhan.draw.data.draw_option.DrawPath
import com.redhoodhan.draw.data.draw_option.LineType
import com.redhoodhan.draw.data.draw_option.PaintOption
import com.redhoodhan.draw.data.path_effect.LineDashPathEffect
import com.redhoodhan.draw.extension.drawWithBlendLayer
import kotlin.math.abs
import kotlin.math.log10

const val DEFAULT_MIN_STROKE_WIDTH = 3F
const val DEFAULT_STROKE_WIDTH = 15F
private const val DEFAULT_PAINT_COLOR = Color.BLACK
private const val DEFAULT_CANVAS_COLOR = Color.WHITE
private const val MAX_STROKE_WIDTH_BIAS = 5F
private const val CHISEL_ALPHA = 80
private const val MAX_ALPHA = 255

private const val TAG = "DrawView"

class DrawView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

//    // Double buffering canvas
//    private var bufferCanvas: Canvas? = null
//
//    // Double buffering bitmap
//    private var bufferBitmap: Bitmap? = null

    // Path of the current draw
    private var drawPath = DrawPath()

    // Paint options for the current draw
    private var drawPaintOption = PaintOption(Paint())

    // Coordinate x for the last move event
    private var curX = 0f

    // Coordinate y for the last move event
    private var curY = 0f

    // Boolean flag to determine the background resource type
    private var isCanvasBackgroundImg = false

    private var isCanvasBackgroundChanged = false

    private var _lineType: LineType = LineType.SOLID
        set(value) {
            field = value

            modifyLineOptions(value)
        }

    private var brushType: BrushType = BrushType.NORMAL

    private var pathEffect: PathEffect? = null
        set(value) {
            field = value
            drawPaintOption.paint.pathEffect = value
        }

    private var _isEraserOn = false
        set(value) {
            field = value
            toggleEraser(value)
        }

    private var _brushSize = DEFAULT_STROKE_WIDTH
        set(value) {
            field = if (value <= DEFAULT_MIN_STROKE_WIDTH) {
                DEFAULT_MIN_STROKE_WIDTH
            } else {
                value
            }
            changeStrokeWidthSrc(false)
            updatePathEffect(_lineType)
        }

    private var _eraserSize = DEFAULT_STROKE_WIDTH
        set(value) {
            field = if (value <= DEFAULT_MIN_STROKE_WIDTH) {
                DEFAULT_MIN_STROKE_WIDTH
            } else {
                value
            }
            changeStrokeWidthSrc(true)
            Log.e(TAG, "eraserSizeSetterPerform:$value ")
        }

    private var velocityTracker: VelocityTracker? = null

    private var needsWidthBias: Boolean = false

    private var _drawState: DrawViewState? = null
        set(value) {
            field = value
            field?.stateActionCallback = {
                onStateNotified()
            }

            invalidate()
        }

    init {
        initDrawPaint()

        _drawState = DrawViewState()
    }

    val drawStateRef
        get() = _drawState!!

    var brushColor = DEFAULT_PAINT_COLOR
        set(value) {
            field = value
            updateBrushColor(_lineType, value)
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

    var eraserSize = _eraserSize
        set(value) {
            field = value
            _eraserSize = value
        }
        get() = _eraserSize

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
            isCanvasBackgroundChanged = true
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
            isCanvasBackgroundChanged = true
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

    fun clearCanvas(needsSaving: Boolean) {
        with(drawStateRef) {
            if (needsSaving) {
                clearCanvasWithSaving()
            } else {
                clearCanvas()
            }
        }
        invalidate()
    }

    /**
     * Gets a bitmap that stores the content in the current drawing canvas.
     *
     */
    fun saveAsBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        Canvas(bitmap).also {
            draw(it)
        }

        return bitmap
    }

    fun clearCallback() {
        drawViewPressCallback = null
        undoStateCallback = null
        redoStateCallback = null

        drawStateRef.clearCallback()
    }

    /**
     * Initial setting of the default brush [BrushType.NORMAL], with line type [LineType.SOLID]
     */
    private fun initDrawPaint() {
        drawPaintOption.paint.apply {
            color = Color.BLACK
            strokeWidth = DEFAULT_STROKE_WIDTH
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
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
     */
    private fun allocatePathEffect(
        pathEffect: PathEffect?,
        lineType: LineType,
        brushSize: Float
    ): PathEffect? {
        return when (lineType) {
            LineType.SOLID -> null
            LineType.DASH -> getDashEffectByBrushSize(pathEffect, brushSize)
            else -> null
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

    private fun modifyLineOptions(lineType: LineType) {
        if (lineType == LineType.ERASER) {
            if (!_isEraserOn) {
                _isEraserOn = true
            }
            return
        }

        _isEraserOn = false

        // Resets the corresponding brushType
        brushType = lineType.getBrushType()

        // Obtains corresponding pathEffect object
        pathEffect = allocatePathEffect(pathEffect, _lineType, _brushSize)

        updateAlphaOption(lineType)

        updateStrokeStyle(lineType)

        updateVelocityTrackerStates(lineType)
    }

    /**
     * This function is called to update flags that indicates whether certain drawing property
     * correlates to the velocity of [MotionEvent.ACTION_MOVE].
     *
     * Note that If the current [lineType] is not velocity-invariant, then we clear the reference of
     * the velocityTracker in case of the [VelocityTracker] object is not cleared when processing
     * the [MotionEvent.ACTION_UP].
     *
     */
    private fun updateVelocityTrackerStates(lineType: LineType) {
        needsWidthBias = lineType.isVelocityVariant()

        if (!needsWidthBias) {
            clearVelocityTrackerRef()
        }
    }

    /**
     * This function is called when we switch the [lineType] and when we set brush color (note that
     * setting color attribute will override alpha channel), to update the alpha value of the current
     * [drawPaintOption].
     */
    private fun updateAlphaOption(lineType: LineType) {
        drawPaintOption.paint.alpha = when (lineType) {
            LineType.CHISEL -> CHISEL_ALPHA
            else -> MAX_ALPHA
        }
    }

    /**
     * This function is called when we change the color of the drawing paint, to retrieve the alpha
     * value overridden by [Paint.setColor] (which will be set to [MAX_ALPHA]).
     */
    private fun updateBrushColor(lineType: LineType, color: Int) {
        drawPaintOption.paint.color = color

        when (lineType) {
            LineType.CHISEL -> {
                updateAlphaOption(_lineType)
            }
            else -> Unit
        }
    }

    private fun updatePathEffect(lineType: LineType = LineType.SOLID) {
        // Checks if we need to retrieve the pathEffect that should be modified because of the
        // size-invariant line type.
        if (lineType.isSizeVariant()) {
            pathEffect = allocatePathEffect(pathEffect, lineType, _brushSize)
        }
    }

    /**
     * This function is called to change the brush size according to whether the eraser is turned on.
     *
     * Note that the brush size of eraser is individually independent.
     */
    private fun changeStrokeWidthSrc(isEraserOn: Boolean) {
        drawPaintOption.paint.strokeWidth = if (isEraserOn) {
            Log.e(TAG, "changeStrokeWidthSrc: set Erasersize: $_eraserSize")
            _eraserSize
        } else {
            _brushSize
        }

    }

    private fun updateStrokeStyle(lineType: LineType) {
        when (lineType) {
            LineType.CHISEL -> {
                drawPaintOption.paint.apply {
                    strokeCap = Paint.Cap.SQUARE
                    strokeJoin = Paint.Join.BEVEL
                }
            }
            else -> {
                drawPaintOption.paint.apply {
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
            }
        }
    }

    /**
     * Note that the eraser mode is really just a NORMAL and SOLID brush with its XferMode set to
     * [PorterDuff.Mode.CLEAR], so that we can keep track of the drawing paths of the eraser without
     * introducing extra lists rather than the previous lists we defined in the [DrawViewState].
     */
    private fun toggleEraser(isEraserOn: Boolean) {
        changeStrokeWidthSrc(isEraserOn)

        if (isEraserOn) {
            brushType = BrushType.NORMAL
            if (_lineType != LineType.ERASER) {
                _lineType = LineType.ERASER
            }

            drawPaintOption.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            drawPaintOption.paint.xfermode = null
        }
    }

    override fun onDraw(canvas: Canvas) {
        setBackground(canvas)

        canvas.drawWithBlendLayer {
            drawPrevious(it)

            drawCurrent(it)
        }

        super.onDraw(canvas)
    }

    /**
     *
     * Note that this is really an expensive operation if we call this function in [onDraw], because
     * [onDraw] is called frequently to redraw the previous and the current paths.
     *
     * One possible optimizing solution is using the SurfaceView to operate the background drawing
     * process.
     *
     * Another possible optimizing solution might be using Double Buffering technique.
     */
    private fun setBackground(canvas: Canvas) {

        if (isCanvasBackgroundImg) {
            if (isCanvasBackgroundChanged) {
                canvasBackgroundImg?.let {
                    setBackgroundResource(it)
                }
                isCanvasBackgroundChanged = false
            }
        } else {
            if (isCanvasBackgroundChanged) {
                // Removes previous background
                setBackgroundResource(0)
                // draw the pure color background
                canvas.drawColor(canvasBackgroundColor)
            }
        }
    }

    private fun drawCurrent(canvas: Canvas) {
        canvas.drawPath(drawPath, drawPaintOption.paint)
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

        if (velocityTracker == null && needsWidthBias) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)

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

        invalidate()

        return true
    }

    /**
     * Resets the DrawPath object and records the starting coordinates of the following "trail".
     *
     * Note that this function is called when [MotionEvent.ACTION_DOWN] is intercepted
     */
    private fun performActionDown(touchX: Float, touchY: Float) {
        // Sets the beginning of the drawing contour to the touched points
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
        // Utilizes quadratic bezier fitting to smoothen the drawing path curve
        drawPath.quadTo(
            curX,
            curY,
            (curX + touchX) / 2,
            (curY + touchY) / 2
        )
        // Updates the coordinates of the last move
        curX = touchX
        curY = touchY

        // Computes the velocity of moving event based on time unit of 1000ms and calculates the
        // bias.
        velocityTracker?.let {
            it.computeCurrentVelocity(1000)
            drawPaintOption.strokeWidthBias = obtainBiasByVelocity(it.xVelocity, it.yVelocity)
        }
    }

    /**
     * Generates bias value of stroke width to simulate "Signing Pen" effect.
     *
     * Note that the minimum value of the bias is 0, which corresponds to zero velocity. If we do not
     * set any upper bound limit, the maximum value of the bias will be log10([Float.MAX_VALUE]),
     * which is equal to 38.5318394234 and literally not suitable for drawing as signing pen.
     *
     * Thus, we set the maximum bias value as [MAX_STROKE_WIDTH_BIAS].
     */
    private fun obtainBiasByVelocity(
        velocityX: Float,
        velocityY: Float,
        max: Float = MAX_STROKE_WIDTH_BIAS
    ): Float {
        log10(1F + abs(velocityX.coerceAtLeast(velocityY))).also {
            return if (it >= max) {
                max
            } else {
                it
            }
        }
    }

    /**
     * Adds the [drawPath] and the [drawPaintOption] to the stored lists respectively when the current
     * drawing process is ended.
     *
     * The lists will be used to retrieve the previous [drawPath] and the [drawPaintOption] when the
     * function [drawPrevious] is called.
     *
     * Note that this function is called when [MotionEvent.ACTION_UP] is intercepted.
     */
    private fun performActionUp() {
        drawStateRef.addToPrev(drawPath, drawPaintOption.paint)

        // Prepares for next draw. Also prevents the drawCurrent function from drawing the same
        // path again and again when the onDraw function is called.
        drawPath = DrawPath()

        drawPaintOption = PaintOption(Paint(drawPaintOption.paint))

        clearVelocityTrackerRef()
    }

    /**
     * Releases the VelocityTracker reference and returns the object back.
     */
    private fun clearVelocityTrackerRef() {
        velocityTracker?.let {
            it.recycle()
            velocityTracker = null
        }
    }


}