package com.redhoodhan.draw.data.draw_option

import android.view.MotionEvent

enum class LineType {
    SOLID {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = false

        override fun isVelocityVariant() = false
    },
    DASH {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = true

        override fun isVelocityVariant() = false
    },
    SIGNING {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = false

        override fun isVelocityVariant() = true
    },
    CHISEL {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = false

        override fun isVelocityVariant() = false
    },
    ERASER {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = false

        override fun isVelocityVariant() = false
    };

    abstract fun getBrushType(): BrushType

    /**
     * Determines whether the corresponding effect should be retrieved when we change the brushSize.
     */
    abstract fun isSizeVariant(): Boolean

    /**
     * Determines whether we need to keep track of the velocity of [MotionEvent.ACTION_MOVE].
     */
    abstract fun isVelocityVariant(): Boolean

}