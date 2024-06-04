package com.qmuiteam.qmui.widget.crop

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class AlwaysClipImageView : AppCompatImageView {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        val count = canvas.save()
        canvas.clipRect(0, 0, width, height)
        super.onDraw(canvas)
        canvas.restoreToCount(count)
    }
}