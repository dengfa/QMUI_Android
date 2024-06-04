
package com.qmuiteam.qmui.widget.crop

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
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
class CropOverlayView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    View(context, attrs) {
    /**
     * Handler from crop window stuff, moving and knowing possition.
     */
    private val mCropWindowHandler = CropWindowHandler()

    /**
     * The bounding box around the Bitmap that we are cropping.
     */
    private val mBoundsPoints = FloatArray(8)

    /**
     * The bounding box around the Bitmap that we are cropping.
     */
    private val mCalcBounds = RectF()

    /**
     * the initial crop window rectangle to set
     */
    private val mInitialCropWindowRect = Rect()

    /**
     * Listener to publicj crop window changes
     */
    private var mCropMoveGestureListener: CropMoveGestureListener? = null

    /**
     * The Paint used to draw the white rectangle around the crop area.
     */
    private var mBorderPaint: Paint? = null

    /**
     * The Paint used to darken the surrounding areas outside the crop area.
     */
    private var mBackgroundPaint: Paint? = null


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
    private var minitialHorizonCropWindowPaddingRatio = 0f
    private var minitialVerticalCropWindowPaddingRatio = 0f

    /**
     * The radius of the touch zone (in pixels) around a given Handle.
     */
    private var mTouchRadius = 0f
    private var touchBorderRadius = 0f

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
     * save the current aspect ratio of the image
     */
    private var mAspectRatioX = 0

    /**
     * save the current aspect ratio of the image
     */
    private var mAspectRatioY = 0

    /**
     * The aspect ratio that the crop area should maintain; this variable is only used when
     * mMaintainAspectRatio is true.
     */
    private var mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY


    /**
     * Whether the Crop View has been initialized for the first time
     */
    private var initializedCropWindow = false


    private var scanBitmap: Bitmap? = null
    private val scanPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var scaleW = 1f
    private var scaleH = 1f

    //记录移动的矩形List
    var cropRectMovingList = mutableSetOf<CropWindowHandler>()


    // endregion
    private var scanAnimator: ValueAnimator? = null
    private val bitmapMatrix = Matrix()
    private var showBitmapHeight = 0f
    private var showBitmapWidth = 0f

    /**
     * 控件的宽
     */
    private var viewWidth = 0

    /**
     * 控件的高
     */
    private var viewHeight = 0

    fun setCropMoveGestureListener(listener: CropMoveGestureListener?) {
        mCropMoveGestureListener = listener
    }
    /**
     * Get the left/top/right/bottom coordinates of the crop window.
     */
    /**
     * Set the left/top/right/bottom coordinates of the crop window.
     */
    var cropWindowRect: RectF
        get() = mCropWindowHandler.rect
        set(rect) {
            mCropWindowHandler.rect = rect
        }
    val cropTouchRect: RectF
        get() {
            val cropWindowRect = cropWindowRect
            HOLDER[cropWindowRect.left - mTouchRadius, cropWindowRect.top - mTouchRadius, cropWindowRect.right + mTouchRadius] =
                cropWindowRect.bottom + mTouchRadius
            return HOLDER
        }

    /**
     * Fix the current crop window rectangle if it is outside of cropping image or view bounds.
     */
    fun fixCurrentCropWindowRect() {
        val rect = cropWindowRect
        fixCropWindowRectByRules(rect)
        mCropWindowHandler.rect = rect
    }

    /**
     * Informs the CropOverlayView of the image's position relative to the ImageView. This is
     * necessary to call in order to draw the crop window.
     *
     * @param boundsPoints the image's bounding points
     * @param viewWidth    The bounding image view width.
     * @param viewHeight   The bounding image view height.
     */
    fun setBounds(boundsPoints: FloatArray?, viewWidth: Int, viewHeight: Int) {
        if (boundsPoints == null || !Arrays.equals(mBoundsPoints, boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(mBoundsPoints, 0f)
            } else {
                System.arraycopy(boundsPoints, 0, mBoundsPoints, 0, boundsPoints.size)
            }
            mViewWidth = viewWidth
            mViewWidth = viewWidth
            mViewHeight = viewHeight
            val cropRect = mCropWindowHandler.rect
            if (cropRect.width() == 0f || cropRect.height() == 0f) {
                initCropWindow()
            }
        }
    }

    /**
     * Resets the crop overlay view.
     */
    fun resetCropOverlayView() {
        if (initializedCropWindow) {
            cropWindowRect = BitmapUtils.EMPTY_RECT_F
            initCropWindow()
            invalidate()
        }
    }

    /**
     * the X value of the aspect ratio;
     */
    /**
     * Sets the X value of the aspect ratio; is defaulted to 1.
     */
    var aspectRatioX: Int
        get() = mAspectRatioX
        set(aspectRatioX) {
            require(aspectRatioX > 0) { "Cannot set aspect ratio value to a number less than or equal to 0." }
            if (mAspectRatioX != aspectRatioX) {
                mAspectRatioX = aspectRatioX
                mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY
                if (initializedCropWindow) {
                    initCropWindow()
                    invalidate()
                }
            }
        }
    /**
     * the Y value of the aspect ratio;
     */
    /**
     * Sets the Y value of the aspect ratio; is defaulted to 1.
     *
     */
    var aspectRatioY: Int
        get() = mAspectRatioY
        set(aspectRatioY) {
            require(aspectRatioY > 0) { "Cannot set aspect ratio value to a number less than or equal to 0." }
            if (mAspectRatioY != aspectRatioY) {
                mAspectRatioY = aspectRatioY
                mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY
                if (initializedCropWindow) {
                    initCropWindow()
                    invalidate()
                }
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
        mCropWindowHandler.setCropWindowLimits(
            maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight
        )
    }
    /**
     * Get crop window initial rectangle.
     */
    /**
     * Set crop window initial rectangle to be used instead of default.
     */
    var initialCropWindowRect: Rect?
        get() = mInitialCropWindowRect
        set(rect) {
            mInitialCropWindowRect.set(rect ?: BitmapUtils.EMPTY_RECT)
            if (initializedCropWindow) {
                initCropWindow()
                invalidate()
            }
        }


    /**
     * Sets all initial values, but does not call initCropWindow to reset the views.<br></br>
     * Used once at the very start to initialize the attributes.
     */
    fun setInitialAttributeValues(options: CropImageOptions) {
        mCropWindowHandler.setInitialAttributeValues(options)
        setSnapRadius(options.snapRadius)
        mTouchRadius = options.touchRadius
        touchBorderRadius = options.touchBorderRadius
        minitialHorizonCropWindowPaddingRatio = options.initialHorizonCropWindowPaddingRatio
        minitialVerticalCropWindowPaddingRatio = options.initialVerticalCropWindowPaddingRatio
        mBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.unSelectedBorderLineColor)
        mBackgroundPaint = getNewPaint(options.backgroundColor)
    }

    /**
     * Set the initial crop window size and position. This is dependent on the size and position of
     * the image being cropped.
     */
    private fun initCropWindow() {
        scanPaint.style = Paint.Style.FILL
        val leftLimit = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0f)
        val topLimit = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0f)
        val rightLimit = Math.min(BitmapUtils.getRectRight(mBoundsPoints), width.toFloat())
        val bottomLimit = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), height.toFloat())
        if (rightLimit <= leftLimit || bottomLimit <= topLimit) {
            return
        }
        val rect = RectF()

        // Tells the attribute functions the crop window has already been initialized
        initializedCropWindow = true
        val horizontalPadding = minitialHorizonCropWindowPaddingRatio * (rightLimit - leftLimit)
        val verticalPadding = minitialVerticalCropWindowPaddingRatio * (bottomLimit - topLimit)
        if (mInitialCropWindowRect.width() > 0 && mInitialCropWindowRect.height() > 0) {
            // Get crop window position relative to the displayed image.
            rect.left =
                leftLimit + mInitialCropWindowRect.left / mCropWindowHandler.scaleFactorWidth
            rect.top = topLimit + mInitialCropWindowRect.top / mCropWindowHandler.scaleFactorHeight
            rect.right =
                rect.left + mInitialCropWindowRect.width() / mCropWindowHandler.scaleFactorWidth
            rect.bottom =
                rect.top + mInitialCropWindowRect.height() / mCropWindowHandler.scaleFactorHeight

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
        fixCropWindowRectByRules(rect)
        mCropWindowHandler.rect = rect
    }

    /**
     * Fix the given rect to fit into bitmap rect and follow min, max and aspect ratio rules.
     */
    private fun fixCropWindowRectByRules(rect: RectF) {
        if (rect.width() < mCropWindowHandler.fixedMinCropWidth) {
            val adj = (mCropWindowHandler.fixedMinCropWidth - rect.width()) / 2
            rect.left -= adj
            rect.right += adj
        }
        if (rect.height() < mCropWindowHandler.fixedMinCropHeight) {
            val adj = (mCropWindowHandler.fixedMinCropHeight - rect.height()) / 2
            rect.top -= adj
            rect.bottom += adj
        }
        if (rect.width() > mCropWindowHandler.fixedMaxCropWidth) {
            val adj = (rect.width() - mCropWindowHandler.fixedMaxCropWidth) / 2
            rect.left += adj
            rect.right -= adj
        }
        if (rect.height() > mCropWindowHandler.fixedMaxCropHeight) {
            val adj = (rect.height() - mCropWindowHandler.fixedMaxCropHeight) / 2
            rect.top += adj
            rect.bottom -= adj
        }
        calculateBounds(rect)
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
        drawCorners(canvas)

        if (scanAnimator != null && scanAnimator!!.isRunning) {
            drawScan(canvas)
        }
    }

    fun getAdjustCropWindow(): List<Int> {
        val list = mutableListOf<Int>()
        if (cropRectMovingList.isNotEmpty()) {
            list.add(0)
        }
        return list
    }

    fun getRadioRect(): List<RectAndIndex> {
        val res = mutableListOf<RectAndIndex>()
        val left = BitmapUtils.getRectLeft(mBoundsPoints).coerceAtLeast(0f)
        val top = BitmapUtils.getRectTop(mBoundsPoints).coerceAtLeast(0f)
        val right = BitmapUtils.getRectRight(mBoundsPoints).coerceAtMost(width.toFloat())
        val bottom = BitmapUtils.getRectBottom(mBoundsPoints).coerceAtMost(height.toFloat())
        mCropWindowHandler.let {
            val topRadio = (it.rect.top - top) / (bottom - top)
            val bottomRadio = (it.rect.bottom - top) / (bottom - top)
            val leftRadio = (it.rect.left - left) / (right - left)
            val rightRadio = (it.rect.right - left) / (right - left)
            val rectF = RectF(leftRadio, topRadio, rightRadio, bottomRadio)
            res.add(RectAndIndex(rectF, 0))
        }
        return res
    }

    private fun drawScan(canvas: Canvas) {
        if (scanBitmap != null) {
            canvas.save()
            val rect = mCropWindowHandler.rect
            canvas.clipRect(rect.left, rect.top, rect.right, rect.bottom) //只保留这部分rect
            canvas.drawBitmap(scanBitmap!!, bitmapMatrix, scanPaint)
            canvas.restore()
        }
    }

    fun startScanAnim(rotateDegree: Int) {
        scanBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.cropper_scan_view
        )
        val rect = mCropWindowHandler.rect
        if (rotateDegree == 90 || rotateDegree == -90) {
            viewWidth = rect.height().toInt()
            viewHeight = rect.width().toInt()
        } else {
            viewWidth = rect.width().toInt()
            viewHeight = rect.height().toInt()
        }
        if (scanBitmap != null) {
            val bitmapWidth: Int = scanBitmap!!.width
            val bitmapHeight: Int = scanBitmap!!.height
            scaleW = viewWidth * 1.0f / bitmapWidth
            scaleH = scaleW
            showBitmapHeight = bitmapHeight * scaleW
            showBitmapWidth = bitmapWidth * scaleW
        }
        if (scanAnimator != null) {
            if (scanAnimator!!.isRunning) {
                scanAnimator!!.cancel()
            }
        }
        scanAnimator = if (rotateDegree == 90) {
            ValueAnimator.ofFloat(rect.width(), -showBitmapHeight)
        } else if (rotateDegree == -90) {
            ValueAnimator.ofFloat(-showBitmapHeight, rect.width())
        } else {
            ValueAnimator.ofFloat(-showBitmapHeight, rect.height())
        }
        scanAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val rect = mCropWindowHandler.rect
            bitmapMatrix.setScale(scaleW, scaleH)
            if (rotateDegree == 90) {
                bitmapMatrix.postRotate(90f, 0f, 0f)
                bitmapMatrix.postTranslate(
                    rect.left + showBitmapHeight,
                    rect.centerY() - showBitmapWidth / 2
                )
                bitmapMatrix.postTranslate(value, 0f)
            } else if (rotateDegree == -90) {
                bitmapMatrix.postRotate(-90f, 0f, 0f)
                bitmapMatrix.postTranslate(rect.left, rect.bottom)
                bitmapMatrix.postTranslate(value, 0f)
            } else {
                bitmapMatrix.postTranslate(rect.centerX() - showBitmapWidth / 2, rect.top)
                bitmapMatrix.postTranslate(0f, value)
            }
            scanAnimator?.duration = 2000
            scanAnimator?.repeatCount = -1
            invalidate()
        })
        scanAnimator?.start()
    }

    fun stopScanView() {
        reset()
        if (scanAnimator != null) {
            if (scanAnimator!!.isRunning) {
                scanAnimator!!.removeAllUpdateListeners()
                scanAnimator!!.removeAllListeners()
                scanAnimator!!.cancel()
                scanAnimator = null
                invalidate()
            }
        }
    }

    private fun reset() {
        bitmapMatrix.reset()
    }

    /**
     * Draw shadow background over the image not including the crop area.
     */
    private fun drawBackground(canvas: Canvas) {
        val rect = mCropWindowHandler.rect
        val left = BitmapUtils.getRectLeft(mBoundsPoints).coerceAtLeast(0f)
        val top = BitmapUtils.getRectTop(mBoundsPoints).coerceAtLeast(0f)
        val right = BitmapUtils.getRectRight(mBoundsPoints).coerceAtMost(width.toFloat())
        val bottom = BitmapUtils.getRectBottom(mBoundsPoints).coerceAtMost(height.toFloat())
        canvas.save()
        canvas.clipRect(rect, Region.Op.DIFFERENCE)
        canvas.drawRect(left, top, right, bottom, mBackgroundPaint!!)
        canvas.restore()
    }

    /**
     * Draw borders of the crop area.
     */
    private fun drawBorders(canvas: Canvas) {
        if (mBorderPaint != null) {
            val w = mBorderPaint!!.strokeWidth
            val rect = mCropWindowHandler.rect
            rect.inset(w / 2, w / 2)
            // Draw rectangle crop window border.
            canvas.drawRect(rect, mBorderPaint!!)

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
    private fun drawCorners(canvas: Canvas) {
        val lineThickness = 4.dpToPxFloat
        val rect = mCropWindowHandler.rect

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
        mMoveHandler =
            mCropWindowHandler.getMoveHandler(x, y, mTouchRadius, touchBorderRadius)
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
        if (mMoveHandler != null) {
            var snapRadius = mSnapRadius
            val rect = mCropWindowHandler.rect
            if (calculateBounds(rect)) {
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
                mTargetAspectRatio
            )
            mCropWindowHandler.rect = rect
            cropRectMovingList.add(mCropWindowHandler)
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
    private fun calculateBounds(rect: RectF): Boolean {
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

    // region: Inner class: CropMoveGestureListener
    interface CropMoveGestureListener {
        fun onCropMoveGestureFinish(moveType: CropWindowMoveHandler.Type?)
    }
    // endregion
    // region: Inner class: ScaleListener


    companion object {
        // region: Fields and Consts
        private val HOLDER = RectF()

        /**
         * Creates the Paint object for drawing.
         */
        private fun getNewPaint(color: Int): Paint {
            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = color
            return paint
        }

        /**
         * Creates the Paint object for given thickness and color, if thickness < 0 return null.
         */
        private fun getNewPaintOrNull(thickness: Float, color: Int): Paint? {
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