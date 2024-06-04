package com.qmuiteam.qmui.widget.crop.photoView

import android.graphics.Matrix

/**
 * Date: 2022-06-28
 * Desc:
 */
class MatrixWithListener(private val changeListener: () -> Unit = {}) : Matrix() {
    override fun postTranslate(dx: Float, dy: Float): Boolean {
        changeListener.invoke()
        return super.postTranslate(dx, dy)
    }

    override fun postScale(sx: Float, sy: Float, px: Float, py: Float): Boolean {
        changeListener.invoke()
        return super.postScale(sx, sy, px, py)
    }

    override fun postScale(sx: Float, sy: Float): Boolean {
        changeListener.invoke()
        return super.postScale(sx, sy)
    }
}