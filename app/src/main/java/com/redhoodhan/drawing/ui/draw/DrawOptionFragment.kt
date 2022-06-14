package com.redhoodhan.drawing.ui.draw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.redhoodhan.drawing.databinding.FragmentDrawOptionBinding
import com.redhoodhan.drawing.ui.draw.adapter.DrawColorResAdapter

private const val TAG = "DrawOptionFragment"

class DrawOptionFragment : Fragment() {

    private var _binding: FragmentDrawOptionBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel: DrawViewModel by activityViewModels()

    private var colorAdapter: DrawColorResAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDrawOptionBinding.inflate(inflater, container, false)

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSeekBarListener()

        initRecyclerView()
    }

    companion object {
        @JvmStatic
        fun newInstance() = DrawOptionFragment()
    }

    private fun initRecyclerView() {
        viewModel.colorList?.let {
            colorAdapter = DrawColorResAdapter(it).also { adapter ->
                adapter.colorSelectCallback = { colorResId ->
                    onColorItemSelected(colorResId)
                }
            }
        }

        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.drawColorRecyclerView.apply {
            adapter = colorAdapter
            layoutManager = linearLayoutManager
        }

        // Initialize default brush color
        colorAdapter?.curSelectItemPosition?.let { pos ->
            viewModel.colorList?.let { list ->
                onColorItemSelected(list[pos])
            }
        }
    }

    private fun onColorItemSelected(colorResId: Int) {
        viewModel.drawColorLiveData.postValue(colorResId)

        changeSeekBarThumbColor(colorResId)
    }

    private fun changeSeekBarThumbColor(
        colorResId: Int,
        blendMode: BlendModeCompat = BlendModeCompat.SRC_ATOP
    ) {
        ResourcesCompat.getColor(resources, colorResId, null).let { color ->
            binding.brushSizeSeekbar.apply {
                val colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, blendMode)
                thumb.colorFilter = colorFilter
                progressDrawable.colorFilter = colorFilter
            }
        }
    }

    private fun initSeekBarListener() {
        // Initialize default brush size that the SeekBar indicates
        binding.brushSizeSeekbar.progress = viewModel.defaultBrushSize.toInt()

        binding.brushSizeSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                seekbar?.let {
                    viewModel.notifyChangeBrushSize(it.progress)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}