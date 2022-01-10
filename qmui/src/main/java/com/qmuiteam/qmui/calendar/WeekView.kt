package com.qmuiteam.qmui.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.qmuiteam.qmui.R

/**
 * Created by dengfa on 2022/1/10
 * calendar week view
 */
class WeekView(context: Context, attrs: AttributeSet, defStyle: Int) : View(context, attrs, defStyle) {
    private val ROWS_CNT = 24
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var daysCount = 7

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    init {
        paint.color = context.resources.getColor(R.color.qmui_config_color_gray_1)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rowHeight = 85
        for (i in 0 until ROWS_CNT) {
            val y = rowHeight * i.toFloat()
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
        }

        val rowWidth = width / daysCount.toFloat()
        for (i in 0 until daysCount) {
            val x = rowWidth * i.toFloat()
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }
    }
}