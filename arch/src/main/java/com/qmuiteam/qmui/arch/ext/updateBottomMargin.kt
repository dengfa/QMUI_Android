package com.qmuiteam.qmui.arch.ext

import android.view.View
import android.widget.ScrollView
import com.qmuiteam.qmui.arch.utils.UIUtils

/**
 * 1.update view margin
 */

fun View?.updateBottomMargin(marginBottomInPx: Int) {
    this?.let {
        val keep = UIUtils.LAYOUT_PARAMS_KEEP_OLD
        UIUtils.updateLayoutMargin(it, keep, keep, keep, marginBottomInPx)
    }
}

fun View?.updateTopMargin(marginTopInPx: Int) {
    this?.let {
        val keep = UIUtils.LAYOUT_PARAMS_KEEP_OLD
        UIUtils.updateLayoutMargin(it, keep, marginTopInPx, keep, keep)
    }
}

fun View?.updateTopPadding(paddingTopInPx: Int) {
    this?.let {
        it.setPadding(it.paddingLeft, paddingTopInPx, it.paddingRight, it.paddingBottom)
    }
}

fun View?.updateLeftMargin(marginLeftInPx: Int) {
    this?.let {
        val keep = UIUtils.LAYOUT_PARAMS_KEEP_OLD
        UIUtils.updateLayoutMargin(it, marginLeftInPx, keep, keep, keep)
    }
}

fun View?.updateRightMargin(marginRightInPx: Int) {
    this?.let {
        val keep = UIUtils.LAYOUT_PARAMS_KEEP_OLD
        UIUtils.updateLayoutMargin(it, keep, keep, marginRightInPx, keep)
    }
}

fun ScrollView.smoothScrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, delta)
}

fun ScrollView.smoothScrollToTop() {
    this.smoothScrollTo(0, 0)
}