package com.qmuiteam.qmuidemo.fragment.components.tablayout.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.ButterKnife;
import com.qmuiteam.qmui.tablayout.SegmentTabLayout;
import com.qmuiteam.qmui.tablayout.listener.OnTabSelectListener;
import com.qmuiteam.qmui.tablayout.widget.MsgView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.components.tablayout.utils.ViewFindUtils;
import java.util.ArrayList;

public class SegmentTabFragment extends BaseFragment {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<Fragment> mFragments2 = new ArrayList<>();

    private String[] mTitles = { "首页", "消息" };
    private String[] mTitles_2 = { "首页", "消息", "联系人" };
    private String[] mTitles_3 = { "首页", "消息", "联系人", "更多" };
    private SegmentTabLayout mTabLayout_3;

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_segment_tab, null);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        for (String title : mTitles_3) {
            mFragments.add(SimpleCardFragment.getInstance("Switch ViewPager " + title));
        }

        for (String title : mTitles_2) {
            mFragments2.add(SimpleCardFragment.getInstance("Switch Fragment " + title));
        }

        SegmentTabLayout tabLayout_1 = ViewFindUtils.find(view, R.id.tl_1);
        SegmentTabLayout tabLayout_2 = ViewFindUtils.find(view, R.id.tl_2);
        mTabLayout_3 = ViewFindUtils.find(view, R.id.tl_3);
        SegmentTabLayout tabLayout_4 = ViewFindUtils.find(view, R.id.tl_4);
        SegmentTabLayout tabLayout_5 = ViewFindUtils.find(view, R.id.tl_5);

        tabLayout_1.setTabData(mTitles);
        tabLayout_2.setTabData(mTitles_2);
        tl_3(view);
        tabLayout_4.setTabData(mTitles_2, getActivity(), R.id.fl_change, mFragments2);
        tabLayout_5.setTabData(mTitles_3);

        //显示未读红点
        tabLayout_1.showDot(2);
        tabLayout_2.showDot(2);
        mTabLayout_3.showDot(1);
        tabLayout_4.showDot(1);

        //设置未读消息红点
        mTabLayout_3.showDot(2);
        MsgView rtv_3_2 = mTabLayout_3.getMsgView(2);
        if (rtv_3_2 != null) {
            rtv_3_2.setBackgroundColor(Color.parseColor("#6D8FB0"));
        }
    }

    private void tl_3(View view) {
        final ViewPager vp_3 = ViewFindUtils.find(view, R.id.vp_2);
        vp_3.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        mTabLayout_3.setTabData(mTitles_3);
        mTabLayout_3.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vp_3.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        vp_3.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout_3.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_3.setCurrentItem(1);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles_3[position];
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
}
