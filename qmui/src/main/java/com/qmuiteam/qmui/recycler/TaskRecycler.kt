package com.qmuiteam.qmui.recyclerimport android.content.Contextimport android.util.Logimport java.util.*/** * Created by dengfa on 2022/1/28 */class TaskRecycler {    private val maxSize = 100    private val taskViewPool: Stack<TaskView> = Stack()    fun recycle(taskView: TaskView) {        if (taskViewPool.size >= maxSize) return        taskViewPool.push(taskView)        Log.i("vincent", "TaskRecycler recycle ${taskViewPool.size}")    }    fun recycleAll(taskViews: MutableCollection<TaskView>){        taskViews.forEach { taskView ->            recycle(taskView)        }    }    fun obtain(context: Context): TaskView {        return if (taskViewPool.isNotEmpty()) {            Log.i("vincent", "TaskRecycler obtain pop")            taskViewPool.pop()        } else {            Log.i("vincent", "TaskRecycler obtain new")            TaskView(context)        }    }}