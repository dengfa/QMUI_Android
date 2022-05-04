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
package com.qmuiteam.qmuidemo.fragment.components.guide

import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.toPx
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.guide.EditorGuideView
import com.qmuiteam.qmui.widget.guide.GuideLiteView
import com.qmuiteam.qmui.widget.guide.TipsView
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription

@Widget(name = "Guide", iconRes = R.mipmap.icon_grid_anim_list_view)
class GuideFragment : BaseFragment() {

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout
    private var btnTest: Button? = null
    private var btnTest2: Button? = null
    private var btnTest3: Button? = null
    private var guideView: GuideLiteView? = null
    private var guideView2: GuideLiteView? = null
    private var guideView3: EditorGuideView? = null

    private lateinit var mQDItemDescription: QDItemDescription

    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_guide, null)
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

    override fun onResume() {
        super.onResume()
        setGuide()
    }

    private fun initView(view: View) {
        btnTest = view.findViewById<View>(R.id.btn_test) as Button
        btnTest2 = view.findViewById<View>(R.id.btn_test2) as Button
        btnTest3 = view.findViewById<View>(R.id.btn_test3) as Button
        targets = arrayListOf<View>(btnTest!!, btnTest2!!, btnTest3!!)
    }

    var targets = arrayListOf<View>()

    private fun setGuide() {
        // 使用图片
        val iv = ImageView(context)
        iv.setImageResource(R.drawable.emoji_1f414)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        iv.layoutParams = params

        // 使用文字
        val tv = TextView(context)
        tv.text = "欢迎使用"
        tv.setTextColor(resources.getColor(R.color.qmui_config_color_white))
        tv.textSize = 30f
        tv.gravity = Gravity.CENTER

        // 使用文字
        val tips = TipsView(requireContext())
        tips.setTips(
            "title",
            "tips",
            "1/4",
            "guide/tips_order.json",
            "pre",
            "next",
            { Toast.makeText(context, "pre", Toast.LENGTH_SHORT).show() },
            { Toast.makeText(context, "next", Toast.LENGTH_SHORT).show() }
        )


        guideView = GuideLiteView.Builder
            .newInstance(requireContext())
            .setTargetView(btnTest3) //设置目标
            .setCustomGuideView(tips)
            .setBgColor(ContextCompat.getColor(requireContext(), R.color.cardview_shadow_start_color))
            .setOnclickListener(object : GuideLiteView.OnClickCallback {
                override fun onClickedGuideView() {
                    guideView?.hide()
                    guideView2?.show()
                }
            })
            .build()


        guideView2 = GuideLiteView.Builder
            .newInstance(requireContext())
            .setTargetView(btnTest)
            .setCustomGuideView(tips)
            .setBgColor(ContextCompat.getColor(requireContext(), R.color.cardview_shadow_start_color))
            .setOnclickListener(object : GuideLiteView.OnClickCallback {
                override fun onClickedGuideView() {
                    guideView2?.hide()
                }
            })
            .build()

        //guideView?.show()

        guideView3 = EditorGuideView(requireContext())
        guideView3?.show(targets, arrayListOf(Point(200.toPx(),200.toPx()), Point(400.toPx(), 400.toPx()), Point(600.toPx(), 600.toPx())))
    }
/*
    private fun resetTarget() {
        count++
        val tips = TipsView(requireContext())
        tips.setTips(
            "title",
            "tips",
            "$count",
            "tips_order.json",
            "pre",
            "next",
            { Toast.makeText(context, "pre", Toast.LENGTH_SHORT).show() },
            { resetTarget() }
        )
        val targetView = targets[count % (targets.size)]
        val location = IntArray(2)
        targetView.getLocationInWindow(location)
        guideView3?.resetTarget(targetView, Point(location[0], location[1]), tips)
    }*/

    var count = 0

    override fun onStop() {
        super.onStop()
        guideView?.hide()
        guideView2?.hide()
        guideView3?.hide()
    }
}