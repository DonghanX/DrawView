package com.redhoodhan.drawing.util

import android.util.Log
import android.view.View

class AnimationUtil {

    companion object {

        /**
         * This function is called when we need to translate a view along the Y-axis and specially
         * the view is expected to translate between two Y-location repeatedly.
         *
         * [curTranslationY] is the current translationY value of the view.
         * [originalTranslationY] is the initial value of the translationY of the view.
         */
        fun repTranslateY(
            view: View,
            curTranslationY: Float,
            originalTranslationY: Float
        ) {
            val newTranslationY = if (curTranslationY == originalTranslationY) {
                0f
            } else {
                originalTranslationY
            }

            view.animate().translationY(newTranslationY)
        }

        fun scaleXY(view: View, curScaleValue: Float) {
            view.animate().scaleX(curScaleValue).scaleY(curScaleValue)
        }
    }

}