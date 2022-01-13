package com.qmuiteam.qmuidemo.fragment.components.calendar

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.res
import com.qmuiteam.qmui.calendar.CalendarScheduleView
import com.qmuiteam.qmui.calendar.format
import com.qmuiteam.qmui.calendar.isSameDay
import com.qmuiteam.qmuidemo.R
import java.util.*

private const val ARG_DAY_TIME_STAMP = "arg_day_time_stamp"

class CalendarWeekFragment : Fragment() {
    private var dayTimeStamp: Long = 0

    @BindView(R.id.weekView)
    internal lateinit var weekView: CalendarScheduleView

    @BindView(R.id.rcWeekHeader)
    internal lateinit var rcWeekHeader: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dayTimeStamp = it.getLong(ARG_DAY_TIME_STAMP)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar_week, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(firstDayOfWeekTimeStamp: Long) =
                CalendarWeekFragment().apply {
                    arguments = Bundle().apply {
                        putLong(ARG_DAY_TIME_STAMP, firstDayOfWeekTimeStamp)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalendar()
    }

    private fun initCalendar() {
        val days = arrayListOf<Calendar>()
        val firstOfWeek = getFirstOfWeek(Date(dayTimeStamp))
        days.add(firstOfWeek)
        Log.d("vincent", "getFirstOfWeek $firstOfWeek")
        for (index in 1..6) {
            val calendar = Calendar.getInstance()
            calendar.time = firstOfWeek.time
            calendar.add(Calendar.DAY_OF_MONTH, index)
            days.add(calendar)
        }

        rcWeekHeader.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val weekHeaderAdapter = WeekHeaderAdapter()
        rcWeekHeader.adapter = weekHeaderAdapter
        weekHeaderAdapter.setData(days)
        weekView.setFirstOfWeekTimeStamp(firstOfWeek.timeInMillis)
    }


    private fun getFirstOfWeek(date: Date): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, 1)
        return calendar
    }
}

class WeekHeaderAdapter : RecyclerView.Adapter<WeekHeaderAdapter.ViewHolder>() {
    private val days = arrayListOf<Calendar>()
    private val weekStr = arrayListOf("日", "一", "二", "三", "四", "五", "六")
    private val currentDay by lazy {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar
    }

    fun setData(data: List<Calendar>) {
        days.clear()
        days.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWeek: TextView = itemView.findViewById(R.id.tvWeek)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val todayIndicator: View = itemView.findViewById(R.id.todayIndicator)
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

        Log.d("vincent", "calendar ${calendar.time.format()}  currentDay ${currentDay.time.format()}  isSameDay ${calendar.isSameDay(currentDay)}")
        if (calendar.isSameDay(currentDay)) {
            holder.tvWeek.setTextColor(res.getColor(R.color.app_color_blue))
            holder.tvDate.setTextColor(res.getColor(R.color.app_color_blue))
            holder.todayIndicator.visibility = View.VISIBLE
        } else {
            holder.tvWeek.setTextColor(res.getColor(R.color.qmui_config_color_black))
            holder.tvDate.setTextColor(res.getColor(R.color.qmui_config_color_black))
            holder.todayIndicator.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return days.size
    }
}