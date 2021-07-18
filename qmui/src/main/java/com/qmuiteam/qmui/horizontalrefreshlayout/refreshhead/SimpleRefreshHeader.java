package com.qmuiteam.qmui.horizontalrefreshlayout.refreshhead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.horizontalrefreshlayout.RefreshHeader;

public class SimpleRefreshHeader implements RefreshHeader {

    private final Context context;
    private ProgressBar loadingView;

    public SimpleRefreshHeader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(ViewGroup container) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_refresh_header, container, false);
        loadingView = view.findViewById(R.id.loadingView);
        return view;
    }

    @Override
    public void onStart(int dragPosition, View refreshHead) {
    }

    @Override
    public void onDragging(float distance, float percent, View refreshHead) {
        loadingView.setRotation(360 * percent);
    }

    @Override
    public void onReadyToRelease(View refreshHead) {
    }

    @Override
    public void onRefreshing(View refreshHead) {
        RotateAnimation rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(2000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        loadingView.startAnimation(rotateAnimation);
        rotateAnimation.start();
    }

    @Override
    public void onStop(View refreshHead) {
        loadingView.clearAnimation();
    }
}
