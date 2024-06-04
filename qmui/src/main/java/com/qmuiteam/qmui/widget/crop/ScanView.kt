package com.qmuiteam.qmui.widget.crop

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.qmuiteam.qmui.R

/**
 * @author milo
 * @data 2019/4/1
 * 上下扫描view
 */
class ScanView : View {

    companion object {
        private const val TAG = "ScanView"
    }

    /** 扫描的图片drawable */
    private var scanImg: Drawable? = null
    private lateinit var paint: Paint

    /** 扫描涂抹bitmap */
    private val scanBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.search_icon_scan)
    }

    /**
     * 是否反方向动画
     */
    private var isPositive = true

    /** 属性动画 */
    private var valueAnimator: ValueAnimator? = null

    /** 动画时长 */
    private var animDuration: Long = 1000L

    private var orientation = 0f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
        init()
    }

    private fun initAttribute(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanView)
            scanImg = typedArray.getDrawable(R.styleable.ScanView_unit_scan_img)
            animDuration = typedArray.getInt(R.styleable.ScanView_anim_duration, 1000).toLong()
            typedArray.recycle()
        }
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
    }

    fun setOrientation(orientation: Float) {
        this.orientation = orientation
    }

    private val srcRect = Rect(0, 0, scanBitmap.width, scanBitmap.height)
    private val desRectF = Rect()
    private var curTop = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (orientation) {
            0f -> {
                desRectF.set(0, curTop, width, scanBitmap.height + curTop)
            }

            else -> {
                desRectF.set(0, curTop, height, scanBitmap.height + curTop)
            }
        }
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.rotate(orientation)
        if (!isPositive) canvas.rotate(180f)
        if (orientation == 0f) {
            canvas.translate(-width / 2f, -height / 2f)
        } else {
            canvas.translate(-height / 2f, -width / 2f)
        }
        canvas.drawBitmap(
            scanBitmap,
            srcRect,
            desRectF,
            paint
        )
        canvas.restore()
    }

    /**
     * 开始做属性动画
     */
    fun startScanAnim() {
        isPositive = true
        valueAnimator?.takeIf { it.isRunning }?.cancel()
        visibility = VISIBLE
        val scanLength = if (orientation == 0f) height else width
        val scanBitmapHeight = scanBitmap.height.toFloat()
        valueAnimator = ValueAnimator.ofFloat(-scanBitmapHeight, scanLength.toFloat())
        valueAnimator?.apply {
            // 使得扫描动画在横竖状态下都是相同的速度
            duration =
                if (orientation == 0f) animDuration else (animDuration * 1.0f / width * height).toLong()
            repeatCount = -1
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation ->
                curTop = (animation?.animatedValue as? Float)?.toInt() ?: 0
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    // 用于控制 scan img 动画来回时的方向
                    isPositive = !isPositive
                }
            })
            start()
        }
    }

    /**
     * 停止属性动画
     */
    fun stopScanAnimAndReset() {
        valueAnimator?.takeIf { it.isRunning }?.cancel()
        reset()
    }

    /**
     * 重置为初始状态
     */
    private fun reset() {
        isPositive = true
        visibility = INVISIBLE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScanAnimAndReset()
    }
}