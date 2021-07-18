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

package com.qmuiteam.qmuidemo.fragment.components;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.qmuiteam.qmui.animation.Rotate3dAnimation;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(widgetClass = Rotate3dAnimation.class, iconRes = R.mipmap.icon_grid_span)
public class QD3DRotationFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.bookCover)
    ImageView bookCover;


    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_3d_rotation, null);
        ButterKnife.bind(this, rootView);
        initTopBar();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyRotation(0, -180);
                retuens = true;
            }
        });
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });
        QDItemDescription qdItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        mTopBar.setTitle(qdItemDescription.getName());
    }

    @Override
    protected void popBackStack() {
        super.popBackStack();
    }


    boolean retuens;

    private void applyRotation(float start, float end) {
        // 计算中心点
        final float centerX = 0;
        final float centerY = bookCover.getHeight() / 2.0f;

        final Rotate3dAnimation rotation = new Rotate3dAnimation(getActivity(), start, end, centerX, centerY, 2f, true);
        rotation.setDuration(1500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());

        rotation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (retuens) {
                    retuens = false;
                    applyRotation(-180, 0);
                }
            }
        });
        bookCover.startAnimation(rotation);
    }
}
