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
package com.miracle.photo.crop

import android.graphics.RectF
import com.miracle.photo.crop.CropUtils.isVertical
import com.qmuiteam.qmui.widget.crop.CropImageOptions
import com.qmuiteam.qmui.widget.crop.dpToPxFloat

/**
 * Handler from crop window stuff, moving and knowing possition.
 */
class CropWindowHandler {
    // region: Fields and Consts
    /**
     * The 4 edges of the crop window defining its coordinates and size
     */
    private val mEdges = RectF()

    /**
     * Rectangle used to return the edges rectangle without ability to change it and without creating
     * new all the time.
     */
    private val mGetEdges = RectF()

    /**
     * Minimum width in pixels that the crop window can get.
     */
    private var mMinCropWindowWidth = 0f

    /**
     * Minimum height in pixels that the crop window can get.
     */
    private var mMinCropWindowHeight = 0f

    /**
     * Maximum width in pixels that the crop window can CURRENTLY get.
     */
    private var mMaxCropWindowWidth = 0f

    /**
     * Maximum height in pixels that the crop window can CURRENTLY get.
     */
    private var mMaxCropWindowHeight = 0f

    /**
     * Minimum width in pixels that the result of cropping an image can get, affects crop window width
     * adjusted by width scale factor.
     */
    private var mMinCropResultWidth = 0f

    /**
     * Minimum height in pixels that the result of cropping an image can get, affects crop window
     * height adjusted by height scale factor.
     */
    private var mMinCropResultHeight = 0f

    /**
     * Maximum width in pixels that the result of cropping an image can get, affects crop window width
     * adjusted by width scale factor.
     */
    private var mMaxCropResultWidth = 0f

    /**
     * Maximum height in pixels that the result of cropping an image can get, affects crop window
     * height adjusted by height scale factor.
     */
    private var mMaxCropResultHeight = 0f
    /**
     * get the scale factor (on width) of the showen image to original image.
     */
    /**
     * The width scale factor of shown image and actual image
     */
    var scaleFactorWidth = 1f
        private set
    /**
     * get the scale factor (on height) of the showen image to original image.
     */
    /**
     * The height scale factor of shown image and actual image
     */
    var scaleFactorHeight = 1f //放的越小越大
        private set
    var rect: RectF
        /**
         * Get the left/top/right/bottom coordinates of the crop window.
         */
        get() {
            mGetEdges.set(mEdges)
            return mGetEdges
        }
        /**
         * Set the left/top/right/bottom coordinates of the crop window.
         */
        set(rect) {
            mEdges.set(rect)
            initTouchRect(mEdges)
        }

    var initRect: RectF? = null

    private var mCornerWidth = 45.dpToPxFloat
    private var handlerWidth = 15.dpToPxFloat
    var topLeftCorner = RectF()
    var topRightCorner = RectF()
    var bottomLeftCorner = RectF()
    var bottomRightCorner = RectF()
    var centerRect = RectF()

    private fun initTouchRect(mEdges: RectF) {
        val cornerWidth = if (mEdges.right - mEdges.left > mCornerWidth * 2) {
            mCornerWidth
        } else if (mEdges.right - mEdges.left > minCropWidth) {
            (mEdges.right - mEdges.left) / 2
        } else {
            minCropWidth / 2
        }
        val cornerHeight = if (mEdges.bottom - mEdges.top > mCornerWidth * 2) {
            mCornerWidth
        } else if (mEdges.bottom - mEdges.top > minCropHeight) {
            (mEdges.bottom - mEdges.top) / 2
        } else {
            minCropHeight / 2
        }
        val handlerLeft = mEdges.left - handlerWidth
        val handlerTop = mEdges.top - handlerWidth
        val handlerRight = mEdges.right + handlerWidth
        val handlerBottom = mEdges.bottom + handlerWidth
        topLeftCorner =
            RectF(handlerLeft, handlerTop, handlerLeft + cornerWidth, handlerTop + cornerHeight)
        topRightCorner =
            RectF(handlerRight - cornerWidth, handlerTop, handlerRight, handlerTop + cornerHeight)
        bottomLeftCorner = RectF(
            handlerLeft,
            handlerBottom - cornerHeight,
            handlerLeft + cornerWidth,
            handlerBottom
        )
        bottomRightCorner = RectF(
            handlerRight - cornerWidth,
            handlerBottom - cornerHeight,
            handlerRight,
            handlerBottom
        )
        centerRect = RectF(
            handlerLeft + minCropWidth / 4,
            handlerTop + minCropHeight / 4,
            handlerRight - minCropWidth / 4,
            handlerBottom - minCropHeight / 4
        )
    }

    val minCropWidth: Float
        /**
         * Minimum width in pixels that the crop window can get.
         */
        get() = Math.max(mMinCropWindowWidth, mMinCropResultWidth / scaleFactorWidth)
    val minCropHeight: Float
        /**
         * Minimum height in pixels that the crop window can get.
         */
        get() = Math.max(mMinCropWindowHeight, mMinCropResultHeight / scaleFactorHeight)
    val maxCropWidth: Float
        /**
         * Maximum width in pixels that the crop window can get.
         */
        get() {
            var width = mMaxCropWindowWidth
            if (isVertical()) {
                width = mMaxCropWindowHeight
            }
            return Math.min(width, mMaxCropResultWidth / scaleFactorWidth)
        }
    val maxCropHeight: Float
        /**
         * Maximum height in pixels that the crop window can get.
         */
        get() {
            var height = mMaxCropWindowHeight
            if (isVertical()) {
                height = mMaxCropResultWidth
            }
            return Math.min(height, mMaxCropResultHeight / scaleFactorHeight)
        }
    val fixedMinCropWidth: Float
        get() = if (isVertical()) {
            minCropHeight
        } else {
            minCropWidth
        }
    val fixedMinCropHeight: Float
        get() = if (isVertical()) {
            minCropWidth
        } else {
            minCropHeight
        }
    val fixedMaxCropWidth: Float
        get() = if (isVertical()) {
            maxCropHeight
        } else {
            maxCropWidth
        }
    val fixedMaxCropHeight: Float
        get() = if (isVertical()) {
            maxCropWidth
        } else {
            maxCropHeight
        }

    /**
     * the min size the resulting cropping image is allowed to be, affects the cropping window limits
     * (in pixels).<br></br>
     */
    fun setMinCropResultSize(minCropResultWidth: Int, minCropResultHeight: Int) {
        mMinCropResultWidth = minCropResultWidth.toFloat()
        mMinCropResultHeight = minCropResultHeight.toFloat()
    }

    /**
     * the max size the resulting cropping image is allowed to be, affects the cropping window limits
     * (in pixels).<br></br>
     */
    fun setMaxCropResultSize(maxCropResultWidth: Int, maxCropResultHeight: Int) {
        mMaxCropResultWidth = maxCropResultWidth.toFloat()
        mMaxCropResultHeight = maxCropResultHeight.toFloat()
    }
    // region: Private methods
    /**
     * set the max width/height and scale factor of the showen image to original image to scale the
     * limits appropriately.
     */
    fun setCropWindowLimits(
        maxWidth: Float, maxHeight: Float, scaleFactorWidth: Float, scaleFactorHeight: Float
    ) {
        mMaxCropWindowWidth = maxWidth
        mMaxCropWindowHeight = maxHeight
        this.scaleFactorWidth = scaleFactorWidth
        this.scaleFactorHeight = scaleFactorHeight
    }

    /**
     * Set the variables to be used during crop window handling.
     */
    fun setInitialAttributeValues(options: CropImageOptions?) {
        if (options == null) {
            return
        }
        mMinCropWindowWidth = options.minCropWindowWidth.toFloat()
        mMinCropWindowHeight = options.minCropWindowHeight.toFloat()
        mMinCropResultWidth = options.minCropResultWidth.toFloat()
        mMinCropResultHeight = options.minCropResultHeight.toFloat()
        mMaxCropResultWidth = options.maxCropResultWidth.toFloat()
        mMaxCropResultHeight = options.maxCropResultHeight.toFloat()
    }

    /**
     * Determines which, if any, of the handles are pressed given the touch coordinates, the bounding
     * box, and the touch radius.
     *
     * @param x                  the x-coordinate of the touch point
     * @param y                  the y-coordinate of the touch point
     * @param targetCornerRadius the target radius in pixels
     * @return the Handle that was pressed; null if no Handle was pressed
     */
    fun getMoveHandler(
        x: Float, y: Float, targetCornerRadius: Float, targetBorderRadius: Float
    ): CropWindowMoveHandler? {
        val type = getRectanglePressedMoveType(x, y, targetCornerRadius, targetBorderRadius)
        return if (type != null) CropWindowMoveHandler(type, this, x, y) else null
    }

    /**
     * Determines which, if any, of the handles are pressed given the touch coordinates, the bounding
     * box, and the touch radius.
     *
     * @param x                  the x-coordinate of the touch point
     * @param y                  the y-coordinate of the touch point
     * @param targetCornerRadius the target radius in pixels
     * @return the Handle that was pressed; null if no Handle was pressed
     */
    fun getRectanglePressedMoveType(
        x: Float, y: Float, targetCornerRadius: Float, targetBorderRadius: Float
    ): CropWindowMoveHandler.Type? {
        var moveType: CropWindowMoveHandler.Type? = null

        // Note: corner-handles take precedence, then side-handles, then center.
        val cornerZone = checkInCornerZone(x, y)
        if (cornerZone != null) {
            return cornerZone
        } else if (isInCenterTargetZone(x, y) && focusCenter()) {
            moveType = CropWindowMoveHandler.Type.CENTER
        } else if (isInHorizontalTargetZone(x, y, mEdges.left, mEdges.right, mEdges.top, targetBorderRadius)) {
            moveType = CropWindowMoveHandler.Type.TOP
        } else if (isInHorizontalTargetZone(x, y, mEdges.left, mEdges.right, mEdges.bottom, targetBorderRadius)) {
            moveType = CropWindowMoveHandler.Type.BOTTOM
        } else if (isInVerticalTargetZone(x, y, mEdges.left, mEdges.top, mEdges.bottom, targetBorderRadius)) {
            moveType = CropWindowMoveHandler.Type.LEFT
        } else if (isInVerticalTargetZone(x, y, mEdges.right, mEdges.top, mEdges.bottom, targetBorderRadius)) {
            moveType = CropWindowMoveHandler.Type.RIGHT
        } else if (isInCenterTargetZone(x, y) && !focusCenter()) {
            moveType = CropWindowMoveHandler.Type.CENTER
        }
        return moveType
    }


    /**
     * cornerRadius：控制边角可触摸的大小
     */
    private fun checkInCornerZone(
        x: Float,
        y: Float,
    ): CropWindowMoveHandler.Type? {
        if (bottomRightCorner.contains(x, y)) {
            return CropWindowMoveHandler.Type.BOTTOM_RIGHT
        }
        if (topLeftCorner.contains(x, y)) {
            return CropWindowMoveHandler.Type.TOP_LEFT
        }
        if (topRightCorner.contains(x, y)) {
            return CropWindowMoveHandler.Type.TOP_RIGHT
        }
        if (bottomLeftCorner.contains(x, y)) {
            return CropWindowMoveHandler.Type.BOTTOM_LEFT
        }
        return null
    }

    private fun isInCenterTargetZone(x: Float, y: Float): Boolean {
        return centerRect.contains(x, y)
    }


    /**
     * Determines if the cropper should focus on the center handle or the side handles. If it is a
     * small image, focus on the center handle so the user can move it. If it is a large image, focus
     * on the side handles so user can grab them. Corresponds to the appearance of the
     * RuleOfThirdsGuidelines.
     *
     * @return true if it is small enough such that it should focus on the center; less than
     * show_guidelines limit
     */
    private fun focusCenter(): Boolean {
        return false
    } // endregion

    companion object {
        // endregion
        /**
         * Determines if the specified coordinate is in the target touch zone for a corner handle.
         *
         * @param x            the x-coordinate of the touch point
         * @param y            the y-coordinate of the touch point
         * @param handleX      the x-coordinate of the corner handle
         * @param handleY      the y-coordinate of the corner handle
         * @param targetRadius the target radius in pixels
         * @return true if the touch point is in the target touch zone; false otherwise
         */
        private fun isInCornerTargetZone(
            x: Float, y: Float, handleX: Float, handleY: Float, targetRadius: Float
        ): Boolean {
            return Math.abs(x - handleX) <= targetRadius && Math.abs(y - handleY) <= targetRadius
        }

        /**
         * Determines if the specified coordinate is in the target touch zone for a horizontal bar handle.
         *
         * @param x            the x-coordinate of the touch point
         * @param y            the y-coordinate of the touch point
         * @param handleXStart the left x-coordinate of the horizontal bar handle
         * @param handleXEnd   the right x-coordinate of the horizontal bar handle
         * @param handleY      the y-coordinate of the horizontal bar handle
         * @param targetRadius the target radius in pixels
         * @return true if the touch point is in the target touch zone; false otherwise
         */
        private fun isInHorizontalTargetZone(
            x: Float,
            y: Float,
            handleXStart: Float,
            handleXEnd: Float,
            handleY: Float,
            targetRadius: Float
        ): Boolean {
            return x > handleXStart && x < handleXEnd && Math.abs(y - handleY) <= targetRadius
        }

        /**
         * Determines if the specified coordinate is in the target touch zone for a vertical bar handle.
         *
         * @param x            the x-coordinate of the touch point
         * @param y            the y-coordinate of the touch point
         * @param handleX      the x-coordinate of the vertical bar handle
         * @param handleYStart the top y-coordinate of the vertical bar handle
         * @param handleYEnd   the bottom y-coordinate of the vertical bar handle
         * @param targetRadius the target radius in pixels
         * @return true if the touch point is in the target touch zone; false otherwise
         */
        private fun isInVerticalTargetZone(
            x: Float,
            y: Float,
            handleX: Float,
            handleYStart: Float,
            handleYEnd: Float,
            targetRadius: Float
        ): Boolean {
            return Math.abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd
        }
    }
}