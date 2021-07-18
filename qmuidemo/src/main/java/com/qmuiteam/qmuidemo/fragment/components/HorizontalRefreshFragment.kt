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
package com.qmuiteam.qmuidemo.fragment.components

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.horizontalrefreshlayout.HorizontalRefreshLayout
import com.qmuiteam.qmui.horizontalrefreshlayout.RefreshCallBack
import com.qmuiteam.qmui.horizontalrefreshlayout.refreshhead.SimpleRefreshHeader
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.fragment.components.tablayout.ui.SimpleCardFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription
import java.util.ArrayList

@Widget(name = "HorizontalRefresh", iconRes = R.mipmap.icon_grid_anim_list_view)
class HorizontalRefreshFragment : BaseFragment() {

    private val mTitles = arrayOf("热门", "iOS", "Android", "前端", "后端", "设计", "工具资源")
    private val mFragments = ArrayList<Fragment>()

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout

    private lateinit var mQDItemDescription: QDItemDescription

    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_horizontal_refresh, null)
        ButterKnife.bind(this, view)
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.javaClass)
        initTopBar()
        initView(view)
        return view
    }

    private fun initTopBar() {
        mTopBar.addLeftBackImageButton().onClick { popBackStack() }
        mTopBar.setTitle(mQDItemDescription.name)
    }

    private fun initView(view: View) {
        val vpRefreshLayout = view.findViewById<HorizontalRefreshLayout>(R.id.vpRefreshLayout)
        vpRefreshLayout.setRefreshMode(HorizontalRefreshLayout.MODE_UNDER_FOLLOW_DRAG)
        vpRefreshLayout.setRefreshHeader(SimpleRefreshHeader(context), HorizontalRefreshLayout.START)
        vpRefreshLayout.setRefreshHeader(SimpleRefreshHeader(context), HorizontalRefreshLayout.END)
        vpRefreshLayout.setRefreshCallback(object : RefreshCallBack {
            override fun onLeftRefreshing() {
                Log.d("vincent", "onLeftRefreshing")
                vpRefreshLayout?.postDelayed({
                    vpRefreshLayout.onRefreshComplete()
                }, 2000)
            }

            override fun onRightRefreshing() {
                Log.d("vincent", "onRightRefreshing")
                vpRefreshLayout?.postDelayed({
                    vpRefreshLayout.onRefreshComplete()
                }, 2000)
            }
        })


        mFragments.clear()
        for (title in mTitles) {
            mFragments.add(SimpleCardFragment.getInstance(title))
        }

        val vpContent = view.findViewById<ViewPager>(R.id.vpContent)
        val mAdapter: MyPagerAdapter = MyPagerAdapter(childFragmentManager)
        vpContent.adapter = mAdapter
    }

    inner class MyPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mTitles[position]
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }
    }
}