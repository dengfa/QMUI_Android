package com.qmuiteam.qmui.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.qmuiteam.qmui.R
import com.qmuiteam.qmui.dp
import com.qmuiteam.qmui.dpF
import java.util.Calendar
import java.util.Date

/**
 * Created by dengfa on 2022/1/10
 * calendar week view
 *  * 知识点：
 * 1.拖放处理
 * 2.自定义View-处理横竖屏切换 todo
 * 3.自定义View-预先获取宽高 todo
 */
class CalendarScheduleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val DEFAULT_BLOCK_SIZE: Int = 85.dp
    private val ROWS_CNT = 24
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var daysCount = 1
    var blockWidth = 85.dp
    var blockHeight = 85.dp
    var timeLineWidth = 58.dp
    var timeLineTextSize = 10.dp
    private var gestureDetector: GestureDetectorCompat
    private var firstOfWeekTimeStamp = 0L

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CalendarScheduleView)
        daysCount = if (a.getInteger(R.styleable.CalendarScheduleView_style, 0) == 0) 7 else 1
        a.recycle()

        highlightPaint.color = Color.parseColor("#FFF2CE")
        highlightPaint.style = FILL
        gestureDetector = GestureDetectorCompat(context, WeekGestureDetector())
        setWillNotDraw(false)
    }

    fun setFirstOfWeekTimeStamp(firstOfWeekTimeStamp: Long) {
        this.firstOfWeekTimeStamp = firstOfWeekTimeStamp
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        blockWidth = (width - timeLineWidth) / daysCount
        blockHeight = (height - timeLineTextSize) / ROWS_CNT
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(DEFAULT_BLOCK_SIZE * daysCount, DEFAULT_BLOCK_SIZE * ROWS_CNT + timeLineTextSize)
        } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(DEFAULT_BLOCK_SIZE * daysCount, heightSize)
        } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, DEFAULT_BLOCK_SIZE * ROWS_CNT + timeLineTextSize)
        }
    }

    private val textBoundsRect = Rect()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until ROWS_CNT + 1) {
            val y = blockHeight * i.toFloat() + timeLineTextSize / 2
            paint.strokeWidth = 1.dpF
            paint.color = 0x1A000000
            canvas.drawLine(timeLineWidth.toFloat(), y, width.toFloat(), y, paint)
            paint.textSize = timeLineTextSize.toFloat()
            paint.color = Color.parseColor("#2B2D33")
            val timeStr = "${if (i < 10) "0" else ""}$i:00"
            paint.getTextBounds(timeStr, 0, timeStr.length, textBoundsRect)
            canvas.drawText(timeStr, (timeLineWidth - textBoundsRect.width()) / 2f, y + textBoundsRect.height() / 2, paint)
        }
    }

    inner class WeekGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {

        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            val targetDate = getTargetDate(e?.x?.toInt() ?: 0, e?.y?.toInt() ?: 0)
            Toast.makeText(context, "onDoubleTap ${targetDate.time.format()}", Toast.LENGTH_SHORT).show()

            //todo test
            val taskInfo = TaskInfo(targetDate.timeInMillis, targetDate.timeInMillis + 60 * 60 * 1000, "task${targetDate.time.format()}")
            val taskView = TaskView(context)
            taskView.setTask(taskInfo)
            addTask(taskInfo.startTimeStamp, taskInfo.endTimeStamp, taskView)
            return true
        }
    }

    fun getTargetRect(x: Int, y: Int): Rect {
        val columnIndex = (x - timeLineWidth) / blockWidth
        val rowIndex = (y + timeLineTextSize / 2) / blockHeight
        return Rect(timeLineWidth + columnIndex * blockWidth,
                rowIndex * blockHeight + timeLineTextSize / 2,
                timeLineWidth + (columnIndex + 1) * blockWidth,
                (rowIndex + 1) * blockHeight + timeLineTextSize / 2)
    }

    fun getTargetDate(x: Int, y: Int): Calendar {
        val columnIndex = (x - timeLineWidth) / blockWidth
        val rowIndex = (y + timeLineTextSize / 2) / blockHeight
        val calendar = Calendar.getInstance()
        calendar.time = Date(firstOfWeekTimeStamp)
        calendar.add(Calendar.DAY_OF_MONTH, columnIndex)
        calendar.set(Calendar.HOUR_OF_DAY, rowIndex)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar
    }

    fun addTask(taskStartTime: Long, taskEndTime: Long, taskView: View) {
        val startTimeCalendar = Calendar.getInstance()
        startTimeCalendar.time = Date(taskStartTime)
        val startDayOfWeek = startTimeCalendar.get(Calendar.DAY_OF_WEEK)
        val startHour = startTimeCalendar.get(Calendar.HOUR_OF_DAY)
        val startMinute = startTimeCalendar.get(Calendar.MINUTE)

        val endTimeCalendar = Calendar.getInstance()
        endTimeCalendar.time = Date(taskEndTime)
        val endDayOfWeek = endTimeCalendar.get(Calendar.DAY_OF_WEEK)
        val endHour = endTimeCalendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = endTimeCalendar.get(Calendar.MINUTE)

        //暂不考虑跨天的情况
        if (startDayOfWeek != endDayOfWeek) return

        val left = if (daysCount == 7) timeLineWidth + (startDayOfWeek - 1) * blockWidth else timeLineWidth
        val top = ((startHour + startMinute / 60f) * blockHeight).toInt() + timeLineTextSize / 2
        val right = if (daysCount == 7) timeLineWidth + startDayOfWeek * blockWidth else timeLineWidth + blockWidth
        val bottom = ((endHour + endMinute / 60f) * blockHeight).toInt() + timeLineTextSize / 2

        taskView.x = left.toFloat()
        taskView.y = top.toFloat()
        addView(taskView, right - left, bottom - top)
    }
}

private class MyDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {

    private val shadow = ColorDrawable(Color.LTGRAY)

    // Defines a callback that sends the drag shadow dimensions and touch point
    // back to the system.
    override fun onProvideShadowMetrics(size: Point, touch: Point) {

        // Set the width of the shadow to half the width of the original View.
        val width: Int = view.width / 2

        // Set the height of the shadow to half the height of the original View.
        val height: Int = view.height / 2

        // The drag shadow is a ColorDrawable. This sets its dimensions to be the
        // same as the Canvas that the system provides. As a result, the drag shadow
        // fills the Canvas.
        shadow.setBounds(0, 0, width, height)

        // Set the size parameter's width and height values. These get back to
        // the system through the size parameter.
        size.set(width, height)

        // Set the touch point's position to be in the middle of the drag shadow.
        touch.set(width / 2, height / 2)
    }

    // Defines a callback that draws the drag shadow in a Canvas that the system
    // constructs from the dimensions passed to onProvideShadowMetrics().
    override fun onDrawShadow(canvas: Canvas) {

        // Draw the ColorDrawable on the Canvas passed in from the system.
        shadow.draw(canvas)
    }
}