package com.qmuiteam.qmui.arch.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class UIUtils {

    private static final float FLOAT_BIAS = 0.5f;

    // 防止被继承
    private UIUtils() {
    }

    /**
     * 显示Toast的hook
     */
    public interface ToastHook {
        /**
         * 显示Toast的时候调用
         *
         * @param context  Context
         * @param iconId   icon的id
         * @param message  toast内容
         * @param duration 时长
         * @param gravity  {@link Gravity}
         * @return 如果hook处理了toast，返回true，否则返回false
         */
        boolean showToast(Context context, int iconId, CharSequence message, long duration, int gravity);
    }

    public static final char ELLIPSIS_CHAR = '\u2026';
    public static final int LAYOUT_PARAMS_KEEP_OLD = -3;

    public static final boolean API_ET_20 = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    private static ToastHook sToastHook;

    /**
     * 设置Toast的hook
     */
    public static void setToastHook(ToastHook toastHook) {
        sToastHook = toastHook;
    }

    public static void displayToast(Context context, String m) {
        displayToast(context, 0, m);
    }

    public static void displayToast(Context context, int resourceId) {
        displayToast(context, context.getString(resourceId));
    }

    public static void displayToastWithIcon(Context context, int iconId, int textId) {
        displayToast(context, iconId, context.getString(textId));
    }

    public static void displayToastWithIcon(Context context, int iconId, String text) {
        displayToast(context, iconId, text);
    }

    public static void displayToast(Context context, int resourceId, int position) {
        displayToast(context, context.getString(resourceId), position);
    }

    public static void displayToast(Context context, String text, int position) {
        displayToastInternal(context, 0, text, Toast.LENGTH_SHORT, position);
    }

    public static void displayToast(Context context, int iconId, String text) {
        displayToastInternal(context, iconId, text, Toast.LENGTH_SHORT, Gravity.CENTER);
    }

    public static void displayLongTimeToast(Context context, int iconId, int resourceId) {
        if (context == null) {
            return;
        }
        String message = context.getString(resourceId);
        if (StringUtils.isEmpty(message)) {
            return;
        }
        displayToastInternal(context, iconId, message, Toast.LENGTH_LONG, Gravity.CENTER);
    }

    private static void displayToastInternal(final Context context, final int iconId, final String message, final int duration, final int gravity) {
        if (context == null || StringUtils.isEmpty(message)) {
            return;
        }
        if (!isInUIThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    displayToastInternal(context, iconId, message, duration, gravity);
                }
            });
        } else {
            if (sToastHook != null && sToastHook.showToast(context, iconId, message, duration, gravity)) {
                return;
            }
            if (context instanceof ICustomToast) {
                if (duration == Toast.LENGTH_LONG) {
                    ((ICustomToast) context).showCustomLongToast(iconId, message);
                } else {
                    ((ICustomToast) context).showCustomToast(iconId, message, duration == Toast.LENGTH_SHORT ? ICustomToast.LENGTH_SHORT : duration, gravity);
                }
            } else {
                try {
                    Toast toast = Toast.makeText(context, message, duration);
                    if (toast != null) {
                        toast.setGravity(gravity, 0, 0);
                        toast.show();
                    }
                } catch (Exception e) {
                    //Logger.throwException(e);
                }
            }
        }
    }

    public static float sp2px(Context context, float sp) {
        if (context != null) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        }
        return 0;
    }


    public static float dip2Px(Context context, float dipValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return dipValue * scale + FLOAT_BIAS;
        }
        return 0;
    }

    public static int px2dip(Context context, float pxValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + FLOAT_BIAS);
        }
        return 0;
    }

    public static void expandClickRegion(final View view, final int left, final int top, final int right,
                                         final int bottom) {
        view.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                view.getHitRect(delegateArea);
                delegateArea.top += top;
                delegateArea.bottom += bottom;
                delegateArea.left += left;
                delegateArea.right += right;
                TouchDelegate expandedArea = new TouchDelegate(delegateArea, view);
                // give the delegate to an ancestor of the view we're delegating
                // the area to
                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(expandedArea);
                }
            }
        });
    }


    public static void setViewBackgroundWithPadding(View view, int resid) {
        if (view == null) {
            return;
        }
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();
        view.setBackgroundResource(resid);
        view.setPadding(left, top, right, bottom);
    }

    public static void setViewBackgroundWithPadding(View view, Resources res, int colorid) {
        if (view == null) {
            return;
        }
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();
        view.setBackgroundColor(res.getColor(colorid));
        view.setPadding(left, top, right, bottom);
    }

    @SuppressWarnings("deprecation")
    public static void setViewBackgroundWithPadding(View view, Drawable drawable) {
        if (view == null) {
            return;
        }
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();
        view.setBackgroundDrawable(drawable);
        view.setPadding(left, top, right, bottom);
    }

    private static final int MAX_COUNT = 10000;

    public static String getDisplayCount(int count) {
        if (count > MAX_COUNT) {
            String result = String.format(Locale.getDefault(), "%.1f", 1.0 * count / MAX_COUNT);
            if ('0' == result.charAt(result.length() - 1)) {
                return result.substring(0, result.length() - 2) + "万";
            } else {
                return result + "万";
            }
        }
        return String.valueOf(count);
    }

    public static int getScreenWidth(Context context) {
        if (context == null) {
            return 0;
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (dm == null) ? 0 : dm.widthPixels;
    }

    public static int getRatioOfScreen(Context context, float ratio) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null) {
            return 0;
        }
        return (int) (dm.widthPixels * ratio);
    }

    public static boolean isInUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void assertInUIThread() {
        boolean isInUIThread = Looper.myLooper() == Looper.getMainLooper();
        if (isInUIThread) {
            return;
        }
        //Logger.alertErrorInfo("not in UI thread");
    }

    public static int getScreenHeight(Context context) {
        if (context == null) {
            return 0;
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        return (dm == null) ? 0 : dm.heightPixels;
    }

    private static String sScreenResolution = StringUtils.EMPTY;

    public static String getScreenResolution(Context context) {
        if (StringUtils.isEmpty(sScreenResolution)) {
            if (context != null) {
                int width = getScreenWidth(context);
                int height = getScreenHeight(context);
                if (width > 0 && height > 0) {
                    sScreenResolution = width + "*" + height;
                }
            }
        }
        return sScreenResolution;
    }

    private static int mDpi = -1;

    public static int getDpi(Context context) {
        if (mDpi == -1) {
            if (context != null) {
                mDpi = context.getApplicationContext().getResources().getDisplayMetrics().densityDpi;
            }
        }
        return mDpi;
    }

    private static final int MAX_WIDTH = 1375;
    private static final int BURY_WIDTH_DP = 20;

    public static int getDiggBuryWidth(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenWidth = screenWidth * MAX_WIDTH / MAX_COUNT + (int) (UIUtils.dip2Px(context, BURY_WIDTH_DP));
        return screenWidth;
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static boolean visibilityValid(int visiable) {
        return visiable == View.VISIBLE || visiable == View.GONE || visiable == View.INVISIBLE;
    }

    public static void setViewVisibility(View v, int visiable) {
        if (v == null || v.getVisibility() == visiable || !visibilityValid(visiable)) {
            return;
        }
        v.setVisibility(visiable);
    }

    public static boolean isViewVisible(View view) {
        if (view == null) {
            return false;
        }

        return view.getVisibility() == View.VISIBLE;
    }

    /**
     * get location of view relative to given upView. get center location if
     * getCenter is true.
     */
    public static void getLocationInUpView(View upView, View view, int[] loc, boolean getCenter) {
        if (upView == null || view == null || loc == null || loc.length < 2) {
            return;
        }
        upView.getLocationInWindow(loc);
        int x1 = loc[0];
        int y1 = loc[1];
        view.getLocationInWindow(loc);
        int x2 = loc[0] - x1;
        int y2 = loc[1] - y1;
        if (getCenter) {
            int w = view.getWidth();
            int h = view.getHeight();
            x2 = x2 + w / 2;
            y2 = y2 + h / 2;
        }
        loc[0] = x2;
        loc[1] = y2;
    }

    public static void updateLayout(View view, int w, int h) {
        if (view == null) {
            return;
        }
        LayoutParams params = view.getLayoutParams();
        if (params == null || (params.width == w && params.height == h)) {
            return;
        }
        if (w != LAYOUT_PARAMS_KEEP_OLD) {
            params.width = w;
        }
        if (h != LAYOUT_PARAMS_KEEP_OLD) {
            params.height = h;
        }
        view.setLayoutParams(params);
    }

    public static void updateLayoutMargin(View view, int l, int t, int r, int b) {
        if (view == null) {
            return;
        }
        LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }
        if (params instanceof ViewGroup.MarginLayoutParams) {
            updateMargin(view, (ViewGroup.MarginLayoutParams) params, l, t, r, b);
        }
    }

    private static void updateMargin(View view, ViewGroup.MarginLayoutParams params, int l, int t, int r, int b) {
        if (view == null || params == null) {
            return;
        }

        if (params.leftMargin == l && params.topMargin == t && params.rightMargin == r && params.bottomMargin == b) {
            return;
        }

        if (l != LAYOUT_PARAMS_KEEP_OLD) {
            params.leftMargin = l;
        }
        if (t != LAYOUT_PARAMS_KEEP_OLD) {
            params.topMargin = t;
        }
        if (r != LAYOUT_PARAMS_KEEP_OLD) {
            params.rightMargin = r;
        }
        if (b != LAYOUT_PARAMS_KEEP_OLD) {
            params.bottomMargin = b;
        }
        view.setLayoutParams(params);
    }

    /**
     * @param view
     * @param topMarginInDp 单位dp
     */
    public static void setTopMargin(View view, float topMarginInDp) {
        if (view == null) {
            return;
        }
        DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMarginInDp, dm);
        updateLayoutMargin(view, LAYOUT_PARAMS_KEEP_OLD, topMargin, LAYOUT_PARAMS_KEEP_OLD, LAYOUT_PARAMS_KEEP_OLD);
    }


    /**
     * 如果传入 {@link Integer#MIN_VALUE} ，那么传入的值将会被忽略
     */
    public static void setLayoutParams(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }
        // ViewGroup.LayoutParams.FILL_PARENT 的值小于 0，所以不能简单的 if(width > 0)
        if (width != Integer.MIN_VALUE) {
            params.width = width;
        }
        if (height != Integer.MIN_VALUE) {
            params.height = height;
        }
    }

    public static void setTxtAndAdjustVisible(TextView tv, CharSequence txt) {
        if (tv == null) {
            return;
        }
        if (TextUtils.isEmpty(txt)) {
            setViewVisibility(tv, View.GONE);
        } else {
            setViewVisibility(tv, View.VISIBLE);
            tv.setText(txt);
        }
    }

    public static void setText(TextView textView, CharSequence text) {
        if (textView == null || TextUtils.isEmpty(text)) {
            return;
        }

        textView.setText(text);
    }

    public static void detachFromParent(View view) {
        if (view == null || view.getParent() == null) {
            return;
        }
        ViewParent parent = view.getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        try {
            ((ViewGroup) parent).removeView(view);
        } catch (Exception e) {
            //Logger.throwException(e);
        }
    }

    @SuppressLint("NewApi")
    public static void setViewMinHeight(View view, int minHeight) {
        if (view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && view.getMinimumHeight() == minHeight) {
            return;
        }
        view.setMinimumHeight(minHeight);
    }

    @SuppressLint("NewApi")
    public static void setTextViewMaxLines(TextView textView, int maxLines) {
        if (textView == null || maxLines <= 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && textView.getMaxLines() == maxLines) {
            return;
        }
        textView.setSingleLine(maxLines == 1);
        textView.setMaxLines(maxLines);
    }

    public static int[] getLocationInAncestor(View child, View ancestor) {
        if (child == null || ancestor == null) {
            //Logger.alertErrorInfo("invalid params: child:" + child + ",ancestor:" + ancestor);
            return null;
        }
        int[] location = new int[2];
        float[] position = new float[2];
        position[0] = position[1] = 0.0f;

        position[0] += child.getLeft();
        position[1] += child.getTop();

        boolean matched = false;
        ViewParent viewParent = child.getParent();
        while (viewParent instanceof View) {
            final View view = (View) viewParent;
            if (viewParent == ancestor) {
                matched = true;
                break;
            }
            position[0] -= view.getScrollX();
            position[1] -= view.getScrollY();

            position[0] += view.getLeft();
            position[1] += view.getTop();

            viewParent = view.getParent();
        }
        if (!matched) {
            Logger.alertErrorInfo("ancestorView:" + ancestor + " is not the ancestor of child : " + child);
            return null;
        }
        location[0] = (int) (position[0] + FLOAT_BIAS);
        location[1] = (int) (position[1] + FLOAT_BIAS);
        return location;
    }

    private static final int MASK_FF = 0xff;
    private static final int MASK_FFFF = 0xffffff;
    private static final int MASK_1000 = 0x1000000;

    public static int setColorAlpha(int color, int alpha) {
        if (alpha > MASK_FF) {
            alpha = MASK_FF;
        } else if (alpha < 0) {
            alpha = 0;
        }
        return (color & MASK_FFFF) | (alpha * MASK_1000);
    }

    public static void ellipseSingleLineStr(String str, final int maxLength, Paint paint, int ellipsisLength, EllipsisMeasureResult out) {
        if (maxLength <= ellipsisLength || StringUtils.isEmpty(str)) {
            out.ellipsisStr = "";
            out.length = 0;
            return;
        }
        int length = floatToIntBig(paint.measureText(str));
        if (length <= maxLength) {
            out.ellipsisStr = str;
            out.length = length;
            return;
        }
        int maxLengthLeft = maxLength - ellipsisLength;
        StringBuilder sb = new StringBuilder();
        int end = paint.breakText(str, 0, str.length(), true, maxLengthLeft, null);
        if (end < 1) {
            out.ellipsisStr = "";
            out.length = 0;
            return;
        }
        sb.append(str.substring(0, end));
        sb.append(ELLIPSIS_CHAR);
        out.ellipsisStr = sb.toString();
        out.length = maxLength;
    }

    private static final float FLOAT_DOT999 = 0.999f;

    public static int floatToIntBig(float value) {
        return (int) (value + FLOAT_DOT999);
    }

    public static class EllipsisMeasureResult {
        public String ellipsisStr;
        public int length;
    }

    public static EllipsisMeasureResult sTempEllipsisResult = new EllipsisMeasureResult();

    public static void requestOrienation(Activity activity, boolean landscape) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.setRequestedOrientation(landscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (landscape) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static int getIndexInParent(View view) {
        if (view == null || view.getParent() == null) {
            return -1;
        }
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            return ((ViewGroup) parent).indexOfChild(view);
        }
        return -1;
    }

    public static boolean clearAnimation(View view) {
        if (view == null || view.getAnimation() == null) {
            return false;
        }
        view.clearAnimation();
        return true;
    }


    public static void setClickListener(boolean clickable, View view, View.OnClickListener clickListener) {
        if (view == null) {
            return;
        }
        if (clickable) {
            view.setOnClickListener(clickListener);
            view.setClickable(true);
        } else {
            view.setOnClickListener(null);
            view.setClickable(false);
        }
    }

}
