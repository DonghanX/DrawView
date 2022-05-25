package com.redhoodhan.drawing.util

import android.content.res.Resources

fun Float.dp2px(): Float = this * Resources.getSystem().displayMetrics.density

fun Float.px2dp(): Float = this / Resources.getSystem().displayMetrics.density
