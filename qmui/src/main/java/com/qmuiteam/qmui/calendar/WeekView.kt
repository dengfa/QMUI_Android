package com.qmuiteam.qmui.calendar

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
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


        setOnDragListener { v, e -> // Handles each of the expected events.
            when (e.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Log.d("vincent", "weekview ACTION_DRAG_STARTED ${e.clipDescription}")
                    e.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> { // Applies a green tint to the View.
                    Log.d("vincent", "weekview ACTION_DRAG_ENTERED")
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> // Ignore the event.
                {
                    Log.d("vincent", "weekview ACTION_DRAG_LOCATION ${e.x}-${e.y}")
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> { // Resets the color tint to blue.
                    Log.d("vincent", "weekview ACTION_DRAG_EXITED")
                    // Returns true; the value is ignored.
                    true
                }
                DragEvent.ACTION_DROP -> { // Gets the item containing the dragged data.
                    Log.d("vincent", "weekview ACTION_DROP")
                    val item: ClipData.Item = e.clipData.getItemAt(0)

                    // Gets the text data from the item.
                    val dragData = item.text

                    // Displays a message containing the dragged data.
                    Toast.makeText(context, "Dragged data is $dragData", Toast.LENGTH_LONG).show()

                    // Returns true. DragEvent.getResult() will return true.
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> { // Turns off any color tinting.
                    Log.d("vincent", "weekview ACTION_DRAG_ENDED")

                    // Does a getResult(), and displays what happened.
                    when (e.result) {
                        true -> Toast.makeText(context, "The drop was handled.", Toast.LENGTH_LONG).show()
                        else -> {
                            Toast.makeText(context, "The drop didn't work.", Toast.LENGTH_LONG).show()
                        }
                    }
                    // Returns true; the value is ignored.
                    true
                }
                else -> { // An unknown action type was received.
                    Log.e("DragDrop Example", "Unknown action type received by View.OnDragListener.")
                    false
                }
            }
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setTasks(tasks: List<WeekTask>, firstDayOfWeekTimeStamp: Long) {
        Log.d("vincent", "setTasks")
        this.firstDayOfWeekTimeStamp = firstDayOfWeekTimeStamp
        this.tasks.clear()
        this.tasks.addAll(tasks)
        post {
            addTaskViews()
        }
    }

    fun addTask(task: WeekTask) {
        Log.d("vincent", "setTasks")
        tasks.add(task)
        post {
            addTaskViews()
        }
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
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("vincent", "onLayout")
        /*blockWidth = width / daysCount
        blockHeight = blockWidth
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
        }*/
    }

    private fun addTaskViews() {
        blockWidth = width / daysCount
        blockHeight = blockWidth

        Log.d("vincent", "addTaskViews blockWidth $blockWidth blockHeight $blockHeight")
        removeAllViews()
        if (tasks.isNotEmpty()) {
            tasks.forEach {
                TextView(context).apply {
                    text = it.taskName
                    setBackgroundColor(context.resources.getColor(R.color.qmui_config_color_red))
                    val taskRect = getTaskRect(it)
                    this@WeekView.addView(this)
                    this.x = taskRect.left.toFloat()
                    this.y = taskRect.top.toFloat()
                    this.layoutParams.apply {
                        width = (taskRect.right - taskRect.left)
                        height = (taskRect.bottom - taskRect.top)
                    }

                    setOnLongClickListener { view ->
                        val item = ClipData.Item(it.taskName)
                        val dragData = ClipData(it.taskName, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                        val shadow = DragShadowBuilder(this)
                        view.startDragAndDrop(dragData, shadow, null, 0)
                        true
                    }

                    setOnDragListener { v, e -> // Handles each of the expected events.
                        when (e.action) {
                            DragEvent.ACTION_DRAG_STARTED -> {
                                Log.d("vincent", "ACTION_DRAG_STARTED ${it.taskName} ${e.clipDescription}")

                                if (e.clipDescription.label == it.taskName) {
                                    v.visibility = INVISIBLE
                                    true
                                } else { // Returns false to indicate that, during the current drag and drop operation,
                                    // this View will not receive events again until ACTION_DRAG_ENDED is sent.
                                    false
                                }
                            }
                            DragEvent.ACTION_DRAG_ENTERED -> { // Applies a green tint to the View.
                                Log.d("vincent", "ACTION_DRAG_ENTERED ${it.taskName}")
                                v.alpha = 0.5f

                                // Invalidates the view to force a redraw in the new tint.
                                v.invalidate()

                                // Returns true; the value is ignored.
                                true
                            }

                            DragEvent.ACTION_DRAG_LOCATION -> // Ignore the event.
                            {
                                Log.d("vincent", "ACTION_DRAG_LOCATION ${it.taskName}")
                                true
                            }
                            DragEvent.ACTION_DRAG_EXITED -> { // Resets the color tint to blue.
                                Log.d("vincent", "ACTION_DRAG_EXITED ${it.taskName}")
                                v.alpha = 1f

                                // Invalidates the view to force a redraw in the new tint.
                                v.invalidate()

                                // Returns true; the value is ignored.
                                true
                            }
                            DragEvent.ACTION_DROP -> { // Gets the item containing the dragged data.
                                Log.d("vincent", "ACTION_DROP ${it.taskName}")
                                val item: ClipData.Item = e.clipData.getItemAt(0)

                                // Gets the text data from the item.
                                val dragData = item.text

                                // Displays a message containing the dragged data.
                                Toast.makeText(context, "Dragged data is $dragData", Toast.LENGTH_LONG).show()

                                // Returns true. DragEvent.getResult() will return true.
                                true
                            }

                            DragEvent.ACTION_DRAG_ENDED -> { // Turns off any color tinting.
                                Log.d("vincent", "ACTION_DRAG_ENDED ${it.taskName}")
                                // Does a getResult(), and displays what happened.
                                when (e.result) {
                                    true -> Toast.makeText(context, "The drop was handled.", Toast.LENGTH_LONG).show()
                                    else -> {
                                        Toast.makeText(context, "The drop didn't work.", Toast.LENGTH_LONG).show()
                                        v.visibility = VISIBLE
                                    }
                                }
                                // Returns true; the value is ignored.
                                true
                            }
                            else -> { // An unknown action type was received.
                                Log.d("vincent", "drag else ${it.taskName}")
                                Log.e("DragDrop Example", "Unknown action type received by View.OnDragListener.")
                                false
                            }
                        }
                    }
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
        Log.d("vincent", "getTaskRect ${task.taskName}  $dayOfWeek $startHour:$startMinute - $endHour:$endMinute")

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
