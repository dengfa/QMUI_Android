package com.qmuiteam.qmui.horizontalrefreshlayout;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

public interface RefreshHeader {

    /**
     * @param dragPosition HorizontalRefreshLayout.START or HorizontalRefreshLayout.END
     */
    void onStart(int dragPosition, View refreshHead);

    /**
     * @param distance
     */
    void onDragging(float distance, float percent, View refreshHead);

    void onReadyToRelease(View refreshHead);

    @NonNull
    View getView(ViewGroup container);

    void onRefreshing(View refreshHead);

    void onStop(View refreshHead);
}
