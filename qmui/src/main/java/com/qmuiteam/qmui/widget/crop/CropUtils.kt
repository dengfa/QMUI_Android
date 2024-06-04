package com.miracle.photo.crop

import kotlin.math.abs

object CropUtils {

    var rotateDegree = 0

    fun isVertical(): Boolean {
        return abs(rotateDegree / 90 % 2) === 1
    }
}