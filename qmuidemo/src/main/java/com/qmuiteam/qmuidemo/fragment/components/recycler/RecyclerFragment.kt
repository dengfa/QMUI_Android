package com.qmuiteam.qmuidemo.fragment.components.recycler

import android.view.LayoutInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.recycler.ScheduleLayout
import com.qmuiteam.qmui.recycler.Task
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription

/**
 * 知识点：
 * 1.自定义View的回收与复用
 *
 */
@Widget(name = "Recycler", iconRes = R.mipmap.icon_grid_tip_dialog)
class RecyclerFragment : BaseFragment() {

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout


    @BindView(R.id.scheduleLayout)
    internal lateinit var scheduleLayout: ScheduleLayout

    private lateinit var mQDItemDescription: QDItemDescription


    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_recycler, null)
        ButterKnife.bind(this, view)
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.javaClass)
        initTopBar()
        initData()
        return view
    }

    private fun initData() {
        val tasks = ArrayList<Task>()
        for (i in 0..6) {
            for (j in 0..23) {
                val task = Task()
                task.index = i
                task.start = j.toFloat()
                task.end = j + 0.8f
                task.taskName += "$i-$j"
                tasks.add(task)
            }
        }
        scheduleLayout.post {
            scheduleLayout.setTask(tasks)
        }
    }

    private fun initTopBar() {
        mTopBar.addLeftBackImageButton().onClick { popBackStack() }
        mTopBar.setTitle(mQDItemDescription.name)
    }
}

