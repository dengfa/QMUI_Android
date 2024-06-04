// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"
package com.qmuiteam.qmui.widget.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.qmuiteam.qmui.widget.crop.CropOverlayView.CropMoveGestureListener
import com.miracle.photo.crop.ICropImageView
import com.miracle.photo.crop.RectAndIndex
import com.qmuiteam.qmui.R
import com.qmuiteam.qmui.widget.crop.photoView.PhotoView2

open class MultiCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    FrameLayout(context, attrs), ICropImageView {
    /**
     * Image view widget used to show the image for cropping.
     */
    private val mImageView: ImageView

    /**
     * Overlay over the image view to show cropping UI.
     */
    val cropOverlayView: MultiCropOverlayView

    lateinit var multi_scan_sv_search: ScanView

    override fun getOverlayView(): View {
        return cropOverlayView
    }

    /**
     * The matrix used to transform the cropping image in the image view
     */
    private val mImageMatrix = Matrix()

    /**
     * Rectangle used in image matrix transformation calculation (reusing rect instance)
     */
    private val mImagePoints = FloatArray(8)

    /**
     * Reusing matrix instance for reverse matrix calculations.
     */
    private val mImageInverseMatrix = Matrix()

    /**
     * Rectangle used in image matrix transformation for scale calculation (reusing rect instance)
     */
    private val mScaleImagePoints = FloatArray(8)

    private var mShowCropOverlay = true
    private var showBitmap: Bitmap? = null
    private var needLayout = false


    /**
     * How much the image is rotated from original clockwise
     */
    private var mLayoutWidth = 0
    private var mLayoutHeight = 0


    private val cropMatrix = Matrix()

    // mImageView 为 PhotoView 时，不支持 MATRIX 类型的 ScaleType
    protected open val forbiddenMatrixScaleType = false

    init {
        val options = CropImageOptions()
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0)
            try {
                options.scaleType = CropImageView.ScaleType.values()[ta.getInt(
                    R.styleable.CropImageView_cropScaleType,
                    options.scaleType.ordinal
                )]
                options.snapRadius =
                    ta.getDimension(R.styleable.CropImageView_cropSnapRadius, options.snapRadius)
                options.touchRadius =
                    ta.getDimension(R.styleable.CropImageView_cropTouchRadius, options.touchRadius)
                options.touchBorderRadius = ta.getDimension(
                    R.styleable.CropImageView_cropTouchBorderRadius,
                    options.touchBorderRadius
                )
                options.initialHorizonCropWindowPaddingRatio = ta.getFloat(
                    R.styleable.CropImageView_cropInitialHorizonCropWindowPaddingRatio,
                    options.initialHorizonCropWindowPaddingRatio
                )
                options.initialVerticalCropWindowPaddingRatio = ta.getFloat(
                    R.styleable.CropImageView_cropInitialVerticalCropWindowPaddingRatio,
                    options.initialVerticalCropWindowPaddingRatio
                )
                options.borderLineThickness = ta.getDimension(
                    R.styleable.CropImageView_cropBorderLineThickness, options.borderLineThickness
                )
                options.unSelectedBorderLineColor = ta.getInteger(
                    R.styleable.CropImageView_unSelectedCropBorderColor,
                    options.unSelectedBorderLineColor
                )
                options.selectedBorderLineColor = ta.getInteger(
                    R.styleable.CropImageView_selectedCropBorderColor,
                    options.selectedBorderLineColor
                )
                options.unSelectedBorderSolidColor = ta.getInteger(
                    R.styleable.CropImageView_unSelectedCropBorderSolidColor,
                    options.unSelectedBorderSolidColor
                )
                options.backgroundColor = ta.getInteger(
                    R.styleable.CropImageView_cropBackgroundColor,
                    options.backgroundColor
                )
                options.minCropWindowWidth = ta.getDimension(
                    R.styleable.CropImageView_cropMinCropWindowWidth,
                    options.minCropWindowWidth.toFloat()
                ).toInt()
                options.minCropWindowHeight = ta.getDimension(
                    R.styleable.CropImageView_cropMinCropWindowHeight,
                    options.minCropWindowHeight.toFloat()
                ).toInt()
                options.minCropResultWidth = ta.getFloat(
                    R.styleable.CropImageView_cropMinCropResultWidthPX,
                    options.minCropResultWidth.toFloat()
                ).toInt()
                options.minCropResultHeight = ta.getFloat(
                    R.styleable.CropImageView_cropMinCropResultHeightPX,
                    options.minCropResultHeight.toFloat()
                ).toInt()
                options.maxCropResultWidth = ta.getFloat(
                    R.styleable.CropImageView_cropMaxCropResultWidthPX,
                    options.maxCropResultWidth.toFloat()
                ).toInt()
                options.maxCropResultHeight = ta.getFloat(
                    R.styleable.CropImageView_cropMaxCropResultHeightPX,
                    options.maxCropResultHeight.toFloat()
                ).toInt()

            } finally {
                ta.recycle()
            }
        }
        options.validate()
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(getLayoutResId(), this, true)
        mImageView = v.findViewById(R.id.multi_image)
        multi_scan_sv_search = v.findViewById(R.id.multi_scan_sv_search)
        mImageView.postDelayed({
            if (!forbiddenMatrixScaleType) {
                mImageView.scaleType = ImageView.ScaleType.MATRIX
            }
        }, 1000L)
        cropOverlayView = v.findViewById(R.id.CropOverlayView)
        cropOverlayView.setInitialAttributeValues(options)
    }

    open fun getLayoutResId():Int{
        return R.layout.muti_crop_cropper_image_view
    }

    override fun setShowCropOverlay(showCropOverlay: Boolean) {
        if (mShowCropOverlay != showCropOverlay) {
            mShowCropOverlay = showCropOverlay
            setCropOverlayVisibility()
        }
    }

    override fun getRadioRect(): List<RectAndIndex> {
        return cropOverlayView.getRadioRect()
    }

    override fun getAdjustCropWindow(): List<Int> {
        return cropOverlayView.getAdjustCropWindow()
    }


    override fun setSelectRect(index: Int) {
        cropOverlayView.setSelectedRectIndex(index)
    }

    open fun setSelectRectNoCallback(index: Int) {
        cropOverlayView.setSelectedRectIndexNoCallback(index)
    }

    override fun setCropMoveGestureListener(listener: CropMoveGestureListener?) {
        cropOverlayView.setCropMoveGestureListener(listener)
    }

    override var rotatedDegrees: Int = 0

    override val croppedImage: Bitmap? = null

    override fun startScanAnim(rotateDegree: Int) {
        startAnim(rotateDegree)
    }

    override fun stopScanView() {
        multi_scan_sv_search?.gone()
        multi_scan_sv_search?.stopScanAnimAndReset()
    }

    override fun setCropRect(rect: List<Rect>) {
        cropOverlayView.setInitCropRect(rect)
    }

    override val cropTouchRect: RectF = RectF()

    /**
     * Sets a Bitmap as the content of the CropImageView.
     *
     * @param bitmap the Bitmap to set
     */
    override fun setImageBitmap(bitmap: Bitmap?) {
        if (showBitmap == null || showBitmap != bitmap) {
            nonPhotoImageView()?.clearAnimation()
            clearImageInt()
            needLayout = true
            showBitmap = bitmap
            mImageView.setImageBitmap(showBitmap)
            applyImageMatrix(width.toFloat(), height.toFloat())
            cropOverlayView.resetCropOverlayView()
            setCropOverlayVisibility()
        }
    }

    private fun nonPhotoImageView(): ImageView? {
        return mImageView.takeIf { (it is PhotoView2).not() }
    }

    private fun startAnim(degree: Int) {
        multi_scan_sv_search?.setOrientation(0f)
        multi_scan_sv_search?.startScanAnim()
        multi_scan_sv_search?.visible()
    }

    // region: Private methods
    /**
     * Clear the current image set for cropping.<br></br>
     * Full clear will also clear the data of the set image like Uri or Resource id while partial
     * clear will only clear the bitmap and recycle if required.
     */
    protected fun clearImageInt() {

        // if we allocated the bitmap, release it as fast as possible
        if (showBitmap != null) {
            showBitmap!!.recycle()
        }
        showBitmap = null
        mImageMatrix.reset()
        nonPhotoImageView()?.setImageBitmap(null)
        setCropOverlayVisibility()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (showBitmap != null) {

            // Bypasses a baffling bug when used within a ScrollView, where heightSize is set to 0.
            if (heightSize == 0) {
                heightSize = showBitmap!!.height
            }
            val desiredWidth: Int
            val desiredHeight: Int
            var viewToBitmapWidthRatio = Double.POSITIVE_INFINITY
            var viewToBitmapHeightRatio = Double.POSITIVE_INFINITY

            // Checks if either width or height needs to be fixed
            if (widthSize < showBitmap!!.width) {
                viewToBitmapWidthRatio = widthSize.toDouble() / showBitmap!!.width.toDouble()
            }
            if (heightSize < showBitmap!!.height) {
                viewToBitmapHeightRatio = heightSize.toDouble() / showBitmap!!.height.toDouble()
            }

            // If either needs to be fixed, choose smallest ratio and calculate from there
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY
                || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY
            ) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize
                    desiredHeight = (showBitmap!!.height * viewToBitmapWidthRatio).toInt()
                } else {
                    desiredHeight = heightSize
                    desiredWidth = (showBitmap!!.width * viewToBitmapHeightRatio).toInt()
                }
            } else {
                // Otherwise, the picture is within frame layout bounds. Desired width is simply picture
                // size
                desiredWidth = showBitmap!!.width
                desiredHeight = showBitmap!!.height
            }
            val width = getOnMeasureSpec(widthMode, widthSize, desiredWidth)
            val height = getOnMeasureSpec(heightMode, heightSize, desiredHeight)
            mLayoutWidth = width
            mLayoutHeight = height
            setMeasuredDimension(mLayoutWidth, mLayoutHeight)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            // Gets original parameters, and creates the new parameters
            val origParams = this.layoutParams
            origParams.width = mLayoutWidth
            origParams.height = mLayoutHeight
            layoutParams = origParams
            if (showBitmap != null) {
                if (needLayout) {
                    applyImageMatrix((r - l).toFloat(), (b - t).toFloat())
                    needLayout = false
                }
            }
        }
    }

    /**
     * Apply matrix to handle the image inside the image view.
     * 先放大
     *
     * @param width  the width of the image view
     * @param height the height of the image view
     */
    private fun applyImageMatrix(width: Float, height: Float) {
        if (showBitmap != null && width > 0 && height > 0) {
            mImageMatrix.invert(mImageInverseMatrix)
            mImageMatrix.reset()

            // move the image to the center of the image view first so we can manipulate it from there
            mImageMatrix.postTranslate(
                (width - showBitmap!!.width) / 2, (height - showBitmap!!.height) / 2
            )
            mapImagePointsByImageMatrix(true)

            // scale the image to the image view, image rect transformed to know new width/height
            val scale = Math.min(
                width / BitmapUtils.getRectWidth(mImagePoints),
                height / BitmapUtils.getRectHeight(mImagePoints)
            )

            mImageMatrix.postScale(
                scale,
                scale,
                BitmapUtils.getRectCenterX(mImagePoints),
                BitmapUtils.getRectCenterY(mImagePoints)
            )
            mapImagePointsByImageMatrix(true)

            cropMatrix.reset()
            mImageMatrix.invert(cropMatrix)
            mapImagePointsByImageMatrix(false)
            mapImagePointsByImageMatrix(false)
            nonPhotoImageView()?.imageMatrix = mImageMatrix

            // update the image rectangle in the crop overlay
            updateImageBounds(false)
        }
    }

    /**
     * Adjust the given image rectangle by image transformation matrix to know the final rectangle of
     * the image.<br></br>
     * To get the proper rectangle it must be first reset to original image rectangle.
     *
     *
     * zoom or scroll don't consider mapScale
     */
    protected fun mapImagePointsByImageMatrix(mapScale: Boolean) {
        if (showBitmap == null) return
        mImagePoints[0] = 0f
        mImagePoints[1] = 0f
        mImagePoints[2] = showBitmap!!.width.toFloat()
        mImagePoints[3] = 0f
        mImagePoints[4] = showBitmap!!.width.toFloat()
        mImagePoints[5] = showBitmap!!.height.toFloat()
        mImagePoints[6] = 0f
        mImagePoints[7] = showBitmap!!.height.toFloat()
        mImageMatrix.mapPoints(mImagePoints)
        if (mapScale) {
            mScaleImagePoints[0] = 0f
            mScaleImagePoints[1] = 0f
            mScaleImagePoints[2] = 100f
            mScaleImagePoints[3] = 0f
            mScaleImagePoints[4] = 100f
            mScaleImagePoints[5] = 100f
            mScaleImagePoints[6] = 0f
            mScaleImagePoints[7] = 100f
            mImageMatrix.mapPoints(mScaleImagePoints)
        }
    }

    /**
     * Set visibility of crop overlay to hide it when there is no image or specificly set by client.
     */
    private fun setCropOverlayVisibility() {
        cropOverlayView.visibility =
            if (mShowCropOverlay && showBitmap != null) VISIBLE else INVISIBLE
    }

    /**
     * Update the scale factor between the actual image bitmap and the shown image.<br></br>
     */
    protected fun updateImageBounds(clear: Boolean) {
        if (showBitmap != null && !clear) {

            // Get the scale factor between the actual Bitmap dimensions and the displayed dimensions for
            // width/height.
            val scaleFactorWidth =
                100f / BitmapUtils.getRectWidth(mScaleImagePoints)
            val scaleFactorHeight =
                100f / BitmapUtils.getRectHeight(mScaleImagePoints)
            cropOverlayView.setCropWindowLimits(
                width.toFloat(), height.toFloat(), scaleFactorWidth, scaleFactorHeight
            )
        }

        // set the bitmap rectangle and update the crop window after scale factor is set
        cropOverlayView.setBounds(if (clear) null else mImagePoints, width, height)
    }

    fun setMovable(b: Boolean) {
        cropOverlayView.movableMode = b
    }
    // endregion
    // region: Inner class: ScaleType


    companion object {
        /**
         * Determines the specs for the onMeasure function. Calculates the width or height depending on
         * the mode.
         *
         * @param measureSpecMode The mode of the measured width or height.
         * @param measureSpecSize The size of the measured width or height.
         * @param desiredSize     The desired size of the measured width or height.
         * @return The final size of the width or height.
         */
        private fun getOnMeasureSpec(
            measureSpecMode: Int,
            measureSpecSize: Int,
            desiredSize: Int
        ): Int {

            // Measure Width
            val spec: Int
            spec = if (measureSpecMode == MeasureSpec.EXACTLY) {
                // Must be this size
                measureSpecSize
            } else if (measureSpecMode == MeasureSpec.AT_MOST) {
                // Can't be bigger than...; match_parent value
                Math.min(desiredSize, measureSpecSize)
            } else {
                // Be whatever you want; wrap_content
                desiredSize
            }
            return spec
        }
    }
}