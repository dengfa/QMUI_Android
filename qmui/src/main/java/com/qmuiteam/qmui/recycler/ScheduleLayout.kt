package com.qmuiteam.qmui.recyclerimport android.content.Contextimport android.util.AttributeSetimport android.widget.ScrollView/** * Created by dengfa on 2022/1/27 */class ScheduleLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {    var scheduleContainer: ScheduleContainer = ScheduleContainer(context)    init {        addView(scheduleContainer, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)    }    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {        super.onScrollChanged(l, t, oldl, oldt)        scheduleContainer.onScroll(l, t, oldl, oldt)    }    fun setTask(tasks: List<Task>){        scheduleContainer.setTask(tasks)    }}