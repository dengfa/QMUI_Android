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

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.crop.CropImageView
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription

@Widget(name = "PhotoCrop", iconRes = R.mipmap.icon_grid_scroll_animator)
class PhotoCropFragment : BaseFragment() {

    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout

    @BindView(R.id.crop_image_view)
    internal lateinit var cropImageView: CropImageView

    private lateinit var mQDItemDescription: QDItemDescription

    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_photo_crop, null)
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
        cropImageView.setImageBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.crop_test
            )
        )
        cropImageView.setCropRect(arrayListOf(Rect(0, 0, 300, 300)))
    }
}