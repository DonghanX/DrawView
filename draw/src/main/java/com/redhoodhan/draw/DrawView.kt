package com.redhoodhan.draw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.redhoodhan.draw.draw_option.DrawOptionContext
import com.redhoodhan.draw.draw_option.data.*
import com.redhoodhan.draw.extension.drawWithBlendLayer
import kotlin.math.abs
import kotlin.math.log10


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

    private var drawOptionContext: DrawOptionContext

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
            if (field == value) {
                return
            }
            field = value
            modifyLineOptions(value)
        }

    private var brushType: BrushType = BrushType.NORMAL

    private var _brushSize = DrawConst.DEFAULT_STROKE_WIDTH
        set(value) {
            field = if (value <= DrawConst.DEFAULT_MIN_STROKE_WIDTH) {
                DrawConst.DEFAULT_MIN_STROKE_WIDTH
            } else {
                value
            }
            changeStrokeWidthSrc(false)
            updatePathEffect()
        }

    private var _eraserSize = DrawConst.DEFAULT_STROKE_WIDTH
        set(value) {
            field = if (value <= DrawConst.DEFAULT_MIN_STROKE_WIDTH) {
                DrawConst.DEFAULT_MIN_STROKE_WIDTH
            } else {
                value
            }
            changeStrokeWidthSrc(true)
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

        drawOptionContext = DrawOptionContext()
        _drawState = DrawViewState()
    }

    val drawStateRef
        get() = _drawState!!

    var brushColor = DrawConst.DEFAULT_PAINT_COLOR
        set(value) {
            field = value
            updateBrushColor(value)
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

    /**
     * Color resource of the drawing canvas background
     */
    var canvasBackgroundColor = DrawConst.DEFAULT_CANVAS_COLOR
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
//    fun switchBrushType(brushType: BrushType) {
//        _isEraserOn = false
//        // TODO: Complete BrushType switching logic
//        //      Note that this should be done after the actual drawing function is completed
//    }

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
            strokeWidth = DrawConst.DEFAULT_STROKE_WIDTH
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

    private fun modifyLineOptions(lineType: LineType) {
        val isEraserOn = lineType == LineType.ERASER
        val curSize = changeStrokeWidthSrc(isEraserOn)

        // Resets the corresponding brushType
        brushType = lineType.getBrushType()

        drawOptionContext.let {
            it.changeStrategyByLineType(lineType)
            it.updateOptionWhenSwitchingLineType(drawPaintOption, curSize)
        }

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

    private fun updateBrushColor(color: Int) {
        drawOptionContext.updateBrushColor(drawPaintOption, color)
    }

    private fun updatePathEffect() {
        drawOptionContext.updatePathEffect(drawPaintOption, brushSize)
    }

    /**
     * This function is called to change the brush size source according to whether the eraser is
     * turned on.
     * Note that the brush size of eraser is individually independent.
     */
    private fun changeStrokeWidthSrc(isEraserOn: Boolean): Float {
        val curSize = if (isEraserOn) {
            eraserSize
        } else {
            brushSize
        }

        drawOptionContext.updateBrushSize(drawPaintOption, curSize)

        return curSize
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

//        // Double Buffer
//        if (bufferBitmap == null) {
//            bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
//                bufferCanvas = Canvas(it)
//            }
//        }

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
     * Thus, we set the maximum bias value as [DrawConst.MAX_STROKE_WIDTH_BIAS].
     */
    private fun obtainBiasByVelocity(
        velocityX: Float,
        velocityY: Float,
        max: Float = DrawConst.MAX_STROKE_WIDTH_BIAS
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