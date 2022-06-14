package com.redhoodhan.drawing.ui.draw.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.redhoodhan.drawing.databinding.ItemBgImgResBinding

class DrawImgResAdapter(private val imgList: List<Int>) :
    RecyclerView.Adapter<DrawImgResAdapter.ViewHolder>() {

    private var _curSelectItemPosition: Int = 0

    val curSelectItemPosition: Int
        get() = _curSelectItemPosition

    var imgSelectCallback: ((colorResId: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        ItemBgImgResBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .also {
                return ViewHolder(it)
            }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgResId = imgList[position]

        holder.bindBackgroundRes(imgResId)
        // Refresh the select state of the current item
        if (position == _curSelectItemPosition) {
            holder.setCurSelected(true)
        } else {
            holder.setCurSelected(false)
        }
    }

    override fun getItemCount(): Int = imgList.size

    inner class ViewHolder(private val binding: ItemBgImgResBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { performClickEvent() }
        }

        private fun performClickEvent() {
            val tempPosition = _curSelectItemPosition
            imgSelectCallback?.invoke(imgList[adapterPosition])
            _curSelectItemPosition = adapterPosition
            setCurSelected(true)

            // Refresh select states of the recycler view
            notifyItemChanged(_curSelectItemPosition)
            notifyItemChanged(tempPosition)
        }

        fun bindBackgroundRes(imgRes: Int) {
            binding.bgImage.apply {
                setImageDrawable(ResourcesCompat.getDrawable(resources, imgRes, null))
            }
        }

        fun setCurSelected(isSelected: Boolean) {
        }

    }
}