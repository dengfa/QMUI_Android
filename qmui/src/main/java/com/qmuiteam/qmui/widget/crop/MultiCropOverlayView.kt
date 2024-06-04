package com.qmuiteam.qmui.widget.crop

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.miracle.photo.crop.CropWindowHandler
import com.miracle.photo.crop.CropWindowMoveHandler
import com.miracle.photo.crop.RectAndIndex
import com.qmuiteam.qmui.R
import java.util.*

/** A custom View representing the crop window and the shaded background outside the crop window.  */
open class MultiCropOverlayView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) :
    View(context, attrs) {

    var movableMode: Boolean = true
    var clickRectListener: ((index: Int) -> Unit)? = null
    private var cropRectNumber: Int = 0 // 裁剪框数量

    protected val mCropWindowHandlers: List<CropWindowHandler> = List(20) { CropWindowHandler() }
    open var currentMoveCropHandler: CropWindowHandler? = mCropWindowHandlers.first()

    //记录矩形与index的对应关系，移动后需要找回原index
    protected var cropWindowHandlerMap = mutableMapOf<CropWindowHandler, Int>()

    //记录移动的矩形List
    private var cropRectMovingList = mutableSetOf<CropWindowHandler>()

    /**
     * The bounding box around the Bitmap that we are cropping.
     */
    val mBoundsPoints = FloatArray(8)

    /**
     * The bounding box around the Bitmap that we are cropping.
     */
    private val mCalcBounds = RectF()

    /**
     * the initial crop window rectangle to set
     */
    protected var mInitialCropWindowRects: List<Rect> = listOf()

    /**
     * Listener to publicj crop window changes
     */
    private var mCropMoveGestureListener: CropOverlayView.CropMoveGestureListener? = null

    /**
     * The Paint used to draw the white rectangle around the crop area.
     */
    var mUnSelectedBorderPaint: Paint? = null

    var mSelectBorderPaint: Paint? = null

    /**
     * The Paint used to darken the surrounding areas outside the crop area.
     */
    var mBackgroundPaint: Paint? = null

    var mUnselectRectPaint: Paint = getNewPaint(Color.parseColor("#1f000000"))

    /**
     * The bounding image view width used to know the crop overlay is at view edges.
     */
    private var mViewWidth = 0

    /**
     * The bounding image view height used to know the crop overlay is at view edges.
     */
    private var mViewHeight = 0

    /**
     * The initial crop window padding from image borders
     */
    var minitialHorizonCropWindowPaddingRatio = 0f
    var minitialVerticalCropWindowPaddingRatio = 0f

    /**
     * The radius of the touch zone (in pixels) around a given Handle.
     */
    protected var mTouchRadius = 0f
    protected var touchBorderRadius = 0f

    /**
     * An edge of the crop window will snap to the corresponding edge of a specified bounding box when
     * the crop window edge is less than or equal to this distance (in pixels) away from the bounding
     * box edge.
     */
    private var mSnapRadius = 0f

    /**
     * The Handle that is currently pressed; null if no Handle is pressed.
     */
    private var mMoveHandler: CropWindowMoveHandler? = null


    /**
     * Whether the Crop View has been initialized for the first time
     */
    protected var initializedCropWindow = false


    var scaleFactorWidth: Float = 1f

    var scaleFactorHeight: Float = 1f


    fun setCropMoveGestureListener(listener: CropOverlayView.CropMoveGestureListener?) {
        mCropMoveGestureListener = listener
    }

    fun getCropWindowHandlers(): List<CropWindowHandler> {
        return mCropWindowHandlers.take(cropRectNumber)
    }


    /**
     * Informs the CropOverlayView of the image's position relative to the ImageView. This is
     * necessary to call in order to draw the crop window.
     *
     * @param boundsPoints the image's bounding points
     * @param viewWidth    The bounding image view width.
     * @param viewHeight   The bounding image view height.
     */
    open fun setBounds(boundsPoints: FloatArray?, viewWidth: Int, viewHeight: Int) {
        if (boundsPoints == null || !Arrays.equals(mBoundsPoints, boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(mBoundsPoints, 0f)
            } else {
                System.arraycopy(boundsPoints, 0, mBoundsPoints, 0, boundsPoints.size)
            }
            mViewWidth = viewWidth
            mViewWidth = viewWidth
            mViewHeight = viewHeight
            /*            val cropRect = mCropWindowHandler.rect
                        if (cropRect.width() == 0f || cropRect.height() == 0f) {
                            initCropWindow()
                        }*/
        }
    }

    /**
     * Resets the crop overlay view.
     */
    fun resetCropOverlayView() {
        if (initializedCropWindow) {
            mCropWindowHandlers.forEach {
                it.rect = BitmapUtils.EMPTY_RECT_F
            }
            initCropWindow()
            invalidate()
        }
    }

    /**
     * An edge of the crop window will snap to the corresponding edge of a specified bounding box when
     * the crop window edge is less than or equal to this distance (in pixels) away from the bounding
     * box edge. (default: 3)
     */
    fun setSnapRadius(snapRadius: Float) {
        mSnapRadius = snapRadius
    }


    // region: Private methods

    /**
     * set the max width/height and scale factor of the shown image to original image to scale the
     * limits appropriately.
     */
    fun setCropWindowLimits(
        maxWidth: Float, maxHeight: Float, scaleFactorWidth: Float, scaleFactorHeight: Float
    ) {
        this.scaleFactorHeight = scaleFactorHeight
        this.scaleFactorWidth = scaleFactorWidth
        mCropWindowHandlers.forEach {
            it.setCropWindowLimits(
                maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight
            )
        }
    }

    fun setInitCropRect(rects: List<Rect>) {
        cropRectNumber = rects.size
        mInitialCropWindowRects = rects

        initCropWindow()
        invalidate()
    }

    /**
     * Sets all initial values, but does not call initCropWindow to reset the views.<br></br>
     * Used once at the very start to initialize the attributes.
     */
    fun setInitialAttributeValues(options: CropImageOptions) {
        mCropWindowHandlers.forEach {
            it.setInitialAttributeValues(options)
        }
        setSnapRadius(options.snapRadius)
        mTouchRadius = options.touchRadius
        touchBorderRadius = options.touchBorderRadius
        minitialHorizonCropWindowPaddingRatio = options.initialHorizonCropWindowPaddingRatio
        minitialVerticalCropWindowPaddingRatio = options.initialVerticalCropWindowPaddingRatio
        mUnSelectedBorderPaint = getNewPaintOrNull(0.5f, options.unSelectedBorderLineColor)
        mSelectBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.selectedBorderLineColor)
        mBackgroundPaint = getNewPaint(options.backgroundColor)
        mUnselectRectPaint = getNewPaint(options.unSelectedBorderSolidColor)
    }

    /**
     * Set the initial crop window size and position. This is dependent on the size and position of
     * the image being cropped.
     */
    protected open fun initCropWindow() {
        val leftLimit = BitmapUtils.getRectLeft(mBoundsPoints).coerceAtLeast(0f)
        val topLimit = BitmapUtils.getRectTop(mBoundsPoints).coerceAtLeast(0f)
        val rightLimit = BitmapUtils.getRectRight(mBoundsPoints).coerceAtMost(width.toFloat())
        val bottomLimit = BitmapUtils.getRectBottom(mBoundsPoints).coerceAtMost(height.toFloat())
        /*if (rightLimit <= leftLimit || bottomLimit <= topLimit) {
            return
        }*/
        mInitialCropWindowRects.forEachIndexed { index, initRect ->
            val rect = RectF()
            val cropHandler = mCropWindowHandlers.getOrNull(index) ?: return@forEachIndexed
            // Tells the attribute functions the crop window has already been initialized
            val horizontalPadding = minitialHorizonCropWindowPaddingRatio * (rightLimit - leftLimit)
            val verticalPadding = minitialVerticalCropWindowPaddingRatio * (bottomLimit - topLimit)
            if (initRect.width() > 0 && initRect.height() > 0) {
                // Get crop window position relative to the displayed image.
                rect.left =
                    leftLimit + initRect.left / (cropHandler?.scaleFactorWidth ?: 1f)
                rect.top = topLimit + initRect.top / (cropHandler?.scaleFactorHeight ?: 1f)
                rect.right =
                    rect.left + initRect.width() / (cropHandler?.scaleFactorWidth ?: 1f)
                rect.bottom =
                    rect.top + initRect.height() / (cropHandler?.scaleFactorHeight ?: 1f)

                // Correct for floating point errors. Crop rect boundaries should not exceed the source Bitmap
                // bounds.
                rect.left = Math.max(leftLimit, rect.left)
                rect.top = Math.max(topLimit, rect.top)
                rect.right = Math.min(rightLimit, rect.right)
                rect.bottom = Math.min(bottomLimit, rect.bottom)
            } else {
                // Initialize crop window to have 10% padding w/ respect to image.
                rect.left = leftLimit + horizontalPadding
                rect.top = topLimit + verticalPadding
                rect.right = rightLimit - horizontalPadding
                rect.bottom = bottomLimit - verticalPadding
            }
            if (index >= 0 && index < mCropWindowHandlers.size) {
                fixCropWindowRectByRules(rect, mCropWindowHandlers[index])
            }
            cropHandler.rect = rect
            cropHandler.initRect = rect
            if (cropHandler != null) {
                cropWindowHandlerMap[cropHandler] = index
            }
        }
        initializedCropWindow = true
    }

    /**
     * Fix the given rect to fit into bitmap rect and follow min, max and aspect ratio rules.
     */
    private fun fixCropWindowRectByRules(rect: RectF, cropWindowHandler: CropWindowHandler) {
        if (rect.width() < cropWindowHandler.fixedMinCropWidth) {
            val adj = (cropWindowHandler.fixedMinCropWidth - rect.width()) / 2
            rect.left -= adj
            rect.right += adj
        }
        if (rect.height() < cropWindowHandler.fixedMinCropHeight) {
            val adj = (cropWindowHandler.fixedMinCropHeight - rect.height()) / 2
            rect.top -= adj
            rect.bottom += adj
        }
        if (rect.width() > cropWindowHandler.fixedMaxCropWidth) {
            val adj = (rect.width() - cropWindowHandler.fixedMaxCropWidth) / 2
            rect.left += adj
            rect.right -= adj
        }
        if (rect.height() > cropWindowHandler.fixedMaxCropHeight) {
            val adj = (rect.height() - cropWindowHandler.fixedMaxCropHeight) / 2
            rect.top += adj
            rect.bottom -= adj
        }
        calculateBounds()
        if (mCalcBounds.width() > 0 && mCalcBounds.height() > 0) {
            val leftLimit = Math.max(mCalcBounds.left, 0f)
            val topLimit = Math.max(mCalcBounds.top, 0f)
            val rightLimit = Math.min(mCalcBounds.right, width.toFloat())
            val bottomLimit = Math.min(mCalcBounds.bottom, height.toFloat())
            if (rect.left < leftLimit) {
                rect.left = leftLimit
            }
            if (rect.top < topLimit) {
                rect.top = topLimit
            }
            if (rect.right > rightLimit) {
                rect.right = rightLimit
            }
            if (rect.bottom > bottomLimit) {
                rect.bottom = bottomLimit
            }
        }
    }

    /**
     * Draw crop overview by drawing background over image not in the cripping area, then borders and
     * guidelines.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawBorders(canvas)
        getCropWindowHandlers().forEach {
            if (it == currentMoveCropHandler && movableMode) {
                drawCorners(canvas, it.rect)
            }
        }
    }

    fun getAdjustCropWindow(): List<Int> {
        val adjustList = mutableListOf<Int>()
        cropRectMovingList.forEach {
            cropWindowHandlerMap[it]?.let { cropIndex ->
                adjustList.add(cropIndex)
            }
        }
        return adjustList
    }

    fun getRadioRect(): List<RectAndIndex> {
        val res = mutableListOf<RectAndIndex>()
        val left = BitmapUtils.getRectLeft(mBoundsPoints).coerceAtLeast(0f)
        val top = BitmapUtils.getRectTop(mBoundsPoints).coerceAtLeast(0f)
        val right = BitmapUtils.getRectRight(mBoundsPoints).coerceAtMost(width.toFloat())
        val bottom = BitmapUtils.getRectBottom(mBoundsPoints).coerceAtMost(height.toFloat())
        getCropWindowHandlers().forEach {
            val topRadio = (it.rect.top - top) / (bottom - top)
            val bottomRadio = (it.rect.bottom - top) / (bottom - top)
            val leftRadio = (it.rect.left - left) / (right - left)
            val rightRadio = (it.rect.right - left) / (right - left)

            val rectF = RectF(leftRadio, topRadio, rightRadio, bottomRadio)
            val index = cropWindowHandlerMap[it]
            res.add(RectAndIndex(rectF, index ?: 0))
        }

        return res
    }

    val clipPath = Path()
    val borderRadius = 5f

    /**
     * Draw shadow background over the image not including the crop area.
     */
    open fun drawBackground(canvas: Canvas) {
        val left = BitmapUtils.getRectLeft(mBoundsPoints).coerceAtLeast(0f)
        val top = BitmapUtils.getRectTop(mBoundsPoints).coerceAtLeast(0f)
        val right = BitmapUtils.getRectRight(mBoundsPoints).coerceAtMost(width.toFloat())
        val bottom = BitmapUtils.getRectBottom(mBoundsPoints).coerceAtMost(height.toFloat())
        canvas.save()
        currentMoveCropHandler?.rect?.let {
            clipPath.reset()
            clipPath.addRoundRect(it, borderRadius, borderRadius, Path.Direction.CCW)
            canvas.clipPath(clipPath, Region.Op.DIFFERENCE)
        }
        canvas.drawRect(left, top, right, bottom, mBackgroundPaint!!)
        canvas.restore()

        drawUnselectedBackGround(canvas)
    }

    /**
     * 绘制未选中框的背景色
     */
    private fun drawUnselectedBackGround(canvas: Canvas) {
        canvas.save()
        getCropWindowHandlers().filter { it != currentMoveCropHandler }.forEach {
            canvas.drawRoundRect(it.rect, borderRadius, borderRadius, mUnselectRectPaint)
        }
        canvas.restore()
    }

    /**
     * Draw borders of the crop area.
     */
    open fun drawBorders(canvas: Canvas) {
        getCropWindowHandlers().forEach {
            if (it == currentMoveCropHandler && !movableMode) {
                if (mSelectBorderPaint != null) {
                    val w = mSelectBorderPaint!!.strokeWidth
                    val rect = it.rect
                    rect.inset(w / 2, w / 2)
                    // Draw rectangle crop window border.
                    canvas.drawRoundRect(rect, borderRadius, borderRadius, mSelectBorderPaint!!)
                }
            } else {
                if (mUnSelectedBorderPaint != null) {
                    val w = mUnSelectedBorderPaint!!.strokeWidth
                    val rect = it.rect
                    rect.inset(w / 2, w / 2)
                    // Draw rectangle crop window border.
                    canvas.drawRoundRect(rect, borderRadius, borderRadius, mUnSelectedBorderPaint!!)
                }
            }
        }
    }


    private val topHandlerBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.crop_handler_top)
    private val bottomHandlerBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.crop_handler_bottom)
    private val rightHandlerBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.crop_handler_right)
    private val leftHandlerBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.crop_handler_left)
    private val topLeftHandlerSmallBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.left_top_white_jiao_little)
    private val topLeftHandlerBigBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.left_top_white_jiao)
    private val bottomLeftHandlerSmallBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.left_bottom_white_jiao_little)
    private val bottomLeftHandlerBigBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.left_bottom_white_jiao)
    private val topRightHandlerSmallBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.right_top_white_jiao_little)
    private val topRightHandlerBigBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.right_top_white_jiao)
    private val bottomRightHandlerSmallBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.right_bottom_white_jiao_little)
    private val bottomRightHandlerBigBitmap =
        BitmapFactory.decodeResource(resources, R.drawable.right_bottom_white_jiao)

    /**
     * Draw the corner of crop overlay.
     */
    private fun drawCorners(canvas: Canvas, rect: RectF) {
        val lineThickness = 4.dpToPxFloat

        var bitmap =
            if (rect.width() < 80.dpToPxInt || rect.height() < 80.dpToPxInt) topLeftHandlerSmallBitmap else topLeftHandlerBigBitmap
        val width = bitmap.width
        canvas.drawBitmap(bitmap, rect.left - lineThickness, rect.top - lineThickness, Paint())

        bitmap =
            if (rect.width() < 80.dpToPxInt || rect.height() < 80.dpToPxInt) bottomLeftHandlerSmallBitmap else bottomLeftHandlerBigBitmap
        canvas.drawBitmap(
            bitmap,
            rect.left - lineThickness,
            rect.bottom - width + lineThickness,
            Paint()
        )

        bitmap =
            if (rect.width() < 80.dpToPxInt || rect.height() < 80.dpToPxInt) topRightHandlerSmallBitmap else topRightHandlerBigBitmap
        canvas.drawBitmap(
            bitmap,
            rect.right - width + lineThickness,
            rect.top - lineThickness,
            Paint()
        )

        bitmap =
            if (rect.width() < 80.dpToPxInt || rect.height() < 80.dpToPxInt) bottomRightHandlerSmallBitmap else bottomRightHandlerBigBitmap
        canvas.drawBitmap(
            bitmap,
            rect.right - width + lineThickness,
            rect.bottom - width + lineThickness,
            Paint()
        )

        if (rect.width() > 100.dpToPxInt){
            canvas.drawBitmap(
                topHandlerBitmap,
                (rect.left + rect.right)/2 - topHandlerBitmap.width/2,
                rect.top - lineThickness ,
                Paint()
            )
            canvas.drawBitmap(
                bottomHandlerBitmap,
                (rect.left + rect.right) / 2 - bottomHandlerBitmap.width / 2,
                rect.bottom,
                Paint()
            )
        }

        if (rect.height() > 100.dpToPxInt) {
            canvas.drawBitmap(
                leftHandlerBitmap,
                rect.left - lineThickness,
                (rect.top + rect.bottom) / 2 - leftHandlerBitmap.height / 2,
                Paint()
            )
            canvas.drawBitmap(
                rightHandlerBitmap,
                rect.right,
                (rect.top + rect.bottom) / 2 - rightHandlerBitmap.height / 2,
                Paint()
            )
        }
    }

    fun setSelectedRectIndex(index: Int) {
        val cropWindowHandler = mCropWindowHandlers.getOrNull(index) ?: return
        currentMoveCropHandler = cropWindowHandler
        clickRectListener?.invoke(index)
        invalidate()
    }

    fun setSelectedRectIndexNoCallback(index: Int) {
        val cropWindowHandler = mCropWindowHandlers.getOrNull(index) ?: return
        currentMoveCropHandler = cropWindowHandler
        // clickRectListener?.invoke(index)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // If this View is not enabled, don't allow for touch interactions.
        return if (isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(event.x, event.y)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                    onActionUp()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    onActionMove(event.x, event.y)
                    parent.requestDisallowInterceptTouchEvent(true)
                    true
                }

                else -> false
            }
        } else {
            false
        }
    }

    /**
     * On press down start crop window movment depending on the location of the press.<br></br>
     * if press is far from crop window then no move handler is returned (null).
     */
    private fun onActionDown(x: Float, y: Float): Boolean {
        getCropWindowHandlers().forEachIndexed { index, cropWindowHandler ->
            cropWindowHandler.getMoveHandler(x, y, mTouchRadius, touchBorderRadius)?.let {
                mMoveHandler = it
                currentMoveCropHandler = cropWindowHandler
                clickRectListener?.invoke(index)
                return@forEachIndexed
            }
        }

        if (mMoveHandler != null) {
            invalidate()
        }
        return mMoveHandler != null
    }

    /**
     * Clear move handler starting in [.onActionDown] if exists.
     */
    private fun onActionUp() {
        if (mMoveHandler != null) {
            callOnMoveGestureFinish(mMoveHandler!!.moveType)
            mMoveHandler = null
            invalidate()
        }
    }

    /**
     * Handle move of crop window using the move handler created in [.onActionDown].<br></br>
     * The move handler will do the proper move/resize of the crop window.
     */
    private fun onActionMove(x: Float, y: Float) {
        if (mMoveHandler != null && movableMode && currentMoveCropHandler != null) {
            var snapRadius = mSnapRadius
            val rect = currentMoveCropHandler!!.rect
            if (calculateBounds()) {
                snapRadius = 0f
            }
            mMoveHandler!!.move(
                rect,
                x,
                y,
                mCalcBounds,
                mViewWidth,
                mViewHeight,
                snapRadius,
                false,
                0f
            )
            currentMoveCropHandler!!.rect = rect
            cropRectMovingList.add(currentMoveCropHandler!!)
            invalidate()
        }
    }

    /**
     * Calculate the bounding rectangle for current crop window, handle non-straight rotation angles.
     * <br></br>
     * If the rotation angle is straight then the bounds rectangle is the bitmap rectangle, otherwsie
     * we find the max rectangle that is within the image bounds starting from the crop window
     * rectangle.
     *
     * @param rect the crop window rectangle to start finsing bounded rectangle from
     * @return true - non straight rotation in place, false - otherwise.
     */
    private fun calculateBounds(): Boolean {
        var left = BitmapUtils.getRectLeft(mBoundsPoints)
        var top = BitmapUtils.getRectTop(mBoundsPoints)
        var right = BitmapUtils.getRectRight(mBoundsPoints)
        var bottom = BitmapUtils.getRectBottom(mBoundsPoints)
        mCalcBounds[left, top, right] = bottom
        return false

    }


    private fun callOnMoveGestureFinish(moveType: CropWindowMoveHandler.Type) {
        if (mCropMoveGestureListener != null) {
            mCropMoveGestureListener!!.onCropMoveGestureFinish(moveType)
        }
    }
    // endregion
    // region: Inner class: ScaleListener

    companion object {
        // region: Fields and Consts
        private val HOLDER = RectF()

        /**
         * Creates the Paint object for drawing.
         */
        fun getNewPaint(color: Int): Paint {
            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = color
            return paint
        }

        /**
         * Creates the Paint object for given thickness and color, if thickness < 0 return null.
         */
        fun getNewPaintOrNull(thickness: Float, color: Int): Paint? {
            return if (thickness > 0) {
                val borderPaint = Paint()
                borderPaint.color = color
                borderPaint.strokeWidth = thickness
                borderPaint.style = Paint.Style.STROKE
                borderPaint.isAntiAlias = true
                borderPaint
            } else {
                null
            }
        }
    }
}