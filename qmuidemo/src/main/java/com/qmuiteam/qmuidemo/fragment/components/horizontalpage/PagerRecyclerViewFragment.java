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

package com.qmuiteam.qmuidemo.fragment.components.horizontalpage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.qmuiteam.qmui.widget.customRecyclerView.pagerRecyclerView.DividerItemDecoration;
import com.qmuiteam.qmui.widget.customRecyclerView.pagerRecyclerView.HorizontalPageLayoutManager;
import com.qmuiteam.qmui.widget.customRecyclerView.pagerRecyclerView.PagingItemDecoration;
import com.qmuiteam.qmui.widget.customRecyclerView.pagerRecyclerView.PagingScrollHelper;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.components.horizontalpage.adapter.MyAdapter;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 分页滑动的RecyclerView ->  ViewPager效果
 */
@Widget(widgetClass = HorizontalPageLayoutManager.class, iconRes = R.mipmap.icon_grid_span)
public class PagerRecyclerViewFragment extends BaseFragment implements PagingScrollHelper.onPageChangeListener, View.OnClickListener {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    MyAdapter myAdapter;

    @BindView(R.id.tv_title)
    TextView tv_title;

    PagingScrollHelper scrollHelper = new PagingScrollHelper();

    @BindView(R.id.rg_layout)
    RadioGroup rg_layout;

    @BindView(R.id.btn_update)
    Button btnUpdate;

    @BindView(R.id.tv_page_total)
    TextView tv_page_total;

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pager_rv, null);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        rg_layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchLayout(checkedId);
            }
        });
        btnUpdate.setOnClickListener(this);
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        scrollHelper.setUpRecycleView(recyclerView);
        scrollHelper.setOnPageChangeListener(this);
        switchLayout(R.id.rb_horizontal_page);
        recyclerView.setHorizontalScrollBarEnabled(true);
        //获取总页数,采用这种方法才能获得正确的页数。否则会因为RecyclerView.State 缓存问题，页数不正确。
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                tv_page_total.setText("共" + scrollHelper.getPageCount() + "页");
            }
        });
    }

    private RecyclerView.ItemDecoration lastItemDecoration = null;
    private HorizontalPageLayoutManager horizontalPageLayoutManager = null;
    private HorizontalPageLayoutManager hLinearLayoutManager = null;
    private LinearLayoutManager vLinearLayoutManager = null;
    private PagingItemDecoration hDividerItemDecoration = null;
    private DividerItemDecoration vDividerItemDecoration = null;
    private PagingItemDecoration pagingItemDecoration = null;

    private void init() {
        hLinearLayoutManager = new HorizontalPageLayoutManager(1, 3);
        hDividerItemDecoration = new PagingItemDecoration(getActivity(), hLinearLayoutManager);

        vDividerItemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        vLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        horizontalPageLayoutManager = new HorizontalPageLayoutManager(3, 4);
        pagingItemDecoration = new PagingItemDecoration(getActivity(), horizontalPageLayoutManager);

    }

    private void switchLayout(int checkedId) {
        RecyclerView.LayoutManager layoutManager = null;
        RecyclerView.ItemDecoration itemDecoration = null;
        switch (checkedId) {
            case R.id.rb_horizontal_page:
                layoutManager = horizontalPageLayoutManager;
                itemDecoration = pagingItemDecoration;
                break;
            case R.id.rb_vertical_page:
                layoutManager = vLinearLayoutManager;
                itemDecoration = vDividerItemDecoration;
                break;
            case R.id.rb_vertical_page2:
                layoutManager = hLinearLayoutManager;
                itemDecoration = hDividerItemDecoration;
                break;
        }
        if (layoutManager != null) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.removeItemDecoration(lastItemDecoration);
            recyclerView.addItemDecoration(itemDecoration);
            scrollHelper.updateLayoutManger();
            scrollHelper.scrollToPosition(2);
            lastItemDecoration = itemDecoration;
        }


    }


    @Override
    public void onPageChange(int index) {
        tv_title.setText("第" + (index + 1) + "页");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_update:
                updateData();
                break;
        }
    }

    private void updateData() {
        myAdapter.updateData();
        myAdapter.notifyDataSetChanged();
        //滚动到第一页
        scrollHelper.scrollToPosition(0);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                tv_page_total.setText("共" + scrollHelper.getPageCount() + "页");
            }
        });
    }
}
