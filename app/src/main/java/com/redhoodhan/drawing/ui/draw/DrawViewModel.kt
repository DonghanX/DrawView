package com.redhoodhan.drawing.ui.draw

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.redhoodhan.draw.draw_option.data.DrawConst.Companion.DEFAULT_STROKE_WIDTH
import com.redhoodhan.drawing.ui.draw.DrawRepository
import kotlin.properties.Delegates

class DrawViewModel : ViewModel() {

    private var mRepo: DrawRepository? = null

    init {
        mRepo = DrawRepository()
    }

    private var _needsShowDrawOptionPanel: Boolean = false

    /**
     * LiveData that stores the color resource id corresponding to the selected item in the recycler
     * view in the [DrawOptionFragment].
     */
    val drawColorLiveData: MutableLiveData<Int> = MutableLiveData()

    /**
     * LiveData that stores the color resource id corresponding to the selected item in the recycler
     * view in the [DrawBackgroundFragment].
     */
    val backgroundColorLiveData: MutableLiveData<Int> = MutableLiveData()

    /**
     * LiveData that stores the drawable resource id corresponding to the selected item in the recycler
     * view in the [DrawBackgroundFragment].
     */
    val backgroundImgResLiveData: MutableLiveData<Int> = MutableLiveData()

    /**
     * LiveData that stores the brush size in Float corresponding to the seekbar in the
     * [DrawEraserFragment].
     */
    val drawBrushSizeLiveData: MutableLiveData<Float> = MutableLiveData()

    val drawEraserSizeLiveData: MutableLiveData<Float> = MutableLiveData()

    val clearCanvasLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var storedClickedStateButtonId: Int = 0

    var curClickedStateButtonId: Int
            by Delegates.observable(0) { _, oldValue, newValue ->
                _needsShowDrawOptionPanel =
                    ((oldValue == newValue))
            }

    val isStateButtonDoubleClicked: Boolean
        get() = _needsShowDrawOptionPanel

    val colorList: List<Int>?
        get() = mRepo?.colorList

    val backgroundList: List<Int>?
        get() = mRepo?.backgroundList

    var defaultBrushSize: Float = DEFAULT_STROKE_WIDTH

    fun notifyChangeBrushSize(
        progress: Int,
        maxProgress: Int = 50,
        isFromEraser: Boolean = false
    ) {
        if (isFromEraser) {
            drawEraserSizeLiveData.postValue(progress.toFloat())
        } else {
            drawBrushSizeLiveData.postValue(progress.toFloat())
        }
    }

    fun notifyClearCanvas(needsSaving: Boolean = true) {
        clearCanvasLiveData.postValue(needsSaving)
    }

    override fun onCleared() {
        super.onCleared()
        mRepo = null
    }

}