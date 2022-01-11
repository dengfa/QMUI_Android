/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qmuiteam.qmuidemo.fragment.components.calendar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.calendar.WeekTask
import com.qmuiteam.qmui.calendar.WeekView
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.fragment.components.calendar.WeekHeaderAdapter.ViewHolder
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription
import java.util.Calendar
import java.util.Date

@Widget(name = "CalendarWeekView", iconRes = R.mipmap.icon_grid_status_bar_helper)
class WeekViewFragment : BaseFragment() {

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout

    @BindView(R.id.weekView)
    internal lateinit var weekView: WeekView

    @BindView(R.id.rcWeekHeader)
    internal lateinit var rcWeekHeader: RecyclerView


    private lateinit var mQDItemDescription: QDItemDescription


    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_calendar_week_view, null)
        ButterKnife.bind(this, view)
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.javaClass)
        initTopBar()
        initCalendar()
        return view
    }

    private fun initTopBar() {
        mTopBar.addLeftBackImageButton().onClick { popBackStack() }
        mTopBar.setTitle(mQDItemDescription.name)
    }

    private fun initCalendar() {
        val days = arrayListOf<Calendar>()
        val firstOfWeek = getFirstOfWeek(Date())
        days.add(firstOfWeek)
        Log.d("vincent", "getFirstOfWeek $firstOfWeek")
        for (index in 1..6) {
            val calendar = Calendar.getInstance()
            calendar.time = firstOfWeek.time
            calendar.add(Calendar.DAY_OF_MONTH, index)
            days.add(calendar)
        }

        //todo
        val tasks = arrayListOf<WeekTask>() //2022.1.11 8:00 - 8:45
        tasks.add(WeekTask(1641859200, 1641861900, "task1")) //2022.1.15 8:45 - 12:00
        tasks.add(WeekTask(1642207500, 1642219200, "task2"))
        weekView.setTasks(tasks, firstOfWeek.timeInMillis)

        rcWeekHeader.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val weekHeaderAdapter = WeekHeaderAdapter()
        rcWeekHeader.adapter = weekHeaderAdapter
        weekHeaderAdapter.setData(days)
    }


    private fun getFirstOfWeek(date: Date): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, 1)
        return calendar
    }
}

class WeekHeaderAdapter : RecyclerView.Adapter<ViewHolder>() {
    private val days = arrayListOf<Calendar>()
    private val weekStr = arrayListOf("日", "一", "二", "三", "四", "五", "六")

    fun setData(data: List<Calendar>) {
        days.clear()
        days.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWeek: TextView = itemView.findViewById(R.id.tvWeek)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemWidth = parent.width / 7
        Log.d("vincent", "itemWidth $itemWidth")
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_week_header, parent, false)
        itemView.layoutParams.width = itemWidth
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendar = days[position]
        holder.tvWeek.text = weekStr[(calendar.get(Calendar.DAY_OF_WEEK) - 1) % weekStr.size]
        holder.tvDate.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    override fun getItemCount(): Int {
        return days.size
    }
}