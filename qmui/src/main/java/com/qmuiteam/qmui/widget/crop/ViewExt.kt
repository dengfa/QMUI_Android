package com.qmuiteam.qmui.widget.crop

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.TypedValue
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.qmuiteam.qmui.util.QMUIDisplayHelper

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isVisible() = (this.visibility == View.VISIBLE)

fun View.isGone() = (this.visibility == View.GONE)

fun View.isInvisible() = (this.visibility == View.INVISIBLE)


inline val Int.dpToPxInt: Int
    get() = this.dpToPxFloat.toInt()

inline val Float.dpToPxInt: Int
    get() = this.dpToPxFloat.toInt()

inline val Int.dpToPxFloat: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), QMUIDisplayHelper.appContext.resources.displayMetrics)

inline val Float.dpToPxFloat: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, QMUIDisplayHelper.appContext.resources.displayMetrics)

inline val Float.spToPxFloat: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, QMUIDisplayHelper.appContext.resources.displayMetrics)

inline val Float.spToPxInt: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, QMUIDisplayHelper.appContext.resources.displayMetrics).toInt()

inline val Int.spToPxFloat: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), QMUIDisplayHelper.appContext.resources.displayMetrics)

inline val Int.spToPxInt: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), QMUIDisplayHelper.appContext.resources.displayMetrics).toInt()

inline val Int.pxToDpFloat: Float
    get() = (this / QMUIDisplayHelper.appContext.resources.displayMetrics.density)

inline val Int.pxToDpInt: Int
    get() = (this / QMUIDisplayHelper.appContext.resources.displayMetrics.density).toInt()

inline val Int.pxTpSpInt: Int
    get() = (this / QMUIDisplayHelper.appContext.resources.displayMetrics.scaledDensity + 0.5f).toInt()

inline val Float.pxTpSpInt: Int
    get() = (this / QMUIDisplayHelper.appContext.resources.displayMetrics.scaledDensity + 0.5f).toInt()

inline val Int.pxTpSpFloat: Float
    get() = (this / QMUIDisplayHelper.appContext.resources.displayMetrics.scaledDensity + 0.5f)

inline val Float.pxTpSpFloat: Float
    get() = (this / QMUIDisplayHelper.appContext.resources.displayMetrics.scaledDensity + 0.5f)


fun View.allChildView(): ArrayList<View> {
    val allChild = ArrayList<View>()
    if (this is ViewGroup) {
        val vp = this
        for (i in 0 until vp.childCount) {
            val child = vp.getChildAt(i)
            allChild.add(child)
            //再次 调用本身（递归）
            allChild.addAll(child.allChildView()!!)
        }
    }
    return allChild
}

fun View.allChildViewIncludeSelf(): List<View> {
    return allChildView().apply {
        add(this@allChildViewIncludeSelf)
    }
}

open class SimpleLayoutChangedListener : View.OnLayoutChangeListener {
    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        onLayoutChange()
    }

    open fun onLayoutChange() {

    }
}

fun Activity.isScreenPortrait(): Boolean {
    return this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Activity.isScreenLandscape(): Boolean {
    return !this.isScreenPortrait()
}

/**
 * Activity是否设置了Sensor属性
 */
fun FragmentActivity.isOpenScreenSensor(): Boolean {
    return this.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR
}

fun Context.isScreenPortrait(): Boolean {
    return this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Context.isScreenLandscape(): Boolean {
    return !this.isScreenPortrait()
}

/**
 * 保证角度始终是正数
 */
fun Int.safeDegree(): Int {
    var tempValue = this
    while (tempValue >= 360) {
        tempValue -= 360;
    }
    while (tempValue < 0) {
        tempValue += 360;
    }
    return tempValue % 360
}

private fun isPortrait(activity: Activity): Boolean {
    val rotation = activity.windowManager.defaultDisplay.rotation
    return rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180
}

@SuppressLint("SourceLockedOrientationActivity")
fun lockScreen(context: Context) {
    if (context is Activity) {
        if (isPortrait(context)) {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        } else {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

}

fun unLockScreen(context: Context) {
    if (context is Activity) {
        context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}
/**
 * 旋转动画
 */
fun View.rotateByAnim(start: Int, end: Int, duration: Long) {
    animate().cancel()
    rotation = start.toFloat()
    if (start == end) {
        rotation = start.toFloat()
    } else {
        animate().rotation(end.toFloat()).setDuration(duration).start()
    }
}

/**
 * 按压缩放 缩放比例为95%
 */
@SuppressLint("ClickableViewAccessibility")
fun View.bounceWhenPressed(onClick: View.OnClickListener?) {
    setOnTouchListener { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                zoomOutAnimation(
                    this, 0.95f
                )
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                zoomInAnimation(this)
            }
        }
        false
    }
    setOnClickListener(singleOnClickListener {
        onClick?.onClick(this)
    })
}

/**
 * 按压缩小
 */
private fun zoomOutAnimation(view: View?, scaleSize: Float) {
    if (view == null) return
    view.scaleX = 1f
    view.scaleY = 1f
    view.animate().cancel()
    view.animate().scaleX(scaleSize).scaleY(scaleSize)
        .setInterpolator(CubicBezierInterpolator(0.42, 0.00, 0.58, 1.00)).setDuration(300)
        .setStartDelay(0).start()
}

/**
 * 松手放大
 */
private fun zoomInAnimation(view: View?) {
    if (view == null) return
    view.animate().cancel()
    view.animate().scaleX(1f).scaleY(1f)
        .setInterpolator(CubicBezierInterpolator(0.42, 0.00, 0.58, 1.00)).setDuration(300)
        .setStartDelay(0).start()
}

operator fun View.plus(view: View): MutableList<View> {
    return mutableListOf(this, view)
}

operator fun MutableList<View>.plus(view: View): MutableList<View> {
    add(view)
    return this
}




