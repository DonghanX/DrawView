package com.redhoodhan.drawing.ui.draw.data.draw_option

enum class LineType {
    SOLID {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = false
    },
    DASH {
        override fun getBrushType() = BrushType.NORMAL

        override fun isSizeVariant() = true
    };

    abstract fun getBrushType(): BrushType

    /**
     * Determines whether the corresponding effect should be retrieved when we change the brushSize.
     */
    abstract fun isSizeVariant(): Boolean

}