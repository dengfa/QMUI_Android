package com.qmuiteam.qmui.arch.storage


/**
 * sp封装使用example
 * sp集中存储类
 * 通过代理模式，将sp的样板代码转换为直接赋值和取值操作！
 * */
object SPUseExample : QMSharedPreferences("sp_test") {

    var lastPlayPlusBubbleTime: Long by QMSharedPreferencesDelegate(this, "last_play_plus_bubble_time", 0L)
}