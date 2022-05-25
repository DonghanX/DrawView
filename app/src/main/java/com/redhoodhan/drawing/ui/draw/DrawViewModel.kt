package com.redhoodhan.drawing.ui.draw

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.redhoodhan.drawing.ui.draw.data.DrawRepository
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
    val drawColorClickLiveData: MutableLiveData<Int> = MutableLiveData()

    /**
     * LiveData that stores the color resource id corresponding to the selected item in the recycler
     * view in the [DrawBackgroundFragment].
     */
    val backgroundColorClickLiveData: MutableLiveData<Int> = MutableLiveData()

    /**
     * LiveData that stores the brush size in Float to the selected item in the recycler view in the
     * [DrawOptionFragment].
     */
    val drawBrushSizeClickLiveData: MutableLiveData<Float> = MutableLiveData()

    var lastClickedStateButtonId: Int
            by Delegates.observable(0) { _, oldValue, newValue ->
                _needsShowDrawOptionPanel =
                    ((oldValue == newValue))
            }

    val isStateButtonDoubleClicked: Boolean
        get() = _needsShowDrawOptionPanel

    val colorList: List<Int>?
        get() = mRepo?.colorList

    var defaultBrushSize: Float = 20F

    fun changeBrushSizeFromSeekBarProgress(progress: Int, maxProgress: Int = 50) {
        drawBrushSizeClickLiveData.postValue(progress.toFloat())
    }

    override fun onCleared() {
        super.onCleared()
        mRepo = null
    }

}