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

package com.qmuiteam.qmuidemo.fragment.components.endlessrv;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.customRecyclerView.EndlessRecyclerViewAdapter2;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.components.endlessrv.adapter.SimpleStringAdapter;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;

/**
 * 双端加载更多的RecyclerView
 */
@Widget(widgetClass = EndlessRecyclerViewAdapter2.class, iconRes = R.mipmap.icon_grid_span)
public class QDEndlessRecyclerViewFragment extends BaseFragment implements EndlessRecyclerViewAdapter2.RequestToLoadMoreListener{

    private EndlessRecyclerViewAdapter2 endlessRecyclerViewAdapter;
    private SimpleStringAdapter adapter;
    RecyclerView rv;


    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_endless_recycler_view, null);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        ArrayList<String> strings = new ArrayList<>();
        strings.addAll(Arrays.asList(
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese(),
                randomCheese()));
        adapter = new SimpleStringAdapter(layoutManager, 30, strings);
        endlessRecyclerViewAdapter = new EndlessRecyclerViewAdapter2(getActivity(), adapter, this);
        rv.setAdapter(endlessRecyclerViewAdapter);



        view.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetClicked();
            }
        });
    }

    @Override
    public void onAfterLoadMoreRequested() {
        new AsyncTask<Void, Void, List>() {
            @Override
            protected List doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Arrays.asList(
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese());
            }

            @Override
            protected void onPostExecute(List list) {
                adapter.appendItems(list);
                if (adapter.getItemCount() >= 500000) {
                    // load 100 items for demo only
                    endlessRecyclerViewAdapter.onDataReadyAfter(false);
                } else {
                    // notify the data is ready
                    endlessRecyclerViewAdapter.onDataReadyAfter(true);
                }
            }
        }.execute();
    }

    @Override
    public void onBeforeLoadMoreRequested() {
        new AsyncTask<Void, Void, List>() {
            @Override
            protected List doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Arrays.asList(
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese(),
                        randomCheese());
            }

            @Override
            protected void onPostExecute(List list) {
                adapter.insertItems(list);

                if (adapter.getItemCount() >= 500000) {
                    // load 100 items for demo only
                    endlessRecyclerViewAdapter.onDataReadyBefore(false);
                } else {
                    // notify the data is ready
                    endlessRecyclerViewAdapter.onDataReadyBefore(true);
                }
            }
        }.execute();
    }

    public void onResetClicked() {
        adapter.clear();
        endlessRecyclerViewAdapter.restartAppending();
    }

    static Random random = new Random();

    static String randomCheese() {
        return Cheeses.sCheeseStrings[random.nextInt(Cheeses.sCheeseStrings.length)];
    }

    @Override
    protected void popBackStack() {
        super.popBackStack();
    }
}
