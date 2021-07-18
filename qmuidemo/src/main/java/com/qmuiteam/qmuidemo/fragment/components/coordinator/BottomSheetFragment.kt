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
package com.qmuiteam.qmuidemo.fragment.components.coordinator

import android.view.LayoutInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.behavior.VpBottomSheetBehavior
import com.qmuiteam.qmui.kotlin.dip
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription

@Widget(name = "BottomSheet", iconRes = R.mipmap.icon_grid_button)
class BottomSheetFragment : BaseFragment() {

    private var behavior: VpBottomSheetBehavior<View>? = null

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout

    private lateinit var mQDItemDescription: QDItemDescription

    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_bottom_sheet, null)
        ButterKnife.bind(this, view)
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.javaClass)
        initTopBar()

        val bottomSheet: View = view.findViewById(R.id.bottomSheetContainer)
        behavior = VpBottomSheetBehavior.from(bottomSheet)
        behavior?.peekHeight = dip(400)
        behavior?.state = VpBottomSheetBehavior.STATE_COLLAPSED
        return view
    }

    private fun initTopBar() {
        mTopBar.addLeftBackImageButton().onClick { popBackStack() }
        mTopBar.setTitle(mQDItemDescription.name)
    }
}