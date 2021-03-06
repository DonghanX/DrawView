package com.redhoodhan.drawing.ui.draw.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.redhoodhan.drawing.ui.draw.DrawBackgroundFragment
import com.redhoodhan.drawing.ui.draw.DrawEraserFragment
import com.redhoodhan.drawing.ui.draw.DrawOptionFragment

private const val TOTAL_FRAGMENT_NUM = 3
private const val TAG = "DrawPagerAdapter"

class DrawPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return TOTAL_FRAGMENT_NUM
    }

    /**
     * Create certain type of fragment according to the item position in the viewPager.
     *
     * Note that the [position] equaling to 0 corresponds to [DrawOptionFragment], and the [position]
     * equaling to 1 corresponds to [DrawBackgroundFragment]
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DrawOptionFragment.newInstance()
            1 -> DrawBackgroundFragment.newInstance()
            2 -> DrawEraserFragment.newInstance()
            else -> DrawOptionFragment.newInstance()
        }
    }

}