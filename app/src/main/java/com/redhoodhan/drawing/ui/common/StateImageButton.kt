package com.redhoodhan.drawing.ui.common

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.redhoodhan.drawing.R

private const val TAG = "StateImageButton"

class StateImageButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int = 0,
) : AppCompatImageButton(context, attributeSet, defStyle) {

    private var originalColorResId: Int = R.color.black
    private var clickedColorResId: Int = R.color.purple_500

    private val originalColor: Int by lazy {
        retrieveColorFromRes(originalColorResId)
    }
    private val clickedColor: Int by lazy {
        retrieveColorFromRes(clickedColorResId)
    }

    var isClicked: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            switchImageState(value)
        }

    init {
        scaleType = ScaleType.CENTER

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.StateImageButton, 0, 0)
            .apply {
                try {
                    originalColorResId =
                        getResourceId(R.styleable.StateImageButton_originalColor, R.color.black)
                    clickedColorResId =
                        getResourceId(
                            R.styleable.StateImageButton_selectedColor,
                            R.color.purple_500
                        )
                } finally {
                    recycle()
                }
            }

        switchImageState(false)
    }

    private fun retrieveColorFromRes(colorResId: Int): Int =
        ResourcesCompat.getColor(resources, colorResId, null)

    private fun switchImageState(isClicked: Boolean) {
        drawable.colorFilter = if (isClicked) {
            blendWithColorFilter(clickedColor)
        } else {
            blendWithColorFilter(originalColor)
        }
    }

    private fun blendWithColorFilter(
        color: Int,
        blendMode: BlendModeCompat = BlendModeCompat.SRC_ATOP
    ) =
        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            color,
            blendMode
        )
}