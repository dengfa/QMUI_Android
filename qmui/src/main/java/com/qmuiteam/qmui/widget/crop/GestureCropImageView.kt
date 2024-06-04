/*
package com.qmuiteam.qmui.widget.crop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType.FIT_XY
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.xxx.edu.tutor.imageviewer.core.photoView.OnMatrixChangedListener
import com.xxx.edu.tutor.imageviewer.core.photoView.PhotoView2
import com.xxx.edu.tutor.settings.SolutionReselectConfig
import com.xxx.edu.tutor.settings.TutorBusinessSettings
import com.xxx.edu.tutor.solution.R
import com.xxx.edu.tutor.solution.entity.MentalRectEntity
import com.xxx.edu.tutor.solution.entity.QuestionPieceWrapper
import com.xxx.edu.tutor.solution.entity.SubItemRectEntity
import com.xxx.edu.tutor.solution.toSubItemRectEntity
import com.xxx.edu.tutor.solution.widget.ArrowLocation.Bottom
import com.xxx.edu.tutor.solution.widget.ArrowLocation.Top
import com.xxx.edu.tutor.solution.widget.CropBizScene.Page_Correct
import com.xxx.edu.tutor.solution.widget.CropBizScene.Page_Search
import com.xxx.edu.tutor.solution.widget.SolutionCropOverlayView.RectMoveListener
import com.xxx.edu.tutor.solution.widget.SolutionCropOverlayView.WindowSizeProvider
import com.xxx.edu.tutor.tools.UiUtil
import com.xxx.edu.tutor.tools.dp
import com.xxx.edu.tutor.tools.gone
import com.xxx.edu.tutor.tools.visible
import com.xxx.news.common.settings.SettingsManager
import com.edu.tutor.guix.toast.TutorToast
import com.miracle.photo.crop.CropWindowHandler
import com.miracle.photo.crop.MultiCropImageView
import com.ss.android.agilelogger.ALog
import hippo.api.turing.question_search.detection.kotlin.CorrectContent
import hippo.api.turing.question_search.detection.kotlin.QuestionCorrectStatus
import hippo.api.turing.question_search.detection.kotlin.QuestionCorrectStatus.HalfRight
import hippo.api.turing.question_search.detection.kotlin.QuestionCorrectStatus.NoAnswer
import hippo.api.turing.question_search.detection.kotlin.QuestionCorrectStatus.Right
import hippo.api.turing.question_search.detection.kotlin.QuestionCorrectStatus.Wrong
import hippo.api.turing.question_search.question.kotlin.QuestionSearchType
import hippo.api.turing.question_search.question.kotlin.QuestionSearchType.AIProblemSolving
import hippo.api.turing.question_search.question.kotlin.QuestionSearchType.MentalCalculation

*/
/**
 *
 *//*

class GestureCropImageView(context: Context, attrs: AttributeSet? = null) :
    MultiCropImageView(context, attrs) {

    companion object {

        internal const val TAG = "GestureCropImageView"

        // 图片移动、缩放动画时长
        private const val ANIMATION_DURATION = 300

        // 图片最小缩放
        private const val MIN_SCALE = 1F

        // 图片最大缩放
        private const val MAX_SCALE = 2f

        const val MARGIN = 1
        const val NUMBER_WIDTH = 25
        const val SUB_NUMBER_WIDTH = 33
        // 引导动画
        private const val GUIDE_LEFT_MARGIN = 124
        private const val GUIDE_TOP_MARGIN = 70
        // 修改后框生效时间
        private const val MODIFY_CONFIRM_DELAY_TIME = 500L
    }

    override val forbiddenMatrixScaleType: Boolean = true

    private val customCropOverlayView get() = super.cropOverlayView as? SolutionCropOverlayView
    private val childrenContainer: FrameLayout
    private val photoView: PhotoView2

    private val correctNumberTagSize = 24.dp
    private val MARGIN = 1.dp
    private val cropWindowHandlerMap = mutableMapOf<Int, CropWindowHandlerWrapper>()
    private val numberMap = mutableMapOf<Int, QuestionNumberView>()
    private val resultTagMap = mutableMapOf<Int, ImageView>()
    private val oralResultTagMap = mutableMapOf<Int, MutableList<ImageView?>>()
    private val subNumberMap = mutableMapOf<Int, MutableList<QuestionNumberView>>()
    private val subItemResultTagMap = mutableMapOf<Int, MutableList<ImageView?>>() //v1.5.0 非口算题子空
    private var lastVisibleNumberView: View? = null
    private var subRectClickListener: ((index: Int?, subIndex: Int?) -> Unit)? = null
    private var bizSceneType = CropBizScene.Page_Search

    private var windowBottomPadding = 0
    private var resetScale = false
    private var targetIndex: Int? = null
    private var currentSelectIndex: Int? = null
    private val rectSafeMarginTop = 30.dp

    private var showGuideAfterInit = false
    private var hasGuideViewShowed = false
    private var guideView: LottieAnimationView? = null
    private var guideShowCallback: (() -> Unit)? = null

    private var requestQuestionCallback: ((pieceId:Long?,rectF:RectF?) -> Unit)? = null
    private var modifyRequesting = false

    private var modifyLimitCallback: (() -> Unit)? = null

    // 是否固定 PhotoView 的初始宽高
    // 开启后 PhotoView 的初始状态(baseMatrix)将使用传出的 stable size，不会随控件尺寸变化而变化
    var useStableSize: Boolean = false
        set(value) {
            if (value != field) {
                updateStableSize(value)
            }
            field = value
        }

    private val updateRunnable = Runnable {
        changePhotoRect()
        this.resetScale = false
    }

    private var moveCompleteRunnable: Runnable? = null

    private val moveControlQueue by lazy { ArrayDeque<Long>() }
    private var controlConfig: SolutionReselectConfig? = null

    @SuppressLint("ClickableViewAccessibility")
    private val photoTouchListener = OnTouchListener { v, event ->
        Log.d(TAG, "onTouch: ${event?.actionMasked}")
        event != null && customCropOverlayView?.handleOnTouch(event) == true
    }

    fun setRequestCallback(callback: ((pieceId:Long?,rectF:RectF?) -> Unit)? = null){
        requestQuestionCallback = callback
    }

    init {
        cropOverlayView.isEnabled = false
        photoView = findViewById(R.id.multi_image)
        childrenContainer = findViewById(R.id.children_container)
        photoView.apply {
            setOnMatrixChangeListener(object : OnMatrixChangedListener {
                override fun onMatrixChanged(rect: RectF) {
                }

                override fun onMatrixChanged(rect: RectF, matrix: Matrix) {
                    Log.d(TAG, "onMatrixChanged: $rect, ${matrix.toShortString()}")
                    customCropOverlayView?.onMatrixChanged(rect, matrix)
                    layoutChildren()
                }

            })
            setOnViewTapListener { _, x, y ->
                onViewClick(x, y)
            }
            minimumScale = MIN_SCALE
            maximumScale = MAX_SCALE
            attacher.apply {
                setDoubleTapEnable(false)
                setZoomTransitionDuration(ANIMATION_DURATION)
                setCustomTouchListener(photoTouchListener)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        customCropOverlayView?.rectMoveListener = object : RectMoveListener {

            override fun onMoveStart(cropHandler: CropWindowHandlerWrapper): Boolean {
                return if (canModify()) {
                    childrenContainer.visibility = View.INVISIBLE
                    moveCompleteRunnable?.let {
                        removeCallbacks(it)
                    }
                    hideGuideView()
                    false
                } else {
                    true
                }
            }

            override fun onMoveComplete(cropHandler: CropWindowHandlerWrapper) {
                layoutChildren()
                moveCompleteRunnable?.let {
                    removeCallbacks(it)
                }
                moveCompleteRunnable = Runnable {
                    val index = cropWindowHandlerMap.entries.indexOfFirst { it.value == cropHandler }
                    subNumberMap[index]?.forEach {
                        childrenContainer.removeView(it)
                    }
                    subNumberMap[index] = mutableListOf()
                    cropHandler.oralCropWindowHandlers = null
                    cropHandler.type = null
                    customCropOverlayView?.exitMoveMode()
                    recordMoveTimestamp()
                    childrenContainer.visibility = View.VISIBLE
                    requestQuestionCallback?.invoke(cropHandler.pieceId,cropHandler.cropHandler?.initRect)
                }
                postDelayed(moveCompleteRunnable, MODIFY_CONFIRM_DELAY_TIME)
            }

            override fun onActionUpNotHandle(x: Float, y: Float) {
                onViewClick(x, y)
            }
        }
        customCropOverlayView?.windowSizeProvider = object : WindowSizeProvider {
            override fun getWindowWidth(): Int {
                return photoView.attacher.windowWidth
            }

            override fun getWindowHeight(): Int {
                return photoView.attacher.windowHeight
            }

        }
    }

    */
/**
     * 是否可以重新框选
     *//*

    private fun canModify(): Boolean {
        // 频控判断
        val config = controlConfig ?: run {
            SettingsManager.obtain(TutorBusinessSettings::class.java).getSolutionReselectConfig().also { controlConfig = it }
        }
        val now = System.currentTimeMillis()
        if (moveControlQueue.size < config.times) {
            return true
        }
        val oldest = moveControlQueue[0]
        if (now - oldest > config.durationSec * DateUtils.SECOND_IN_MILLIS) {
            moveControlQueue.removeFirst()
            return true
        }
        TutorToast.showToast("操作频繁，稍后再试试吧")
        modifyLimitCallback?.invoke()
        return false
    }

    fun setModifyRequesting(requesting: Boolean) {
        this.modifyRequesting = requesting
    }

    fun onModifyLimit(callback: () -> Unit) {
        this.modifyLimitCallback = callback
    }

    private fun recordMoveTimestamp() {
        moveControlQueue.add(System.currentTimeMillis())
    }

    fun setClickRectListener(callback: (index: Int) -> Unit) {
        cropOverlayView.clickRectListener = callback
    }

    fun getCurrentSelectCrop(): CropWindowHandler? {
        return cropOverlayView.currentMoveCropHandler
    }

    fun setBizScene(scene: CropBizScene) {
        bizSceneType = scene
    }

    fun setDoubleTapEnable(enable: Boolean) {
        photoView.attacher.setDoubleTapEnable(enable)
    }

    fun setClickSubRectListener(callback: (index: Int?, subIndex: Int?) -> Unit) {
        subRectClickListener = callback
    }

    */
/**
     * 返回 index 对应的选框
     * 如果题目类型为口算题，返回当前选中子题
     *//*

    private fun getTargetRect(index: Int?): RectF? {
        if (index == null) return null
        val cropHandlerWrapper = cropWindowHandlerMap[index] ?: return null
        if (cropHandlerWrapper.cropHandler == null) return null
        return if (cropHandlerWrapper.type == MentalCalculation) {
            cropHandlerWrapper.currentOralItemIndex?.let {
                val originIndex = cropHandlerWrapper.mapTargetIndex2SubIndex(it)
                cropHandlerWrapper.oralCropWindowHandlers?.get(originIndex)
            }?.rect ?: cropHandlerWrapper.cropHandler.rect
        } else {
            cropHandlerWrapper.cropHandler.rect
        }
    }

    fun setShowGuideAfterInit(show: Boolean) {
        this.showGuideAfterInit = show
    }

    fun onGuideViewShowed(callback: () -> Unit) {
        this.guideShowCallback = callback
    }

    fun setWindowPaddingBottom(size: Int, resetScale: Boolean) {
        Log.d(TAG, "call setWindowPaddingBottom:$size, $currentSelectIndex, $targetIndex")
        this.windowBottomPadding = size
        if (currentSelectIndex != null && targetIndex != currentSelectIndex) {
            moveToRect(index = currentSelectIndex, resetScale = resetScale)
        } else {
            moveToRect(resetScale = resetScale)
        }
    }

    */
/**
     * 将 index 对应的 rect 移动到当前视窗中心
     *//*

    fun moveToRect(index: Int? = null, resetScale: Boolean = false) {
        index?.let { targetIndex = it }
        this.resetScale = this.resetScale || resetScale
        applyChange()
        hideGuideView()
    }

    override fun setSelectRect(index: Int) {
        super.setSelectRect(index)
        currentSelectIndex = index
    }

    override fun setSelectRectNoCallback(index: Int) {
        super.setSelectRectNoCallback(index)
        currentSelectIndex = index
    }

    private fun applyChange() {
        removeCallbacks(updateRunnable)
        post(updateRunnable)
    }

    private fun changePhotoRect() {
        val attacher = photoView.attacher ?: return
        val viewWidth = this.width.takeIf { it > 0 } ?: return
        val viewHeight = this.height.takeIf { it > 0 } ?: return
        // 当前图片 Rect
        val srcAll = attacher.displayRect ?: return
        // 当前选中框
        val srcSmall = getTargetRect(targetIndex) ?: getCurrentSelectCrop()?.rect ?: return
        Log.d(TAG, "changePhotoRect: target=${targetIndex} , src=${srcSmall.toShortString()}")
        val centerX = viewWidth / 2F
        val centerY = (viewHeight - windowBottomPadding) / 2F
        val scale = if (resetScale) {
            1F / attacher.scale
        } else {
            1F
        }
        val dst = RectF().apply {
            set(
                centerX - srcSmall.width() * scale / 2F,
                centerY - srcSmall.height() * scale / 2F,
                centerX + srcSmall.width() * scale / 2F,
                centerY + srcSmall.height() * scale / 2F
            )
            if (this.top < rectSafeMarginTop) {
                offset(0F, rectSafeMarginTop - this.top)
            }
            set(
                scale * (srcAll.left - srcSmall.left) + this.left,
                scale * (srcAll.top - srcSmall.top) + this.top,
                scale * (srcAll.right - srcSmall.right) + this.right,
                scale * (srcAll.bottom - srcSmall.bottom) + this.bottom,
            )
        }
        attacher.boundPaddingBottom = windowBottomPadding
        attacher.animateRectToRect(srcAll, dst)
    }

    private fun updateStableSize(useStableSize: Boolean) {
        if (useStableSize) {
            if (photoView.width > 0 && photoView.height > 0) {
                photoView.attacher.apply {
                    setStableViewSize(photoView.width, photoView.height)
                    setOverDragPadding(photoView.width / 4)
                }
            } else {
                photoView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View?,
                        left: Int,
                        top: Int,
                        right: Int,
                        bottom: Int,
                        oldLeft: Int,
                        oldTop: Int,
                        oldRight: Int,
                        oldBottom: Int
                    ) {
                        photoView.attacher.apply {
                            setStableViewSize(photoView.width, photoView.height)
                            setOverDragPadding(photoView.width / 4)
                        }
                        photoView.removeOnLayoutChangeListener(this)
                    }

                })
            }
        } else {
            photoView.attacher.setStableViewSize(0, 0)
        }
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        photoView.attacher.boundPaddingBottom = windowBottomPadding
        super.setImageBitmap(bitmap)
    }

    override fun getLayoutResId(): Int {
        return R.layout.solution_crop_cropper_image_view
    }

    */
/**
     * View 点击。根据坐标过滤出选择的框体
     *//*

    private fun onViewClick(x: Float, y: Float) {
        val clickRect = cropWindowHandlerMap.entries.find {
            it.value.cropHandler?.rect?.contains(x, y) == true
        }
        val clickSubRect = clickRect?.value?.oralCropWindowHandlers?.entries?.find {
            it.value.rect.contains(x, y)
        }
        if (clickRect != null && clickSubRect != null && subRectClickListener != null) {
            // 小题点击
            val index = clickRect.key
            val targetIndex = cropWindowHandlerMap[index]?.mapSubIndex2TargetIndex(clickSubRect.key)
            if (targetIndex == null || targetIndex < 0) {
                // 点击的小题不可选中，降级为大题点击
//                cropOverlayView.clickRectListener?.invoke(clickRect.key)
                return
            } else {
                val subIndex = clickSubRect.key
                cropWindowHandlerMap[index]?.currentOralItemIndex = subIndex
                (cropOverlayView as? SolutionCropOverlayView)?.setSubItemSelected()
                showTargetQuestionNumber(index)
                subRectClickListener?.invoke(clickRect.key, targetIndex)
            }
        } else if (clickRect != null) {
            cropOverlayView.clickRectListener?.invoke(clickRect.key)
        }
    }

    */
/**
     * 图片缩放平移后修改标记位置
     * 题号、小题题号、批改标记、小题批改标记
     *//*

    private fun layoutChildren() {
        // 引导
        if (guideView != null) {
            val selectRect = getCurrentSelectCrop()?.rect
            if (selectRect != null) {
                layoutGuideView(selectRect)
            }
        }
        cropWindowHandlerMap.forEach { entry ->
            val (index, windowHandlerWrapper) = entry
            if (windowHandlerWrapper.cropHandler != null) {
                // 题号
                numberMap[index]?.let {
                    updateQuestionNumLocation(it, windowHandlerWrapper.cropHandler.rect)
                }
                // 批改标记
                resultTagMap[index]?.let {
                    if (it.measuredHeight > 0 && it.measuredWidth > 0) {
                        updateCorrectStateLocation(
                            it,
                            windowHandlerWrapper.cropHandler.rect,
                            windowHandlerWrapper.correctState,
                            false,
                            it.measuredWidth,
                            it.measuredHeight
                        )
                    }
                }

                subItemResultTagMap[index]?.forEachIndexed { imgIdx, img ->
                    if (img != null && img.measuredHeight > 0 && img.measuredWidth > 0) {
                        updateSubCorrectStateLocation(
                            img,
                            windowHandlerWrapper.subItemCropWindowHandlers?.get(imgIdx)?.rect,
                            windowHandlerWrapper.subItemInfo?.getOrNull(imgIdx)?.correctStatus,
                            img.measuredWidth,
                            img.measuredHeight
                        )
                    }
                }

            }
            // 口算小题
            windowHandlerWrapper.mentalCalcInfo?.forEachIndexed { subIndex, mentalRectEntity ->
                val subCropWindowHandler = windowHandlerWrapper.oralCropWindowHandlers?.get(subIndex) ?: return@forEachIndexed
                val targetIndex = windowHandlerWrapper.mapSubIndex2TargetIndex(subIndex)
                // 题号
                if (targetIndex != null) {
                    subNumberMap[index]?.getOrNull(targetIndex)?.let {
                        updateSubQuestionNumLocation(it, subCropWindowHandler.rect)
                    }
                }
                // 批改标记
                oralResultTagMap[index]?.getOrNull(subIndex)?.let {
                    if (it.measuredHeight > 0 && it.measuredWidth > 0) {
                        updateCorrectStateLocation(
                            it, subCropWindowHandler.rect, mentalRectEntity.correctStatus,
                            true, it.measuredWidth, it.measuredHeight
                        )
                    }
                }
            }


        }
    }

    */
/**
     * 更新题号坐标
     *//*

    private fun updateQuestionNumLocation(view: QuestionNumberView, rect: RectF) {
        view.x = rect.left + (rect.width() - NUMBER_WIDTH.dp) / 2
        view.y = if (view.getArrowLocation() == Top) rect.top else rect.top - view.measuredHeight
    }

    */
/**
     * 更新小题题号坐标
     *//*

    private fun updateSubQuestionNumLocation(view: QuestionNumberView, rect: RectF?) {
        rect ?: return
        view.x = rect.left + (rect.width() - SUB_NUMBER_WIDTH.dp) / 2
        view.y = if (view.getArrowLocation() == Top) rect.bottom + MARGIN else rect.top - view.measuredHeight
    }

    */
/**
     * 更新批改标记坐标
     *//*

    private fun updateCorrectStateLocation(
        view: View,
        rect: RectF?,
        state: QuestionCorrectStatus?,
        isMentalSubItem: Boolean,
        imgWidth: Int,
        imgHeight: Int
    ) {
        if (rect == null) return
        val horMargin = (view.getTag(R.id.view_correct_state_hor_margin) as? Int) ?: run {
            if (isMentalSubItem && (state == Wrong || state == NoAnswer) && rect.right < UiUtil.getScreenWidth(context) - 2 * imgWidth) {
                MARGIN * 2
            } else {
                - imgWidth - MARGIN
            }.also { view.setTag(R.id.view_correct_state_hor_margin, it) }
        }
        val verMargin = (view.getTag(R.id.view_correct_state_ver_margin) as? Int) ?: run {
            if (state == Right && isMentalSubItem) {
                - imgHeight + 4.dp
            } else if ((state == Wrong || state == NoAnswer) && isMentalSubItem) {
                0
            } else {
                - imgHeight - MARGIN
            }.also { view.setTag(R.id.view_correct_state_ver_margin, it) }
        }
        view.x = if (isMentalSubItem && (state == Wrong || state == NoAnswer)) {
            if (imgWidth > rect.width()) {
                rect.left - (imgWidth - rect.width()) / 2
            } else {
                rect.left
            }
        } else {
            rect.right + horMargin
        }

        view.y = if (isMentalSubItem && (state == Wrong || state == NoAnswer)) {
            if (imgHeight > rect.height()) {
                rect.top - (imgHeight - rect.height()) / 2
            } else {
                rect.top
            }
        } else {
            rect.bottom + verMargin
        }

        if ((state == Wrong || state == NoAnswer) && isMentalSubItem) {
            val lp = view.layoutParams
            lp.width = rect.width().toInt().coerceAtLeast(20.dp)
            lp.height = rect.height().toInt().coerceAtLeast(12.dp)
            view.layoutParams = lp
        }


    }


    */
/**
     * 更新非口算子题批改标签坐标
     *//*

    private fun updateSubCorrectStateLocation(
        view: View,
        rect: RectF?,
        state: QuestionCorrectStatus?,
        imgWidth: Int,
        imgHeight: Int
    ) {
        if (rect == null) return
        view.x = if (state == Wrong) {
            if (imgWidth > rect.width()) {
                rect.left - (imgWidth - rect.width()) / 2
            } else {
                rect.left
            }
        } else {
            rect.right -imgWidth
        }
        view.y = if (state == Wrong) {

            if (imgHeight > rect.height()) {
                rect.top - (imgHeight - rect.height())
            } else {
                rect.top
            }
        } else {
            rect.bottom - imgHeight
        }

        if (state == Wrong) {
            val lp = view.layoutParams
            lp.width = rect.width().toInt().coerceAtLeast(20.dp)
            lp.height = rect.height().toInt().coerceAtLeast(12.dp)
            view.layoutParams = lp
        }
    }

    */
/**
     * 渲染题目框图
     *//*

    fun initRect(piece: List<QuestionPieceWrapper>) {

        piece.forEachIndexed { index, questionPiece ->
            val cropHandler = cropOverlayView.getCropWindowHandlers().getOrNull(index)
            cropWindowHandlerMap[index] = CropWindowHandlerWrapper(
                cropHandler = cropHandler,
                correctState = questionPiece.correctStatus,
                pieceId = questionPiece.pieceId,
                type = questionPiece.type,
                mentalCalcInfo = questionPiece.mentalCalcInfo,
                oralCropWindowHandlers = customCropOverlayView?.createSubWindow(questionPiece.mentalCalcInfo?.map { mentalEntity ->
                    mentalEntity.rect
                }),
                targetOralItem = if (bizSceneType == Page_Correct) questionPiece.mentalCalcInfo?.filter { oral ->
                    oral.correctStatus == Wrong || oral.correctStatus == NoAnswer || oral.correctStatus == QuestionCorrectStatus.Unknown
                } else questionPiece.mentalCalcInfo,

                subItemInfo = questionPiece.subItemRectInfo,
                subItemCropWindowHandlers = customCropOverlayView?.createSubWindow(questionPiece.subItemRectInfo?.map { subEntity ->
                    subEntity.rect
                })
            )
        }
        customCropOverlayView?.cropInfo = cropWindowHandlerMap
        customCropOverlayView?.updateRect()
        piece.forEachIndexed { index, questionPiece ->
            val cropHandler = cropOverlayView.getCropWindowHandlers().getOrNull(index)
            setCorrectResultTag(index)
            setQuestionNumberTag(index, cropHandler?.rect)
            if (questionPiece.type == MentalCalculation) {
                setOralItemNumberTag(index)
            }
        }
        checkAndShowGuideView()
    }

    */
/**
     * 重搜后更新口算大题
     *//*

    fun initTargetRect(piece: QuestionPieceWrapper?, index: Int) {
        if (piece == null || index == -1) return
        val cropHandler = cropWindowHandlerMap[index] ?: return
        cropHandler.type = piece.type
        if (piece.type == AIProblemSolving) return
        cropHandler.apply {
            currentOralItemIndex = 0
            type = piece.type
            mentalCalcInfo = piece.mentalCalcInfo
            targetOralItem = piece.mentalCalcInfo
            oralCropWindowHandlers = customCropOverlayView?.createSubWindow(piece.mentalCalcInfo?.map { mentalEntity ->
                mentalEntity.rect
            })
        }
        customCropOverlayView?.updateRect()
        setOralItemNumberTag(index)
        showTargetQuestionNumber(index)
    }


    fun updateRectState(index: Int, content: CorrectContent?) {
        ALog.e("chenbo", "updateRectState" + "index:${index},content:$content")
        val cropInfo = cropWindowHandlerMap[index] ?: return
        val originState = cropInfo.correctState
        val newSubItem = content?.subItems

        */
/**
         * 设置批改标签
         *//*

        if (content?.status != originState) {
            cropInfo.correctState = content?.status
            childrenContainer.removeView(resultTagMap[index])
            if (newSubItem.isNullOrEmpty() && cropInfo.subItemInfo == null) {

                */
/**
                 * 当没有子题时才渲染大题批改状态
                 *//*

                setCorrectResultTag(index)
            }
        }
        */
/**
         * 修改题目序号UI
         *//*

        val questionNumberTag = numberMap[index] ?: return
        questionNumberTag.updateUIByScene(content?.status)

        */
/**
         * 渲染子空批改状态
         *//*

        if (cropInfo.subItemInfo == null && newSubItem?.isNotEmpty() == true) {
            cropInfo.subItemInfo = content.toSubItemRectEntity()
            cropInfo.subItemCropWindowHandlers = customCropOverlayView?.createSubWindow(cropInfo.subItemInfo?.map { subEntity ->
                subEntity.rect
            })
            customCropOverlayView?.updateRect()
            subItemResultTagMap[index]?.forEach { subView ->
                childrenContainer.removeView(subView)
            }
            cropInfo.subItemInfo?.forEachIndexed { subIndex, sub ->
                val subItemResultTag = setSubItemCorrectResult(sub.correctStatus, cropInfo.subItemCropWindowHandlers?.get(subIndex)?.rect)
                val subResultTagList = subItemResultTagMap[index] ?: mutableListOf()
                subResultTagList.add(subIndex, subItemResultTag)
                subItemResultTagMap[index] = subResultTagList
            }
        }

    }

    */
/**
     * 1.在框选View的右下角打上批改状态标签
     * 2.v1.5.0新增小空框选及批改结果标记
     *//*

    private fun setCorrectResultTag(index: Int) {
        val cropInfo = cropWindowHandlerMap[index] ?: return
        val subItem = cropInfo.subItemInfo


        if(subItem.isNullOrEmpty()){
            */
/**
             * 给题目渲染批改状态
             * 口算:小题状态
             * 非口算:整题状态
             *//*

            if (cropInfo.type == MentalCalculation) {
                setOralCorrectResultTag(index)
            } else {
                setCorrectResultTag(cropInfo.cropHandler?.rect, cropInfo.correctState, index)
            }
        }else {
            */
/**
             * v1.5.0:给非口算题的子空渲染批改状态
             *//*

            subItem.forEachIndexed { subIndex, sub ->
                val subItemResultTag = setSubItemCorrectResult(sub.correctStatus, cropInfo.subItemCropWindowHandlers?.get(subIndex)?.rect)
                val subResultTagList = subItemResultTagMap[index] ?: mutableListOf()
                subResultTagList.add(subIndex, subItemResultTag)
                subItemResultTagMap[index] = subResultTagList
            }
        }


    }

    */
/**
     * 非口算题子空渲染批改结果
     *//*

    private fun setSubItemCorrectResult(state: QuestionCorrectStatus?,rect:RectF?): ImageView? {
        if (state == null || rect == null) return null
        val imageId = when (state) {
            Right -> {
                if (rect.height() > 16.dp)
                    R.drawable.ic_correct_right_big
                else R.drawable.ic_correct_right_little
            }

            Wrong -> {

                R.drawable.solution_red_circle

            }

            HalfRight -> {
                R.drawable.ic_correct_half_right
            }

            else -> {
                null
            }
        } ?: return null

        val drawable = ResourcesCompat.getDrawable(context.resources, imageId, null)

        val imgWidth = if (state == Wrong ) {
            Math.max(rect.width().toInt(), 20.dp)
        } else {
            drawable?.intrinsicWidth ?: 0
        }

        val imgHeight = if (state == Wrong) {
            Math.max(rect.height().toInt(), 12.dp)
        } else {
            drawable?.intrinsicHeight ?: 0
        }
        val subResultImg = ImageView(context).apply {
            setImageResource(imageId)
            elevation = 1.0f
            scaleType = FIT_XY
            layoutParams = ViewGroup.LayoutParams(imgWidth, imgHeight)
            // 更新坐标。必须放在 setLayoutParams 后面执行
            updateSubCorrectStateLocation(this, rect, state, imgWidth, imgHeight)
        }
        childrenContainer.addView(subResultImg)
        return subResultImg
    }

    private fun addCorrectStateView(
        state: QuestionCorrectStatus?,
        rect: RectF,
        isMentalSubItem: Boolean = false
    ): ImageView? {
        val imgId = when (state) {
            QuestionCorrectStatus.Correcting -> {
                R.drawable.ic_correct_correcting
            }

            Right -> {
                if (rect.height() > 16.dp)
                    R.drawable.ic_correct_right_big
                else R.drawable.ic_correct_right_little
            }

            HalfRight -> {
                R.drawable.ic_correct_half_right
            }

            Wrong -> {
                if (isMentalSubItem) {
                    R.drawable.solution_red_circle
                } else R.drawable.ic_correct_wrong_text
            }

            NoAnswer -> {
                if (isMentalSubItem) {
                    R.drawable.solution_red_circle
                } else null
            }


            else -> {
                null
            }
        } ?: return null
        val drawable = ResourcesCompat.getDrawable(context.resources, imgId, null)
        val imgWidth = if (isMentalSubItem && (state == Wrong || state == NoAnswer)) {
            rect.width().toInt().coerceAtLeast(20.dp)
        } else {
            drawable?.intrinsicWidth ?: 0
        }
        val imgHeight = if (isMentalSubItem && (state == Wrong || state == NoAnswer)) {
            rect.height().toInt().coerceAtLeast(12.dp)
        } else {
            drawable?.intrinsicHeight ?: 0
        }

        val correctResultImg = ImageView(context).apply {
            setImageResource(imgId)
            elevation = 1.0f
            scaleType = FIT_XY
            layoutParams = ViewGroup.LayoutParams(imgWidth, imgHeight)
            // 更新坐标。必须放在 setLayoutParams 后面执行
            updateCorrectStateLocation(this, rect, state, isMentalSubItem, imgWidth, imgHeight)
        }
        childrenContainer.addView(correctResultImg)
        return correctResultImg
    }

    */
/**
     * 口算子题设置批改结果标签
     *//*

    private fun setOralCorrectResultTag(index: Int) {
        val cropInfo = cropWindowHandlerMap[index] ?: return
        if (cropInfo.mentalCalcInfo == null) return
        cropInfo.mentalCalcInfo?.forEachIndexed { subIndex, mentalRectEntity ->
            val correctResultImg = cropInfo.oralCropWindowHandlers?.get(subIndex)?.rect?.let { rect ->
                addCorrectStateView(mentalRectEntity.correctStatus, rect, true)
            }
            val subResultTagList = oralResultTagMap[index] ?: mutableListOf()
            subResultTagList.add(subIndex, correctResultImg)
            oralResultTagMap[index] = subResultTagList
        }

    }


    */
/**
     * 大题设置批改标签
     *//*

    private fun setCorrectResultTag(rect: RectF?, state: QuestionCorrectStatus?, index: Int) {
        if (rect == null || state == null) return
        val correctResultImg = addCorrectStateView(state, rect) ?: return
        resultTagMap[index] = correctResultImg
    }

    private fun setQuestionNumberTag(index: Int, rect: RectF?) {
        if (rect == null) return
        val cropInfo = cropWindowHandlerMap[index] ?: return
        val questionNumberView = QuestionNumberView(context).apply {
            setQuestionNumber((index + 1).toString())
            measure(
                MeasureSpec.makeMeasureSpec(NUMBER_WIDTH.dp, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            elevation = 2.0f
            setCropScene(CropBizSceneWrapper(bizSceneType, AIProblemSolving, if (rect.top < this.measuredHeight + MARGIN) Top else Bottom))
            updateUIByScene(cropInfo.correctState)
            visibility = View.GONE
            layoutParams = ViewGroup.LayoutParams(NUMBER_WIDTH.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
            updateQuestionNumLocation(this, rect)
        }
        childrenContainer.addView(questionNumberView)
        numberMap[index] = questionNumberView
    }

    private fun setOralItemNumberTag(index: Int) {
        val cropInfo = cropWindowHandlerMap[index] ?: return
        cropInfo.targetOralItem?.forEachIndexed { subIndex, mentalEntity ->
            mentalEntity.rect?.let {
                val subNumberTag = QuestionNumberView(context).apply {
                    setQuestionNumber("${mentalEntity.pieceIndex + 1}" + "-" + "${subIndex + 1}")
                    measure(
                        MeasureSpec.makeMeasureSpec(SUB_NUMBER_WIDTH.dp, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED)
                    )
                    setCropScene(CropBizSceneWrapper(bizSceneType, MentalCalculation, if (it.top < this.measuredHeight + MARGIN) Top else Bottom))
                    updateUIByScene(mentalEntity.correctStatus)
                    visibility = View.GONE
                    layoutParams = ViewGroup.LayoutParams(SUB_NUMBER_WIDTH.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
                    val rectIndex = cropInfo.mapTargetIndex2SubIndex(subIndex)
                    if (rectIndex != null) {
                        updateSubQuestionNumLocation(this, cropInfo.oralCropWindowHandlers?.get(rectIndex)?.rect)
                    }
                }
                childrenContainer.addView(subNumberTag)
                val subNumberTagList = subNumberMap[index] ?: mutableListOf()
                subNumberTagList.add(subNumberTag)
                subNumberMap[index] = subNumberTagList
            }
        }
    }

    */
/**
     * 展示当前选中题目的序号
     *//*


    fun showTargetQuestionNumber(index: Int?) {
        val cropInfo = cropWindowHandlerMap[index] ?: return
        lastVisibleNumberView?.gone()
        lastVisibleNumberView = if (cropInfo.type == MentalCalculation && (cropInfo.targetOralItem?.isNotEmpty() == true || bizSceneType == Page_Search)) {
            subNumberMap[index]?.getOrNull((cropInfo.currentOralItemIndex) ?: 0)
        } else {
            numberMap[index]
        }
        lastVisibleNumberView?.visible()
    }

    fun setSelectRect(index: Int, subIndex: Int?) {
        if (subIndex == null) {
            //选中大题
            setSelectRect(index)
        } else {
            setSelectRect(index)
            if (cropWindowHandlerMap[index]?.type != QuestionSearchType.MentalCalculation) return
            cropWindowHandlerMap[index]?.currentOralItemIndex = subIndex
            cropOverlayView.invalidate()
            showTargetQuestionNumber(index)
        }
    }

    fun getTargetSubItemIndex(index: Int): Int {
        return cropWindowHandlerMap[index]?.currentOralItemIndex ?: 0
    }

    */
/**
     * 获取当前大题对应的小题index
     *//*

    fun getCurrentSubItemIndex(index: Int): Int {
        val cropInfo = cropWindowHandlerMap[index] ?: return 0
        if (cropInfo.type == QuestionSearchType.AIProblemSolving) return 0
        return cropInfo.currentOralItemIndex ?: 0
    }

    */
/**
     * 展示引导动画
     *//*

    private fun checkAndShowGuideView() {
        if (!showGuideAfterInit || hasGuideViewShowed) return
        val selectRect = getCurrentSelectCrop()?.rect ?: return
        val windowWidth = photoView.attacher.windowWidth
        val windowHeight = photoView.attacher.windowHeight
        if (windowHeight <= 0 || windowWidth <= 0) return

        val left = selectRect.right.toInt() - GUIDE_LEFT_MARGIN.dp
        val top = selectRect.bottom.toInt() - GUIDE_TOP_MARGIN.dp

        val width = 270.dp
        val height = 188.dp
        val bound = Rect(0, 0, windowWidth, windowHeight)
        val guideRect = Rect(left, top, left + width, top + height)
        // lottie周边空白距离，需要减掉
        val paddingHor = 54.dp
        val paddingVer = 39.dp
        guideRect.inset(paddingHor, paddingVer)

        if (!bound.contains(guideRect)) {
            // 引导动画超出边界，不展示
            return
        }
        hasGuideViewShowed = true
        if (guideView == null) {
            guideView = LottieAnimationView(context).apply {
                repeatCount = LottieDrawable.INFINITE
                imageAssetsFolder = "solution_move_guide/images"
                setAnimation("solution_move_guide/data.json")
                childrenContainer.addView(this, LayoutParams(width, height))
            }
            layoutGuideView(selectRect)
        }
        guideView?.let {
            it.playAnimation()
            guideShowCallback?.invoke()
            postDelayed({ hideGuideView() }, 5000)
        }
    }

    private fun layoutGuideView(rect: RectF) {
        guideView?.let {
            it.x = rect.right - GUIDE_LEFT_MARGIN.dp
            it.y = rect.bottom - GUIDE_TOP_MARGIN.dp
        }
    }

    private fun hideGuideView() {
        if (hasGuideViewShowed) {
            guideView?.apply {
                cancelAnimation()
                childrenContainer.removeView(this)
                guideView = null
            }
        }
    }

}

data class CropWindowHandlerWrapper(
    val cropHandler: CropWindowHandler?,
    var correctState: QuestionCorrectStatus?,
    val pieceId:Long?,
    var type: QuestionSearchType?,
    var mentalCalcInfo: List<MentalRectEntity>?,
    var oralCropWindowHandlers: MutableMap<Int, CropWindowHandler>?, // key: index in mentalCalcInfo 口算子题信息
    var currentOralItemIndex: Int? = 0, //口算题当前选中的子题index
    var targetOralItem: List<MentalRectEntity>?, //作业批改只展示错误/未作答的口算小题，页搜展示所有小题
    var subItemInfo: List<SubItemRectEntity>?, //v1.5.0 非口算题新增子题批改结果
    var subItemCropWindowHandlers: MutableMap<Int, CropWindowHandler>? //非口算子题信息
) {

    fun mapSubIndex2TargetIndex(indexInMetal: Int): Int? {
        val entry = mentalCalcInfo?.getOrNull(indexInMetal) ?: return null
        return targetOralItem?.indexOfFirst { it.itemIndex == entry.itemIndex }
    }

    fun mapTargetIndex2SubIndex(targetIndex: Int): Int? {
        val target = targetOralItem?.getOrNull(targetIndex)
        return mentalCalcInfo?.indexOfFirst { it.itemIndex == target?.itemIndex }
    }

    fun getSelectIndexInMental(): Int? {
        val selectIndex = currentOralItemIndex ?: return null
        return mapTargetIndex2SubIndex(selectIndex)
    }
}

enum class CropBizScene {
    Page_Search,
    Page_Correct
}
*/
