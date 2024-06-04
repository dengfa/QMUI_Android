package com.qmuiteam.qmui.widget.crop

import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * @author Murcy on 2019-12-15.
 * @describe 点击保护
 */
class OnClickListenerProxy(val interval: Long = 500, val realObj: View.OnClickListener) :
    View.OnClickListener {

    companion object {
        private val mainHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Handler(Looper.getMainLooper()) }
    }

    private var enabled: Boolean = true
    private val enableAgain = Runnable { this.enabled = true }

    override fun onClick(v: View) {
        if (!this.enabled) return
        this.enabled = false
        mainHandler.postDelayed({
            this.enableAgain.run()
        }, this.interval)
        realObj.onClick(v)
    }
}

fun singleOnClickListener(interval: Long = 500, block: ((View?) -> Unit)): View.OnClickListener {
    return OnClickListenerProxy(interval = interval, realObj = View.OnClickListener(block))
}

fun View.setSingleClickListener(block: ((View?) -> Unit)) {
    setOnClickListener(singleOnClickListener(500, block))
}

fun View.setShortSingleClickListener(block: ((View?) -> Unit)) {
    setOnClickListener(singleOnClickListener(200, block))
}