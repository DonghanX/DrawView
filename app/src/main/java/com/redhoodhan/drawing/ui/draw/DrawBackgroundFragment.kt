package com.redhoodhan.drawing.ui.draw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.redhoodhan.drawing.databinding.FragmentDrawBackgroundBinding
import com.redhoodhan.drawing.ui.draw.adapter.DrawColorResAdapter
import com.redhoodhan.drawing.ui.draw.adapter.DrawImgResAdapter

private const val TAG = "DrawOptionFragment"

class DrawBackgroundFragment : Fragment() {

    private var _binding: FragmentDrawBackgroundBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel: DrawViewModel by activityViewModels()

    private var colorAdapter: DrawColorResAdapter? = null

    private var imgResAdapter: DrawImgResAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDrawBackgroundBinding.inflate(inflater, container, false)

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initColorRecyclerView()
        initImgResRecyclerView()
    }

    companion object {
        @JvmStatic
        fun newInstance() = DrawBackgroundFragment()
    }

    private fun initImgResRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewModel.backgroundList?.let {
            imgResAdapter = DrawImgResAdapter(it).also { adapter ->
                adapter.imgSelectCallback = { imgResId ->
                    onImgItemSelected(imgResId)
                }
            }
        }

        binding.drawBgRecyclerView.apply {
            adapter = imgResAdapter
            layoutManager = linearLayoutManager
        }
    }

    private fun initColorRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewModel.colorList?.let {
            colorAdapter = DrawColorResAdapter(it).also { adapter ->
                adapter.colorSelectCallback = { colorResId ->
                    onColorItemSelected(colorResId)
                }
            }
        }

        binding.drawColorRecyclerView.apply {
            adapter = colorAdapter
            layoutManager = linearLayoutManager
        }
    }

    private fun onColorItemSelected(colorResId: Int) {
        viewModel.backgroundColorLiveData.postValue(colorResId)
    }

    private fun onImgItemSelected(imgResId: Int) {
        viewModel.backgroundImgResLiveData.postValue(imgResId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}