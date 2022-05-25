package com.redhoodhan.drawing.ui.draw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.redhoodhan.drawing.databinding.FragmentDrawBackgroundBinding

private const val TAG = "DrawOptionFragment"

class DrawBackgroundFragment : Fragment() {

    private var _binding: FragmentDrawBackgroundBinding? = null

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
        _binding = FragmentDrawBackgroundBinding.inflate(inflater, container, false)

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
    }

    companion object {
        @JvmStatic
        fun newInstance() = DrawBackgroundFragment()
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
    }

    private fun onColorItemSelected(colorResId: Int) {
        viewModel.backgroundColorClickLiveData.postValue(colorResId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}