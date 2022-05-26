package com.redhoodhan.drawing.ui.draw.data

import com.redhoodhan.drawing.R

class DrawRepository {

    val colorList = mutableListOf<Int>()
    
    val backgroundList = mutableListOf<Int>()

    init {
        initColorList()
        
        initBackgroundList()
    }
    
    private fun initColorList() {
        colorList.apply {
            add(R.color.black)
            add(R.color.hotpink)
            add(R.color.coral)
            add(R.color.deeppink)
            add(R.color.salmon)
            add(R.color.palevioletred)
            add(R.color.papayawhip)
            add(R.color.gold)
            add(R.color.pink)
            add(R.color.lightyellow)
            add(R.color.peachpuff)
            add(R.color.red)
            add(R.color.orchid)
            add(R.color.mediumvioletred)
            add(R.color.darkkhaki)
            add(R.color.yellowgreen)
            add(R.color.darkseagreen)
            add(R.color.powderblue)
            add(R.color.paleturquoise)
            add(R.color.olivedrab)
            add(R.color.mediumaquamarine)
            add(R.color.cornflowerblue)
            add(R.color.cadetblue)
            add(R.color.steelblue)
            add(R.color.royalblue)
            add(R.color.darkslateblue)
            add(R.color.royalblue)
            add(R.color.seagreen)
            add(R.color.darkturquoise)
            add(R.color.mediumblue)
            add(R.color.white)
        }
    }
    
    private fun initBackgroundList() {
        backgroundList.apply { 
            add(R.drawable.img_draw_background_1)
            add(R.drawable.img_draw_background_2)
        }
    }

}