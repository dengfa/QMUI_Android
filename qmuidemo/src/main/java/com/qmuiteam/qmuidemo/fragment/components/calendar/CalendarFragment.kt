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
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.calendar.add
import com.qmuiteam.qmui.calendar.format
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription
import java.util.*

/**
 * 知识点：
 * 1.无限循环ViewPager
 * 2.ViewPager2的用法
 *
 */
@Widget(name = "Calendar", iconRes = R.mipmap.icon_grid_status_bar_helper)
class CalendarFragment : BaseFragment() {

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout


    @BindView(R.id.vpSchedule)
    internal lateinit var vpSchedule: ViewPager2

    private lateinit var mQDItemDescription: QDItemDescription


    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_calendar, null)
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
        vpSchedule.adapter = WeekFragmentPageAdapter(this)
        vpSchedule.setCurrentItem(DEFAULT_COUNT / 2, false)
    }
}

const val DEFAULT_COUNT = 500

class WeekFragmentPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return DEFAULT_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        val days = (position - DEFAULT_COUNT / 2) * 7
        val curTimeStamp = Date().add(days).time
        Log.d("vincent", "createFragment position $position ${Date().format()} + $days -> ${Date(curTimeStamp).format()}")
        return CalendarWeekFragment.newInstance(curTimeStamp)
    }
}

