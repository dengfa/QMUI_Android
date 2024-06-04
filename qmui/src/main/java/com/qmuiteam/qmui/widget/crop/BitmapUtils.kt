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

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.qmuiteam.qmui.widget.crop.CropImageView.RequestSizeOptions

/** Utility class that deals with operations with an ImageView.  */
object BitmapUtils {

    private val TAG = "crop_bitmap_utils"

    val EMPTY_RECT = Rect()
    val EMPTY_RECT_F = RectF()

    /**
     * Reusable rectangle for general internal usage
     */
    val RECT = RectF()

    /**
     * Reusable point for general internal usage
     */
    val POINTS = FloatArray(6)

    /**
     * Reusable point for general internal usage
     */
    val POINTS2 = FloatArray(6)




    /**
     * Crop image bitmap from given bitmap using the given points in the original bitmap and the given
     * rotation.<br></br>
     * if the rotation is not 0,90,180 or 270 degrees then we must first crop a larger area of the
     * image that contains the requires rectangle, rotate and then crop again a sub rectangle.<br></br>
     * If crop fails due to OOM we scale the cropping image by 0.5 every time it fails until it is
     * small enough.
     */
    fun cropBitmapObjectHandleOOM(
        bitmap: Bitmap,
        points: FloatArray,
        degreesRotated: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int
    ): BitmapSampled {
        var scale = 1
        while (true) {
            try {
                val cropBitmap = cropBitmapObjectWithScale(
                    bitmap,
                    points,
                    degreesRotated,
                    fixAspectRatio,
                    aspectRatioX,
                    aspectRatioY,
                    1 / scale.toFloat()
                )
                return BitmapSampled(cropBitmap)
            } catch (e: OutOfMemoryError) {
                scale *= 2
                if (scale > 8) {
                    throw e
                }
            }
        }
    }

    /**
     * Crop image bitmap from given bitmap using the given points in the original bitmap and the given
     * rotation.<br></br>
     * if the rotation is not 0,90,180 or 270 degrees then we must first crop a larger area of the
     * image that contains the requires rectangle, rotate and then crop again a sub rectangle.
     *
     * @param scale how much to scale the cropped image part, use 0.5 to lower the image by half (OOM
     * handling)
     */
    private fun cropBitmapObjectWithScale(
        bitmap: Bitmap,
        points: FloatArray,
        degreesRotated: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
        scale: Float
    ): Bitmap {

        // get the rectangle in original image that contains the required cropped area (larger for non
        // rectangular crop)
        val rect = getRectFromPoints(
            points,
            bitmap.width,
            bitmap.height,
            fixAspectRatio,
            aspectRatioX,
            aspectRatioY
        )

        // crop and rotate the cropped image in one operation
        val matrix = Matrix()
        matrix.setRotate(
            degreesRotated.toFloat(),
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat()
        )
        matrix.postScale(scale, scale)
        var result = Bitmap.createBitmap(
            bitmap,
            rect.left,
            rect.top,
            rect.width(),
            rect.height(),
            matrix,
            true
        )

        // https://github.com/ArthurHub/Android-Image-Cropper/issues/560
        if (result == bitmap && bitmap.config != null) {
            // corner case when all bitmap is selected, no worth optimizing for it
            result = bitmap.copy(bitmap.config, false)
        }

        // rotating by 0, 90, 180 or 270 degrees doesn't require extra cropping
        if (degreesRotated % 90 != 0) {

            // extra crop because non rectangular crop cannot be done directly on the image without
            // rotating first
            result = cropForRotatedImage(
                result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY
            )
        }
        return result
    }



    /**
     * Get left value of the bounding rectangle of the given points.
     */
    fun getRectLeft(points: FloatArray): Float {
        if (points.size < 7) {
            return 0f
        }
        return Math.min(Math.min(Math.min(points[0], points[2]), points[4]), points[6])
    }

    /**
     * Get top value of the bounding rectangle of the given points.
     */
    fun getRectTop(points: FloatArray): Float {
        if (points.size < 8) {
            return 0f
        }
        return Math.min(Math.min(Math.min(points[1], points[3]), points[5]), points[7])
    }

    /**
     * Get right value of the bounding rectangle of the given points.
     */
    fun getRectRight(points: FloatArray): Float {
        if (points.size < 7) {
            return 0f
        }
        return Math.max(Math.max(Math.max(points[0], points[2]), points[4]), points[6])
    }

    /**
     * Get bottom value of the bounding rectangle of the given points.
     */
    fun getRectBottom(points: FloatArray): Float {
        if (points.size < 8) {
            return 0f
        }
        return Math.max(Math.max(Math.max(points[1], points[3]), points[5]), points[7])
    }

    /**
     * Get width of the bounding rectangle of the given points.
     */
    fun getRectWidth(points: FloatArray): Float {
        return getRectRight(points) - getRectLeft(points)
    }

    /**
     * Get height of the bounding rectangle of the given points.
     */
    fun getRectHeight(points: FloatArray): Float {
        return getRectBottom(points) - getRectTop(points)
    }

    /**
     * Get horizontal center value of the bounding rectangle of the given points.
     */
    fun getRectCenterX(points: FloatArray): Float {
        return (getRectRight(points) + getRectLeft(points)) / 2f
    }

    /**
     * Get vertical center value of the bounding rectangle of the given points.
     */
    fun getRectCenterY(points: FloatArray): Float {
        return (getRectBottom(points) + getRectTop(points)) / 2f
    }

    /**
     * Get a rectangle for the given 4 points (x0,y0,x1,y1,x2,y2,x3,y3) by finding the min/max 2
     * points that contains the given 4 points and is a straight rectangle.
     */
    fun getRectFromPoints(
        points: FloatArray,
        imageWidth: Int,
        imageHeight: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int
    ): Rect {
        val left = Math.round(Math.max(0f, getRectLeft(points)))
        val top = Math.round(Math.max(0f, getRectTop(points)))
        val right = Math.round(Math.min(imageWidth.toFloat(), getRectRight(points)))
        val bottom = Math.round(Math.min(imageHeight.toFloat(), getRectBottom(points)))
        val rect = Rect(left, top, right, bottom)
        if (fixAspectRatio) {
            fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
        }
        return rect
    }

    /**
     * Fix the given rectangle if it doesn't confirm to aspect ration rule.<br></br>
     * Make sure that width and height are equal if 1:1 fixed aspect ratio is requested.
     */
    private fun fixRectForAspectRatio(rect: Rect?, aspectRatioX: Int, aspectRatioY: Int) {
        if (aspectRatioX == aspectRatioY && rect!!.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width()
            } else {
                rect.right -= rect.width() - rect.height()
            }
        }
    }

    /**
     * Resize the given bitmap to the given width/height by the given option.<br></br>
     */
    fun resizeBitmap(
        bitmap: Bitmap, reqWidth: Int, reqHeight: Int, options: RequestSizeOptions
    ): Bitmap {
        try {
            if (reqWidth > 0 && reqHeight > 0 && (options === RequestSizeOptions.RESIZE_FIT || options === RequestSizeOptions.RESIZE_INSIDE || options === RequestSizeOptions.RESIZE_EXACT)) {
                var resized: Bitmap? = null
                if (options === RequestSizeOptions.RESIZE_EXACT) {
                    resized = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false)
                } else {
                    val width = bitmap.width
                    val height = bitmap.height
                    val scale = Math.max(width / reqWidth.toFloat(), height / reqHeight.toFloat())
                    if (scale > 1 || options === RequestSizeOptions.RESIZE_FIT) {
                        resized = Bitmap.createScaledBitmap(
                            bitmap, (width / scale).toInt(), (height / scale).toInt(), false
                        )
                    }
                }
                if (resized != null) {
                    if (resized != bitmap) {
                        bitmap.recycle()
                    }
                    return resized
                }
            }
        } catch (e: Exception) {
            Log.w("AIC", "Failed to resize cropped image, return bitmap before resize", e)
        }
        return bitmap
    }



    /**
     * Special crop of bitmap rotated by not stright angle, in this case the original crop bitmap
     * contains parts beyond the required crop area, this method crops the already cropped and rotated
     * bitmap to the final rectangle.<br></br>
     * Note: rotating by 0, 90, 180 or 270 degrees doesn't require extra cropping.
     */
    private fun cropForRotatedImage(
        bitmap: Bitmap?,
        points: FloatArray,
        rect: Rect?,
        degreesRotated: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int
    ): Bitmap? {
        var bitmap = bitmap
        val isAvailableRect = rect != null && rect.width() > 0 && rect.height() > 0
        if (degreesRotated % 90 != 0 && isAvailableRect) {
            var adjLeft = 0
            var adjTop = 0
            var width = 0
            var height = 0
            val rads = Math.toRadians(degreesRotated.toDouble())
            val compareTo =
                if (degreesRotated < 90 || degreesRotated > 180 && degreesRotated < 270) rect!!.left else rect!!.right
            var i = 0
            while (i < points.size) {
                if (points[i] >= compareTo - 1 && points[i] <= compareTo + 1) {
                    adjLeft = Math.abs(Math.sin(rads) * (rect.bottom - points[i + 1])).toInt()
                    adjTop = Math.abs(Math.cos(rads) * (points[i + 1] - rect.top)).toInt()
                    width = Math.abs((points[i + 1] - rect.top) / Math.sin(rads)).toInt()
                    height = Math.abs((rect.bottom - points[i + 1]) / Math.cos(rads)).toInt()
                    break
                }
                i += 2
            }
            rect[adjLeft, adjTop, adjLeft + width] = adjTop + height
            if (fixAspectRatio) {
                fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
            }
            val bitmapTmp = bitmap
            bitmap = Bitmap.createBitmap(bitmap!!, rect.left, rect.top, rect.width(), rect.height())
            if (bitmapTmp != bitmap) {
                bitmapTmp!!.recycle()
            }
        }
        return bitmap
    }

    // endregion
    // region: Inner class: BitmapSampled
    /**
     * Holds bitmap instance and the sample size that the bitmap was loaded/cropped with.
     */
    class BitmapSampled(
        /**
         * The bitmap instance
         */
        val bitmap: Bitmap,
    )
    // endregion
    // region: Inner class: RotateBitmapResult

}