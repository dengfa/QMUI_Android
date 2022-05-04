package com.qmuiteam.qmui.widget.guideimport android.content.Contextimport android.graphics.*import android.util.AttributeSetimport android.util.Logimport com.qmuiteam.qmui.Rimport android.view.Viewimport android.widget.FrameLayoutimport androidx.core.content.ContextCompatclass GuideCoverView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {    var radius = 0f    /**     * 需要显示提示信息的View     */    private var targetView: View? = null    /**     * 背景色和透明度，格式 #aarrggbb     */    private var bgColor = ContextCompat.getColor(context, R.color.qmui_config_color_25_pure_black)    /**     * targetView左上角坐标     */    private var location: IntArray = IntArray(2)    init {        setLayerType(View.LAYER_TYPE_SOFTWARE, null)        setWillNotDraw(false)    }    fun resetTarget(targetView: View) {        Log.d("vincent", "GuideCoverView resetTarget")        this.targetView = targetView        targetView.getLocationInWindow(location)        postInvalidate()    }    override fun onDraw(canvas: Canvas?) {        Log.d("vincent", "GuideCoverView onDraw")        super.onDraw(canvas)        if (canvas != null) {            drawBackground(canvas)        }    }    private val porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)    private fun drawBackground(canvas: Canvas) {        Log.d("vincent", "GuideCoverView drawBackground")        // 背景画笔        val bgPaint = Paint()        bgPaint.isAntiAlias = true        canvas.save()        // 绘制屏幕背景        canvas.drawColor(bgColor)        // targetView的透明挖空        bgPaint.xfermode = porterDuffXfermode        val rect = RectF()        rect.left = location[0].toFloat()        rect.top = location[1].toFloat()        rect.right = location[0].toFloat() + (targetView?.width ?: 0)        rect.bottom = location[1].toFloat() + (targetView?.height ?: 0)        canvas.drawRoundRect(rect, radius, radius, bgPaint)        bgPaint.xfermode = null        canvas.restore()    }}