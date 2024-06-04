package com.miracle.photo.crop

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import com.qmuiteam.qmui.widget.crop.CropOverlayView

data class RectAndIndex(
   val rectF: RectF,
    val index: Int,
)

interface ICropImageView {

    val cropTouchRect: RectF

    var rotatedDegrees: Int

    val croppedImage: Bitmap?

    fun setCropMoveGestureListener(listener: CropOverlayView.CropMoveGestureListener?)

    fun setShowCropOverlay(showCropOverlay: Boolean)

    fun setImageBitmap(bitmap: Bitmap?)

    fun getOverlayView(): View

    fun setCropRect(rect: List<Rect>)

    fun startScanAnim(rotateDegree: Int)

    fun stopScanView()

    fun getRadioRect(): List<RectAndIndex> {
        return listOf()
    }

    fun getAdjustCropWindow(): List<Int> {
        return listOf()
    }

    fun setSelectRect(index: Int) {}
}