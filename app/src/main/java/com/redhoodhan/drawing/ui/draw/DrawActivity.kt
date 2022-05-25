package com.redhoodhan.drawing.ui.draw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.redhoodhan.drawing.R
import com.redhoodhan.drawing.databinding.ActivityMainBinding
import com.redhoodhan.drawing.ui.common.StateImageButton
import com.redhoodhan.drawing.ui.draw.data.draw_option.LineType
import com.redhoodhan.drawing.util.AnimationUtil

private const val TAG = "MainActivity"

private const val POSITION_DRAW_OPTION_FRAGMENT = 0
private const val POSITION_DRAW_BACKGROUND_FRAGMENT = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: DrawViewModel by viewModels()

    private lateinit var pagerAdapter: DrawPagerAdapter
    private lateinit var viewPager: ViewPager2

    private val drawButtonList = mutableListOf<StateImageButton>()

    /**
     * List that stores all the button that should perform with the drawing panel fragments, such as
     * [DrawBackgroundFragment] and [DrawOptionFragment]
     */
    private val invokeFragmentButtonList = mutableListOf<StateImageButton>()

    private val doubleClickExceptionButtonList = mutableListOf<StateImageButton>()

    private val drawOptionOriginalTranslationY: Float by lazy {
        resources.getDimension(R.dimen.draw_option_translation_y_init)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()

        initButtonState()

        initClickListener()

        initDrawView()

        initObservers()

        initDrawFragmentsInPager()
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initButtonState() {
        binding.paintSolidButton.isClicked = true

        // Initialize the id of the default clicked StateButton
        viewModel.lastClickedStateButtonId = binding.paintSolidButton.id

        doubleClickExceptionButtonList.add(binding.backgroundButton)
    }

    private fun initClickListener() {
        binding.apply {
            paintSolidButton.setOnStateClickListener(true) {
                switchFragmentInPager(POSITION_DRAW_OPTION_FRAGMENT)
                switchToLineType(LineType.SOLID)
            }

            paintDashedButton.setOnStateClickListener(true) {
                switchFragmentInPager(POSITION_DRAW_OPTION_FRAGMENT)
                switchToLineType(LineType.DASH)
            }

            eraserButton.setOnStateClickListener(false) {
                toggleEraser()
            }

            paletteButton.setOnStateClickListener(false) {
                // TODO
            }

            backgroundButton.setOnStateClickListener(true) {
                switchFragmentInPager(POSITION_DRAW_BACKGROUND_FRAGMENT)
            }

            undoButton.setOnClickListener {
                drawView.undo()
            }

            redoButton.setOnClickListener {
                drawView.redo()
            }

        }
    }

    private fun initDrawView() {
        initDrawCallback()

        initDefaultDrawOptions()
    }

    private fun initDrawCallback() {
        binding.drawView.apply {
            drawViewPressCallback = {
                hideDrawOptionPanel()
            }

            undoStateCallback = { isAvailable ->
                Log.e(TAG, "undoStateCallback: $isAvailable")
                binding.undoButton.isClicked = isAvailable
            }

            redoStateCallback = { isAvailable ->
                Log.e(TAG, "redoStateCallback: $isAvailable")
                binding.redoButton.isClicked = isAvailable
            }
        }
    }

    private fun initObservers() {
        viewModel.let {
            it.drawColorClickLiveData.observe(this) { colorId ->
                onDrawColorChanged(colorId)
            }

            it.drawBrushSizeClickLiveData.observe(this) { brushSize ->
                onBrushSizeChanged(brushSize)
            }

            it.backgroundColorClickLiveData.observe(this) { colorId ->
                onBackgroundColorChanged(colorId)
            }
        }
    }

    private fun initDrawFragmentsInPager() {
        pagerAdapter = DrawPagerAdapter(this)

        viewPager = binding.drawPager.apply {
            isUserInputEnabled = false
            adapter = pagerAdapter
        }
    }

    private fun switchFragmentInPager(position: Int) {
        viewPager.setCurrentItem(position, false)
    }

    private fun onDrawColorChanged(colorResId: Int) {
        binding.drawView.brushColor = ResourcesCompat.getColor(resources, colorResId, null)
    }

    private fun onBrushSizeChanged(brushSize: Float) {
        binding.drawView.brushSize = brushSize
    }

    private fun onBackgroundColorChanged(colorResId: Int) {
        binding.drawView.canvasBackgroundColor =
            ResourcesCompat.getColor(resources, colorResId, null)
    }

    private fun initDefaultDrawOptions() {
        viewModel.defaultBrushSize = binding.drawView.brushSize
    }

    /**
     * Wrapper of the [View.OnClickListener] to extract common operations when clicking the
     * [StateImageButton] on the draw-options layout and leaves a callback [clickCallback] for each
     * [StateImageButton] to perform its own click event.
     */
    private fun StateImageButton.setOnStateClickListener(
        needsPerformWithPanel: Boolean,
        clickCallback: (View) -> Unit
    ) {
        // Adds the current state button to a general list for a easier retrieving and iterating.
        drawButtonList.add(this)

        if (needsPerformWithPanel) {
            invokeFragmentButtonList.add(this)
        }

        setOnClickListener {
            viewModel.lastClickedStateButtonId = it.id
            refreshAllButtonState(id)

            // TODO: Refactor the if-else logic
            if (needsPerformWithPanel && viewModel.isStateButtonDoubleClicked) {
                performWithDrawOptionPanel(it)
            } else if (!invokeFragmentButtonList.contains(this)) {
                // If the new clicked state button should also perform with the draw option panel,
                // then we do not hide the fragment container. We just switch the original fragment
                // to the new fragment corresponding to what state button we clicked.
                hideDrawOptionPanel()
            } else {
                // Case for the StateButton in the exception list
                showDrawOptionPanel()
            }

            // Callback that performs the actual click event logic
            clickCallback.invoke(this)
        }
    }

    private fun performWithDrawOptionPanel(view: View) {
        val curTranslationY = binding.drawOptionLayout.translationY

        AnimationUtil.repTranslateY(
            binding.drawOptionLayout,
            curTranslationY,
            drawOptionOriginalTranslationY
        )
    }

    private fun hideDrawOptionPanel() {
        binding.drawOptionLayout.apply {
            if (translationY == drawOptionOriginalTranslationY) {
                return
            }
            animate().translationY(drawOptionOriginalTranslationY)
        }
    }

    private fun showDrawOptionPanel() {
        binding.drawOptionLayout.apply {
            if (translationY == 0F) {
                return
            }
            animate().translationY(0F)
        }
    }

    /**
     * This function is called when one of the [StateImageButton] on the button panel is clicked, in
     * order to refresh the buttons' clicked states.
     *
     * Note that we can simply repeatedly assigning value to [StateImageButton.isClicked] without
     * worrying about calling ColorFilter redundantly, because [StateImageButton.isClicked] will
     * call ColorFilter related function only if the state is changed.
     */
    private fun refreshAllButtonState(clickedViewId: Int) {
        drawButtonList.forEach {
            it.isClicked = (it.id == clickedViewId)
        }
    }

    private fun switchToLineType(lineType: LineType = LineType.SOLID) {
        binding.drawView.lineType = lineType
    }

    private fun toggleEraser() {
        binding.drawView.isEraserOn = true
    }

    override fun onDestroy() {
        binding.drawView.clearCallback()

        super.onDestroy()
    }
}