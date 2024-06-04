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
import com.miracle.photo.crop.CropUtils.rotateDegree
import com.miracle.photo.crop.ICropImageView
import com.miracle.photo.crop.RectAndIndex
import com.qmuiteam.qmui.R

class CropImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), ICropImageView {
    // region: Fields and Consts
    /**
     * Image view widget used to show the image for cropping.
     */
    val mImageView: ImageView

    /**
     * Overlay over the image view to show cropping UI.
     */
    val cropOverlayView: CropOverlayView

    /**
     * The matrix used to transform the cropping image in the image view
     */
    protected val mImageMatrix = Matrix()

    /**
     * Rectangle used in image matrix transformation calculation (reusing rect instance)
     */
    protected val mImagePoints = FloatArray(8)

    /**
     * Reusing matrix instance for reverse matrix calculations.
     */
    private val mImageInverseMatrix = Matrix()

    /**
     * Rectangle used in image matrix transformation for scale calculation (reusing rect instance)
     */
    private val mScaleImagePoints = FloatArray(8)

    private var mShowCropOverlay = true
    var showBitmap: Bitmap? = null
        private set
    private var needLayout = false


    /**
     * How much the image is rotated from original clockwise
     */
    private var mDegreesRotated = 0
    private var mLayoutWidth = 0
    private var mLayoutHeight = 0
    /**
     * Get the scale type of the image in the crop view.
     */
    /**
     * The initial scale type of the image in the crop image view
     */
    val scaleType: ScaleType

    // endregion
    private var mInitialWidth = 0
    private var mInitialHeight = 0
    private val cropMatrix = Matrix()

    init {
        val options = CropImageOptions()
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0)
            try {
                options.scaleType = ScaleType.values()[ta.getInt(
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
        scaleType = options.scaleType
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.crop_cropper_image_view, this, true)
        mImageView = v.findViewById(R.id.single_image)
        mImageView.postDelayed({ mImageView.scaleType = ImageView.ScaleType.MATRIX }, 1000L)
        cropOverlayView = v.findViewById(R.id.CropOverlayView)
        cropOverlayView.setInitialAttributeValues(options)
    }

    override fun setShowCropOverlay(showCropOverlay: Boolean) {
        if (mShowCropOverlay != showCropOverlay) {
            mShowCropOverlay = showCropOverlay
            setCropOverlayVisibility()
        }
    }


    override fun setCropMoveGestureListener(listener: CropMoveGestureListener?) {
        cropOverlayView.setCropMoveGestureListener(listener)
    }

    override var rotatedDegrees: Int
        /**
         * Get the amount of degrees the cropping image is rotated cloackwise.<br></br>
         *
         * @return 0-360
         */
        get() = mDegreesRotated
        /**
         * Set the amount of degrees the cropping image is rotated cloackwise.<br></br>
         *
         * @param degrees 0-360
         */
        set(degrees) {
            if (mDegreesRotated != degrees) {
                rotateImage(degrees - mDegreesRotated)
            }
        }

    override fun startScanAnim(rotateDegree: Int) {
        cropOverlayView.startScanAnim(rotateDegree)
    }

    override fun stopScanView() {
        cropOverlayView.stopScanView()
    }

    override fun setCropRect(rect: List<Rect>) {
        cropOverlayView.initialCropWindowRect = rect.firstOrNull()
    }

    override fun getAdjustCropWindow(): List<Int> {
       return cropOverlayView.getAdjustCropWindow()
    }

    override fun getRadioRect(): List<RectAndIndex> {
        return cropOverlayView.getRadioRect()
    }

    override val cropTouchRect: RectF
        /**
         * @return 裁剪框+热区的rect，相对于View
         */
        get() = cropOverlayView.cropTouchRect
    val cropPoints: FloatArray
        /**
         * Gets the 4 points of crop window's position relative to the source Bitmap (not the image
         * displayed in the CropImageView) using the original image rotation.<br></br>
         * Note: the 4 points may not be a rectangle if the image was rotates to NOT stright angle (!=
         * 90/180/270).
         *
         * @return 4 points (x0,y0,x1,y1,x2,y2,x3,y3) of cropped area boundaries
         */
        get() {

            // Get crop window position relative to the displayed image.
            val cropWindowRect = cropOverlayView.cropWindowRect
            val points = floatArrayOf(
                cropWindowRect.left,
                cropWindowRect.top,
                cropWindowRect.right,
                cropWindowRect.top,
                cropWindowRect.right,
                cropWindowRect.bottom,
                cropWindowRect.left,
                cropWindowRect.bottom
            )
            mImageMatrix.invert(mImageInverseMatrix)
            mImageInverseMatrix.mapPoints(points)
            return points
        }

    override val croppedImage: Bitmap?
        /**
         * Gets the cropped image based on the current crop window.
         *
         * @return a new Bitmap representing the cropped image
         */
        get() = getCroppedImage(0, 0, RequestSizeOptions.NONE)

    /**
     * Gets the cropped image based on the current crop window.<br></br>
     *
     * @param reqWidth  the width to resize the cropped image to (see options)
     * @param reqHeight the height to resize the cropped image to (see options)
     * @param options   the resize method to use, see its documentation
     * @return a new Bitmap representing the cropped image
     */
    private fun getCroppedImage(
        reqWidth: Int,
        reqHeight: Int,
        options: RequestSizeOptions
    ): Bitmap? {
        var reqWidth = reqWidth
        var reqHeight = reqHeight
        var croppedBitmap: Bitmap? = null
        if (showBitmap != null) {
            mImageView.post { mImageView.clearAnimation() }
            reqWidth = if (options != RequestSizeOptions.NONE) reqWidth else 0
            reqHeight = if (options != RequestSizeOptions.NONE) reqHeight else 0
            croppedBitmap = BitmapUtils.cropBitmapObjectHandleOOM(
                showBitmap!!,
                cropPoints,
                mDegreesRotated,
                false,
                cropOverlayView.aspectRatioX,
                cropOverlayView.aspectRatioY
            ).bitmap

            croppedBitmap = BitmapUtils.resizeBitmap(croppedBitmap, reqWidth, reqHeight, options)
        }
        return croppedBitmap
    }

    /**
     * Sets a Bitmap as the content of the CropImageView.
     *
     * @param bitmap the Bitmap to set
     */
    override fun setImageBitmap(bitmap: Bitmap?) {
        cropOverlayView.initialCropWindowRect = null
        setBitmap(bitmap, 0)
    }

    override fun getOverlayView(): View {
        return cropOverlayView
    }

    private fun setInnerRotateDegree(degree: Int) {
        mDegreesRotated = degree
        rotateDegree = degree
    }

    /**
     * Rotates image by the specified number of degrees clockwise.<br></br>
     * Negative values represent counter-clockwise rotations.
     *
     * @param degrees Integer specifying the number of degrees to rotate.
     */
    private fun rotateImage(degrees: Int) {
        var degrees = degrees
        if (showBitmap != null) {
            // Force degrees to be a non-zero value between 0 and 360 (inclusive)
            degrees = if (degrees < 0) {
                degrees % 360 + 360
            } else {
                degrees % 360
            }
            val flipAxes = ((degrees in 46..134 || degrees in 216..304))
            BitmapUtils.RECT.set(cropOverlayView.cropWindowRect)
            var halfWidth =
                (if (flipAxes) BitmapUtils.RECT.height() else BitmapUtils.RECT.width()) / 2f
            var halfHeight =
                (if (flipAxes) BitmapUtils.RECT.width() else BitmapUtils.RECT.height()) / 2f
            mImageMatrix.invert(mImageInverseMatrix)
            BitmapUtils.POINTS[0] = BitmapUtils.RECT.centerX()
            BitmapUtils.POINTS[1] = BitmapUtils.RECT.centerY()
            BitmapUtils.POINTS[2] = 0f
            BitmapUtils.POINTS[3] = 0f
            BitmapUtils.POINTS[4] = 1f
            BitmapUtils.POINTS[5] = 0f
            mImageInverseMatrix.mapPoints(BitmapUtils.POINTS)

            // This is valid because degrees is not negative.
            setInnerRotateDegree((mDegreesRotated + degrees) % 360)
            applyImageMatrix(width.toFloat(), height.toFloat())
            mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS)
            applyImageMatrix(width.toFloat(), height.toFloat())
            mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS)

            // adjust the width/height by the changes in scaling to the image
            val change = Math.sqrt(
                Math.pow((BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]).toDouble(), 2.0)
                        + Math.pow(
                    (BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]).toDouble(),
                    2.0
                )
            )
            halfWidth *= change.toFloat()
            halfHeight *= change.toFloat()

            // calculate the new crop window rectangle to center in the same location and have proper
            // width/height
            BitmapUtils.RECT[BitmapUtils.POINTS2[0] - halfWidth, BitmapUtils.POINTS2[1] - halfHeight, BitmapUtils.POINTS2[0] + halfWidth] =
                BitmapUtils.POINTS2[1] + halfHeight
            cropOverlayView.resetCropOverlayView()
            cropOverlayView.cropWindowRect = BitmapUtils.RECT
            applyImageMatrix(width.toFloat(), height.toFloat())

            // make sure the crop window rectangle is within the cropping image bounds after all the
            // changes
            cropOverlayView.fixCurrentCropWindowRect()
        }
    }
    // region: Private methods
    /**
     * Set the given bitmap to be used in for cropping<br></br>
     * Optionally clear full if the bitmap is new, or partial clear if the bitmap has been
     * manipulated.
     */
    private fun setBitmap(
        bitmap: Bitmap?, degreesRotated: Int
    ) {
        if (showBitmap == null || showBitmap != bitmap) {
            mImageView.clearAnimation()
            clearImageInt()
            needLayout = true
            showBitmap = bitmap
            mImageView.setImageBitmap(showBitmap)
            setInnerRotateDegree(degreesRotated)
            applyImageMatrix(width.toFloat(), height.toFloat())
            if (cropOverlayView != null) {
                cropOverlayView.resetCropOverlayView()
                setCropOverlayVisibility()
            }
        }
    }

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
        setInnerRotateDegree(0)
        mImageMatrix.reset()
        mImageView.setImageBitmap(null)
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
            } else {
                updateImageBounds(true)
            }
        } else {
            updateImageBounds(true)
        }
    }

    /**
     * Apply matrix to handle the image inside the image view.
     * 先放大
     *
     * @param width  the width of the image view
     * @param height the height of the image view
     */
    protected fun applyImageMatrix(width: Float, height: Float) {
        if (showBitmap != null && width > 0 && height > 0) {
            mImageMatrix.invert(mImageInverseMatrix)
            val cropRect = cropOverlayView!!.cropWindowRect
            cropMatrix.mapRect(cropRect)
            mImageMatrix.reset()

            // move the image to the center of the image view first so we can manipulate it from there
            mImageMatrix.postTranslate(
                (width - showBitmap!!.width) / 2, (height - showBitmap!!.height) / 2
            )
            mapImagePointsByImageMatrix(true)

            // rotate the image the required degrees from center of image
            if (mDegreesRotated > 0) {
                mImageMatrix.postRotate(
                    mDegreesRotated.toFloat(),
                    BitmapUtils.getRectCenterX(mImagePoints),
                    BitmapUtils.getRectCenterY(mImagePoints)
                )
                mapImagePointsByImageMatrix(true)
            }

            // scale the image to the image view, image rect transformed to know new width/height
            var scale = Math.min(
                width / BitmapUtils.getRectWidth(mImagePoints),
                height / BitmapUtils.getRectHeight(mImagePoints)
            )
            if (scaleType == ScaleType.FIT_INITIAL) {
                val widthScale = mInitialWidth / BitmapUtils.getRectWidth(mImagePoints)
                val heightScale = mInitialHeight / BitmapUtils.getRectHeight(mImagePoints)
                scale = if (widthScale < heightScale) { //横长图
                    heightScale
                } else { //竖长图
                    //横贴边
                    widthScale
                }
            }
            if (scaleType == ScaleType.FIT_CENTER || scaleType == ScaleType.CENTER_INSIDE && scale < 1 || scaleType == ScaleType.FIT_INITIAL) {
                mImageMatrix.postScale(
                    scale,
                    scale,
                    BitmapUtils.getRectCenterX(mImagePoints),
                    BitmapUtils.getRectCenterY(mImagePoints)
                )
                mapImagePointsByImageMatrix(true)
            }
            cropMatrix.reset()
            mImageMatrix.invert(cropMatrix)
            mImageMatrix.mapRect(cropRect)
            mapImagePointsByImageMatrix(false)
            cropOverlayView.cropWindowRect = cropRect
            mapImagePointsByImageMatrix(false)
            cropOverlayView.invalidate()
            mImageView.imageMatrix = mImageMatrix

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
        if (cropOverlayView != null) {
            cropOverlayView.visibility =
                if (mShowCropOverlay && showBitmap != null) VISIBLE else INVISIBLE
        }
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
            cropOverlayView!!.setCropWindowLimits(
                width.toFloat(), height.toFloat(), scaleFactorWidth, scaleFactorHeight
            )
        }

        // set the bitmap rectangle and update the crop window after scale factor is set
        cropOverlayView!!.setBounds(if (clear) null else mImagePoints, width, height)
    }
    // endregion
    // region: Inner class: ScaleType
    /**
     * Options for scaling the bounds of cropping image to the bounds of Crop Image View.<br></br>
     * Note: Some options are affected by auto-zoom, if enabled.
     */
    enum class ScaleType {
        /**
         * Scale the image uniformly (maintain the image's aspect ratio) to fit in crop image view.<br></br>
         * The largest dimension will be equals to crop image view and the second dimension will be
         * smaller.
         */
        FIT_CENTER,

        /**
         * Center the image in the view, but perform no scaling.<br></br>
         * Note: If auto-zoom is enabled and the source image is smaller than crop image view then it
         * will be scaled uniformly to fit the crop image view.
         */
        CENTER,

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width
         * and height) of the image will be equal to or **larger** than the corresponding dimension
         * of the view (minus padding).<br></br>
         * The image is then centered in the view.
         */
        CENTER_CROP,

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width
         * and height) of the image will be equal to or **less** than the corresponding dimension of
         * the view (minus padding).<br></br>
         * The image is then centered in the view.<br></br>
         * Note: If auto-zoom is enabled and the source image is smaller than crop image view then it
         * will be scaled uniformly to fit the crop image view.
         */
        CENTER_INSIDE, FIT_INITIAL
    }
    // endregion
    // region: Inner class: RequestSizeOptions
    /**
     * Possible options for handling requested width/height for cropping.
     */
    enum class RequestSizeOptions {
        /**
         * No resize/sampling is done unless required for memory management (OOM).
         */
        NONE,

        /**
         * Resize the image uniformly (maintain the image's aspect ratio) so that both dimensions (width
         * and height) of the image will be equal to or **less** than the corresponding requested
         * dimension.<br></br>
         * If the image is smaller than the requested size it will NOT change.
         */
        RESIZE_INSIDE,

        /**
         * Resize the image uniformly (maintain the image's aspect ratio) to fit in the given
         * width/height.<br></br>
         * The largest dimension will be equals to the requested and the second dimension will be
         * smaller.<br></br>
         * If the image is smaller than the requested size it will enlarge it.
         */
        RESIZE_FIT,

        /**
         * Resize the image to fit exactly in the given width/height.<br></br>
         * This resize method does NOT preserve aspect ratio.<br></br>
         * If the image is smaller than the requested size it will enlarge it.
         */
        RESIZE_EXACT
    }
    // endregion
    // region: Inner class: OnSetImageUriCompleteListener


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