package com.redhoodhan.drawing.ui.draw.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.redhoodhan.drawing.databinding.ItemDrawResBinding

class DrawColorResAdapter(private val colorList: List<Int>) :
    RecyclerView.Adapter<DrawColorResAdapter.ViewHolder>() {

    private var _curSelectItemPosition: Int = 0

    val curSelectItemPosition: Int
        get() = _curSelectItemPosition

    var colorSelectCallback: ((colorResId: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        ItemDrawResBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .also {
                return ViewHolder(it)
            }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorResId = colorList[position]

        holder.bindBackgroundColor(colorResId)
        // Refresh the select state of the current item
        if (position == _curSelectItemPosition) {
            holder.setCurSelected(true)
        } else {
            holder.setCurSelected(false)
        }
    }

    override fun getItemCount(): Int = colorList.size

    inner class ViewHolder(private val binding: ItemDrawResBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { performClickEvent() }
        }

        private fun performClickEvent() {
            val tempPosition = _curSelectItemPosition
            colorSelectCallback?.invoke(colorList[adapterPosition])
            _curSelectItemPosition = adapterPosition
            setCurSelected(true)

            // Refresh select states of the recycler view
            notifyItemChanged(_curSelectItemPosition)
            notifyItemChanged(tempPosition)
        }

        fun bindBackgroundColor(colorRes: Int) {
            binding.drawColorItem.drawColorResId = colorRes
        }

        fun setCurSelected(isSelected: Boolean) {
            binding.drawColorItem.isCurSelected = isSelected
        }

    }
}

