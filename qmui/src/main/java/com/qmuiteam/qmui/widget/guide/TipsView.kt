package com.qmuiteam.qmui.widget.guideimport android.content.Contextimport android.util.AttributeSetimport android.view.LayoutInflaterimport android.view.Viewimport android.widget.FrameLayoutimport com.qmuiteam.qmui.Rimport kotlinx.android.synthetic.main.layout_tips_view.view.*/** * Created by dengfa on 2022/5/5 */class TipsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {    init {        LayoutInflater.from(context).inflate(R.layout.layout_tips_view, this, true)    }    fun setTips(        title: String, tips: String, stepStr: String, lottie: String = "", preStr: String = "", nextStr: String = "",        onPre: (() -> Unit)? = null, onNext: (() -> Unit)? = null    ) {        tvTitle.text = title        tvContent.text = tips        tvStep.text = stepStr        if (nextStr.isNotEmpty()) {            tvNext.visibility = View.VISIBLE            tvNext.text = nextStr            tvNext.setOnClickListener {                onNext?.invoke()            }        } else {            tvNext.visibility = View.GONE        }        if (preStr.isNotEmpty()) {            tvPre.visibility = View.VISIBLE            tvPre.text = preStr            tvPre.setOnClickListener {                onPre?.invoke()            }        } else {            tvPre.visibility = View.GONE        }        if (lottie.isNotEmpty()) {            lottieView.visibility = View.VISIBLE            lottieView.setAnimation(lottie)        } else {            lottieView.visibility = View.GONE        }    }}