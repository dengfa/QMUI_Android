package com.qmuiteam.qmui

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

val appContext: Application = ApplicationHolder.application
val res: Resources = appContext.resources

inline val Number.dpF
    get() = toPxF()

inline val Number.dp
    get() = toPx()

fun Number.toPx() = toPxF().toInt()
fun Number.toPx(context: Context) = toPxF(context).toInt()

fun Number.toPxF() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), res.displayMetrics)
fun Number.toPxF(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)
fun Number.toDp() = toDpF().toInt()
fun Number.toDpF() = this.toFloat() * DisplayMetrics.DENSITY_DEFAULT / res.displayMetrics.densityDpi