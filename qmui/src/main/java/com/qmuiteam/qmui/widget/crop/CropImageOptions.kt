// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth;
// inexhaustible as the great rivers.
// When they come to an end;
// they begin again;
// like the days and months;
// they die and are reborn;
// like the four seasons."
//
// - Sun Tsu;
// "The Art of War"
package com.qmuiteam.qmui.widget.crop

import android.content.res.Resources
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.util.TypedValue
import com.qmuiteam.qmui.widget.crop.CropImageView
import com.qmuiteam.qmui.widget.crop.CropImageView.RequestSizeOptions

/**
 * All the possible options that can be set to customize crop image.<br></br>
 * Initialized with default values.
 */
class CropImageOptions  {
    /**
     * An edge of the crop window will snap to the corresponding edge of a specified bounding box when
     * the crop window edge is less than or equal to this distance (in pixels) away from the bounding
     * box edge. (in pixels)
     */
    var snapRadius: Float

    /**
     * The radius of the touchable area around the handle. (in pixels)<br></br>
     * We are basing this value off of the recommended 48dp Rhythm.<br></br>
     * See: http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm
     */
    var touchRadius // corner
            : Float
    var touchBorderRadius // border
            : Float

    /**
     * The initial scale type of the image in the crop image view
     */
    var scaleType: CropImageView.ScaleType

    /**
     * if to show crop overlay UI what contains the crop window UI surrounded by background over the
     * cropping image.<br></br>
     * default: true, may disable for animation or frame transition.
     */
    var showCropOverlay: Boolean

    /**
     * The initial crop window padding from image borders in percentage of the cropping image
     * dimensions.
     */
    var initialHorizonCropWindowPaddingRatio: Float
    var initialVerticalCropWindowPaddingRatio: Float

    /**
     * the thickness of the guidelines lines in pixels. (in pixels)
     */
    var borderLineThickness: Float

    /**
     * the color of the guidelines lines
     */
    var unSelectedBorderLineColor: Int
    var selectedBorderLineColor: Int
    var unSelectedBorderSolidColor: Int

    /**
     * the color of the overlay background around the crop window cover the image parts not in the
     * crop window.
     */
    var backgroundColor: Int

    /**
     * the min width the crop window is allowed to be. (in pixels)
     */
    var minCropWindowWidth: Int

    /**
     * the min height the crop window is allowed to be. (in pixels)
     */
    var minCropWindowHeight: Int

    /**
     * the min width the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    var minCropResultWidth: Int

    /**
     * the min height the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    var minCropResultHeight: Int

    /**
     * the max width the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    var maxCropResultWidth: Int

    /**
     * the max height the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    var maxCropResultHeight: Int

    /**
     * the Android Uri to save the cropped image to
     */
    var outputUri: Uri?

    /**
     * the compression format to use when writing the image
     */
    var outputCompressFormat: CompressFormat

    /**
     * the quality (if applicable) to use when writing the image (0 - 100)
     */
    var outputCompressQuality: Int

    /**
     * the width to resize the cropped image to (see options)
     */
    var outputRequestWidth: Int

    /**
     * the height to resize the cropped image to (see options)
     */
    var outputRequestHeight: Int

    /**
     * the resize method to use on the cropped bitmap (see options documentation)
     */
    var outputRequestSizeOptions: RequestSizeOptions

    /**
     * if the result of crop image activity should not save the cropped image bitmap
     */
    var noOutputImage: Boolean

    /**
     * the initial rectangle to set on the cropping image after loading
     */
    var initialCropWindowRectangle: Rect?

    /**
     * the initial rotation to set on the cropping image after loading (0-360 degrees clockwise)
     */
    var initialRotation: Int

    /**
     * if to allow (all) rotation during cropping (activity)
     */
    var allowRotation: Boolean

    /**
     * if to allow (all) flipping during cropping (activity)
     */
    var allowFlipping: Boolean

    /**
     * if to allow counter-clockwise rotation during cropping (activity)
     */
    var allowCounterRotation: Boolean

    /**
     * the amount of degrees to rotate clockwise or counter-clockwise
     */
    var rotationDegrees: Int

    /**
     * optional, the text of the crop menu crop button
     */
    var cropMenuCropButtonTitle: CharSequence?

    /**
     * optional image resource to be used for crop menu crop icon instead of text
     */
    var cropMenuCropButtonIcon: Int

    /**
     * option 是否可以任意移动
     */
    var cropMoveNotRestricted: Boolean

    /**
     * Init options with defaults.
     */
    constructor() {
        val dm = Resources.getSystem().displayMetrics
        snapRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, dm)
        touchRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54f, dm)
        touchBorderRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, dm)
        scaleType = CropImageView.ScaleType.FIT_CENTER
        showCropOverlay = true
        initialHorizonCropWindowPaddingRatio = 0.1f
        initialVerticalCropWindowPaddingRatio = 0.1f
        borderLineThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, dm)
        unSelectedBorderLineColor = Color.parseColor("#80ffffff")
        selectedBorderLineColor = Color.parseColor("#1fffffff")
        unSelectedBorderSolidColor = Color.parseColor("#1f000000")
        backgroundColor = Color.parseColor("#4D161823")
        minCropWindowWidth = 0 //最小不设限制
        minCropWindowHeight = 0 //最小不设限制
        minCropResultWidth = 40
        minCropResultHeight = 40
        maxCropResultWidth = 99999
        maxCropResultHeight = 99999
        outputUri = Uri.EMPTY
        outputCompressFormat = CompressFormat.JPEG
        outputCompressQuality = 90
        outputRequestWidth = 0
        outputRequestHeight = 0
        outputRequestSizeOptions = RequestSizeOptions.NONE
        noOutputImage = false
        initialCropWindowRectangle = null
        initialRotation = -1
        allowRotation = true
        allowFlipping = true
        allowCounterRotation = false
        rotationDegrees = 90
        cropMenuCropButtonTitle = null
        cropMenuCropButtonIcon = 0
        cropMoveNotRestricted = false
    }


    /**
     * Validate all the options are withing valid range.
     *
     * @throws IllegalArgumentException if any of the options is not valid
     */
    fun validate() {
        require(touchRadius >= 0) { "Cannot set touch radius value to a number <= 0 " }
        require(touchBorderRadius >= 0) { "Cannot set touch border radius value to a number <= 0 " }
        require(!(initialHorizonCropWindowPaddingRatio < 0 || initialHorizonCropWindowPaddingRatio >= 0.5)) { "Cannot set initial crop horizon window padding value to a number < 0 or >= 0.5" }
        require(!(initialVerticalCropWindowPaddingRatio < 0 || initialVerticalCropWindowPaddingRatio >= 0.5)) { "Cannot set initial crop vertical window padding value to a number < 0 or >= 0.5" }
        require(borderLineThickness >= 0) { "Cannot set line thickness value to a number less than 0." }
        require(minCropWindowHeight >= 0) { "Cannot set min crop window height value to a number < 0 " }
        require(minCropResultWidth >= 0) { "Cannot set min crop result width value to a number < 0 " }
        require(minCropResultHeight >= 0) { "Cannot set min crop result height value to a number < 0 " }
        require(maxCropResultWidth >= minCropResultWidth) { "Cannot set max crop result width to smaller value than min crop result width" }
        require(maxCropResultHeight >= minCropResultHeight) { "Cannot set max crop result height to smaller value than min crop result height" }
        require(outputRequestWidth >= 0) { "Cannot set request width value to a number < 0 " }
        require(outputRequestHeight >= 0) { "Cannot set request height value to a number < 0 " }
        require(!(rotationDegrees < 0 || rotationDegrees > 360)) { "Cannot set rotation degrees value to a number < 0 or > 360" }
    }

}