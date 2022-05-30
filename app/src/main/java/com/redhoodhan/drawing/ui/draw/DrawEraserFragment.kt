package com.redhoodhan.drawing.ui.draw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import com.redhoodhan.drawing.databinding.FragmentDrawEraserBinding

class DrawEraserFragment : Fragment() {

    private var _binding: FragmentDrawEraserBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel: DrawViewModel by activityViewModels()

    companion object {
        @JvmStatic
        fun newInstance() = DrawEraserFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDrawEraserBinding.inflate(inflater, container, false)

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListener()

        initSeekbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initClickListener() {
        binding.clearButton.setOnClickListener {
            viewModel.notifyClearCanvas(true)
        }
    }

    private fun initSeekbar() {
        // Initialize default brush size that the SeekBar indicates
        binding.eraseSizeSeekbar.progress = viewModel.defaultBrushSize.toInt()

        binding.eraseSizeSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.notifyChangeBrushSize(progress, isFromEraser = true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }
}