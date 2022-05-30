package com.redhoodhan.drawing.ui.draw

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.redhoodhan.draw.DrawView
import com.redhoodhan.draw.data.draw_option.LineType
import com.redhoodhan.drawing.R
import com.redhoodhan.drawing.databinding.ActivityMainBinding
import com.redhoodhan.drawing.ui.common.StateImageButton
import com.redhoodhan.drawing.util.AnimationUtil


private const val TAG = "MainActivity"

private const val POSITION_DRAW_OPTION_FRAGMENT = 0
private const val POSITION_DRAW_BACKGROUND_FRAGMENT = 1
private const val POSITION_DRAW_ERASER_FRAGMENT = 2

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
        binding.paintSolidButton.id.let {
            viewModel.curClickedStateButtonId = it
            viewModel.storedClickedStateButtonId = it
        }

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

            eraserButton.setOnStateClickListener(true) {
                switchFragmentInPager(POSITION_DRAW_ERASER_FRAGMENT)
                toggleEraser()
            }

            paintChiselTipButton.setOnStateClickListener(true) {
                switchFragmentInPager(POSITION_DRAW_OPTION_FRAGMENT)
                switchToLineType(LineType.CHISEL)
            }

            backgroundButton.setOnStateClickListener(
                needsPerformWithPanel = true,
                needsStoreButtonId = false
            ) {
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
                refreshDrawButtonStateByMode()
            }

            undoStateCallback = { isAvailable ->
                binding.undoButton.isClicked = isAvailable
            }

            redoStateCallback = { isAvailable ->
                binding.redoButton.isClicked = isAvailable
            }
        }
    }

    private fun initObservers() {
        viewModel.let {
            it.drawColorLiveData.observe(this) { colorId ->
                onDrawColorChanged(colorId)
            }

            it.drawBrushSizeLiveData.observe(this) { brushSize ->
                onBrushSizeChanged(brushSize)
            }

            it.drawEraserSizeLiveData.observe(this) { eraserSize ->
                onBrushSizeChanged(eraserSize, true)
            }

            it.backgroundColorLiveData.observe(this) { colorId ->
                onBackgroundColorChanged(colorId)
            }

            it.backgroundImgResLiveData.observe(this) { imgResId ->
                onBackgroundImgResChanged(imgResId)
            }

            it.clearCanvasLiveData.observe(this) { needsSaving ->
                onCanvasCleared(needsSaving)
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

    private fun onBrushSizeChanged(size: Float, isFromEraser: Boolean = false) {
        if (isFromEraser) {
            binding.drawView.eraserSize = size
        } else {
            binding.drawView.brushSize = size
        }
    }

    private fun onBackgroundColorChanged(colorResId: Int) {
        binding.drawView.canvasBackgroundColor =
            ResourcesCompat.getColor(resources, colorResId, null)
    }

    private fun onBackgroundImgResChanged(imgResId: Int) {
        binding.drawView.canvasBackgroundImg = imgResId
    }

    private fun onCanvasCleared(needsSaving: Boolean) {
        binding.drawView.clearCanvas(needsSaving)
    }

    private fun initDefaultDrawOptions() {
        viewModel.defaultBrushSize = binding.drawView.brushSize
    }

    /**
     * Wrapper of the [View.OnClickListener] to extract common operations when clicking the
     * [StateImageButton] on the draw-options layout and leaves a callback [clickCallback] for each
     * [StateImageButton] to perform its own click event.
     *
     */
    private fun StateImageButton.setOnStateClickListener(
        needsPerformWithPanel: Boolean,
        needsStoreButtonId: Boolean = true,
        clickCallback: (View) -> Unit
    ) {
        // Adds the current state button to a general list for a easier retrieving and iterating.
        drawButtonList.add(this)

        if (needsPerformWithPanel) {
            invokeFragmentButtonList.add(this)
        }

        setOnClickListener {
            viewModel.curClickedStateButtonId = it.id
            refreshAllButtonState(id)

            // If stored, the button ID could be retrieved for calling refreshDrawButtonStateByMode
            if (needsStoreButtonId) {
                viewModel.storedClickedStateButtonId = id
            }

            // TODO: Refactor the if-else logic
            if (needsPerformWithPanel && viewModel.isStateButtonDoubleClicked) {
                performWithDrawOptionPanel()
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

    /**
     * This function is called when user has clicked the buttons that has nothing to do with the
     * paint option and paths and only affects the canvas, i.e., the background button, and then
     * press the [DrawView] directly without clicking other button on the draw option panel.
     *
     * By calling this function, we retrieve the state button ID by accessing to the ???
     * we are using and then retrieve the button by ID. Then
     * set the button we retrieved as the clicked one.
     */
    private fun refreshDrawButtonStateByMode() {
        // TODO: abstraction is needed
        when (viewModel.curClickedStateButtonId) {
            binding.backgroundButton.id -> {
                refreshAllButtonState(viewModel.storedClickedStateButtonId)
            }
            else -> {}
        }
    }

    private fun performWithDrawOptionPanel() {
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
}