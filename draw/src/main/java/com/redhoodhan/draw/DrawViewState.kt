package com.redhoodhan.draw

import android.graphics.Paint
import android.graphics.Path


class DrawViewState {

    private val prevDrawPath = mutableListOf<Path>()
    private val prevDrawPaint = mutableListOf<Paint>()

    private val undoneDrawPath = mutableListOf<Path>()
    private val undoneDrawPaint = mutableListOf<Paint>()

    /**
     * Returns the size of list that contains all the previous [Path]. This is used to check if
     * we can perform any undo action.
     */
    val prevSize: Int
        get() = prevDrawPath.size

    val isPathEmpty: Boolean
        get() = (prevSize == 0)

    /**
     * Returns the size of [undoneDrawPath]. This is used to check if we can perform any redo
     * action.
     */
    val undoneSize: Int
        get() = undoneDrawPath.size

    val isUndoAvailable
        get() = (prevSize > 0)

    val isRedoAvailable
        get() = (undoneSize > 0)

    /**
     * This callback is triggered in three cases in which we may want to notify other components:
     *
     * 1. New paths and paints are added to the corresponding "prevList", which means the user
     * drew something on the [DrawView].
     *
     * 2. User performs undo action.
     *
     * 3. User performs redo action.
     */
    var stateActionCallback: (() -> Unit)? = null

    fun addToPrev(path: Path, paint: Paint) {
        prevDrawPath.add(path)
        prevDrawPaint.add(paint)

        // Clear undo list to make redo unavailable
        clearUndoList()

        stateActionCallback?.invoke()
    }

    fun getPrevPathWithIndexed(index: Int): Path =
        prevDrawPath[index]

    fun getPrevPaintWithIndexed(index: Int): Paint =
        prevDrawPaint[index]

    /**
     * Performs undo actions by removing the last [Path] and [Paint] objects in the [prevDrawPath]
     * list and the [prevDrawPaint] list and adding them to the corresponding undone lists. Thus,
     * when calling the [DrawView.invalidate] function, the canvas should draw all the previous paths
     * except for the certain path we recalled before.
     */
    fun undo() {
        prevDrawPath.removeLastOrNull()?.let {
            undoneDrawPath.add(it)
        }
        prevDrawPaint.removeLastOrNull()?.let {
            undoneDrawPaint.add(it)
        }

        stateActionCallback?.invoke()
    }

    fun redo() {
        undoneDrawPath.removeLastOrNull()?.let {
            prevDrawPath.add(it)
        }
        undoneDrawPaint.removeLastOrNull()?.let {
            prevDrawPaint.add(it)
        }

        stateActionCallback?.invoke()
    }

    /**
     * TODO: the paths cleared should have ability of being retrieved at once simultaneously.
     * Clears canvas and stores all the [prevDrawPath] and [prevDrawPaint] to the corresponding
     * undo list.
     */
    fun clearCanvasWithSaving() {
        with(undoneDrawPath) {
            clear()
            addAll(prevDrawPath)
            prevDrawPath.clear()
        }

        with(undoneDrawPaint) {
            clear()
            addAll(prevDrawPaint)
            prevDrawPaint.clear()
        }

        stateActionCallback?.invoke()
    }

    fun clearCanvas() {
        prevDrawPath.clear()
        prevDrawPaint.clear()
        undoneDrawPath.clear()
        undoneDrawPaint.clear()

        stateActionCallback?.invoke()
    }

    fun clearCallback() {
        stateActionCallback = null
    }

    private fun clearUndoList() {
        undoneDrawPath.clear()
        undoneDrawPaint.clear()
    }

}