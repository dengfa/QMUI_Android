package com.qmuiteam.qmui.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import com.qmuiteam.qmui.R
import java.util.Calendar
import java.util.Date

/**
 * Created by dengfa on 2022/1/10
 * calendar week view
 */
class WeekView : FrameLayout {
    private val DEFAULT_SIZE: Int = 200
    private val ROWS_CNT = 24
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var daysCount = 7
    var blockWidth = 180
    var blockHeight = 180
    private var gestureDetector: GestureDetectorCompat

    private var highlightRect: Rect? = null
    private var tasks = arrayListOf<WeekTask>()
    private var firstDayOfWeekTimeStamp = 0L

    init {
        paint.color = context.resources.getColor(R.color.qmui_config_color_gray_1)
        highlightPaint.color = context.resources.getColor(R.color.qmui_config_color_red)
        highlightPaint.style = FILL
        gestureDetector = GestureDetectorCompat(context, WeekGestureDetector())
        setWillNotDraw(false)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setTasks(tasks: List<WeekTask>, firstDayOfWeekTimeStamp: Long) {
        Log.d("vincent", "setTasks")
        this.firstDayOfWeekTimeStamp = firstDayOfWeekTimeStamp
        this.tasks.clear()
        this.tasks.addAll(tasks)
        requestLayout()
    }

    fun addTask(task: WeekTask) {
        Log.d("vincent", "setTasks")
        tasks.add(task)
        requestLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("vincent", "weekview onSizeChanged $w-$h old $oldw-$oldh")
        blockWidth = width / daysCount
        blockHeight = blockWidth

        //为什么要加个post， addView才显示？
        //onSizeChanged在onMeasure后执行，这时候addView的话，子View错过了onMeasure阶段。
        /*removeAllViews()
          post {
            if (tasks.isNotEmpty()) {
                tasks.forEach {
                    TextView(context).apply {
                        text = it.taskName
                        setBackgroundColor(context.resources.getColor(R.color.qmui_config_color_red))
                        val taskRect = getTaskRect(it)
                        this@WeekView.addView(this)
                        this.x = taskRect.left
                        this.y = taskRect.top
                        this.layoutParams.apply {
                            width = (taskRect.right - taskRect.left).toInt()
                            height = (taskRect.bottom - taskRect.top).toInt()
                            Log.d("vincent", "width $width height $height")
                        }
                    }
                }
            }
        }*/
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("vincent", "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Log.d("vincent", "widthSpecMode $widthSpecMode heightSpecMode $heightSpecMode")

        // widthSpecMode 1073741824 heightSpecMode 0 （UNSPECIFIED）
        // 嵌套在ScrollerView里面时，WrapContent对应于UNSPECIFIED，所有应该通过layoutParams判断
        /*if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, rawHeight * ROWS_CNT)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, heightSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, rawHeight * ROWS_CNT)
        }*/


        // 当布局参数设置为wrap_content时，设置默认值
        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE / daysCount * ROWS_CNT)
        } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE / daysCount * ROWS_CNT)
        } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, widthSize / daysCount * ROWS_CNT)
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("vincent", "onLayout")

        removeAllViews()
        if (tasks.isNotEmpty()) {
            tasks.forEach {
                TextView(context).apply {
                    text = it.taskName
                    setBackgroundColor(context.resources.getColor(R.color.qmui_config_color_red))
                    val taskRect = getTaskRect(it)
                    this@WeekView.addView(this)
                    layout(taskRect.left, taskRect.top, taskRect.right, taskRect.bottom)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("vincent", "onDraw")

        for (i in 0 until ROWS_CNT) {
            val y = blockHeight * i.toFloat()
            canvas.drawLine(0f, y, width.toFloat(), y, paint)

            paint.textSize = 28f
            canvas.drawText("$i", blockWidth / 2f, y + blockHeight / 2, paint)
        }

        for (i in 0 until daysCount) {
            val x = blockWidth * i.toFloat()
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }
    }


    var startTimeTest = 1641916800L
    var taskIndex = 3

    inner class WeekGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            Log.d("vincent", "onLongPress $e")

        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            highlightRect = getTargetRect(e?.x?.toInt() ?: 0, e?.y?.toInt() ?: 0)
            Log.d("vincent", "highlightRect $highlightRect")
            addTask(WeekTask(startTimeTest, startTimeTest + 60 * 60, "task$taskIndex"))
            startTimeTest += 60 * 60
            taskIndex++
            return true
        }
    }

    fun getTargetRect(x: Int, y: Int): Rect {
        Log.d("vincent", "x $x y $y getTargetRect ${x / blockWidth} ${y / blockHeight}")
        return Rect(x / blockWidth * blockWidth,
            y / blockHeight * blockHeight,
            (x / blockWidth + 1) * blockWidth,
            (y / blockHeight + 1) * blockHeight)
    }

    //暂不考虑跨天的情况
    private fun getTaskRect(task: WeekTask): Rect { //todo : 优化 缓存rect
        val startTimeCalendar = Calendar.getInstance()
        startTimeCalendar.time = Date(task.startTimeStamp * 1000)
        val dayOfWeek = startTimeCalendar.get(Calendar.DAY_OF_WEEK)
        val startHour = startTimeCalendar.get(Calendar.HOUR_OF_DAY)
        val startMinute = startTimeCalendar.get(Calendar.MINUTE)

        val endTimeCalendar = Calendar.getInstance()
        endTimeCalendar.time = Date(task.endTimeStamp * 1000)
        val endHour = endTimeCalendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = endTimeCalendar.get(Calendar.MINUTE)
        Log.d("vincent", "${task.taskName} dayOfWeek $dayOfWeek $startHour:$startMinute - $endHour:$endMinute")

        return Rect((dayOfWeek - 1) * blockWidth,
            ((startHour + startMinute / 60f) * blockHeight).toInt(),
            dayOfWeek * blockWidth,
            ((endHour + endMinute / 60f) * blockHeight).toInt())
    }
}

class WeekTask(
    var startTimeStamp: Long = 0L,
    var endTimeStamp: Long = 0L,
    var taskName: String = "task",
)