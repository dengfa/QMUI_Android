package com.qmuiteam.qmuidemo.fragment.components.tablayout.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.qmuiteam.qmui.tablayout.CommonTabLayout;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import java.util.ArrayList;

@Widget(widgetClass = CommonTabLayout.class, iconRes = R.mipmap.icon_grid_span)
public class TabLayoutFragment extends BaseFragment {

    @BindView(R.id.vpContainer)
    ViewPager mViewPager;
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles = { "首页", "消息", "联系人", "更多" };

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tablayout, null);
        ButterKnife.bind(this, rootView);
        initTopBar();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragments.add(new TabLayoutCommonFragment());
        mFragments.add(new SegmentTabFragment());
        mFragments.add(new SlidingTabFragment());
        mViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
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
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    protected int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
