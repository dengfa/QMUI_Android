package com.qmuiteam.qmui.tablayout.mvp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.qmuiteam.qmui.R
import com.qmuiteam.qmui.R.styleable
import java.util.ArrayList

/**
 * 简化版
 */
/** 滑动TabLayout,对于ViewPager的依赖性强  */
class SlidingTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    HorizontalScrollView(context, attrs, defStyleAttr), OnPageChangeListener {

    companion object {
        private const val STYLE_NORMAL = 0
        private const val STYLE_TRIANGLE = 1
        private const val STYLE_BLOCK = 2
    }

    private val mContext: Context
    private var mViewPager: ViewPager? = null
    private var mTitles: ArrayList<String>? = null
    private val mTabsContainer: LinearLayout
    private var mCurrentTab = 0
    private var mCurrentPositionOffset = 0f
    private var tabCount = 0

    /** 用于绘制显示器  */
    private val mIndicatorRect = Rect()

    /** 用于实现滚动居中  */
    private val mTabRect = Rect()
    private val mIndicatorDrawable = GradientDrawable()
    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePath = Path()
    private var mIndicatorStyle = STYLE_NORMAL
    private var mTabPadding = 0f
    private var mTabSpaceEqual = false
    private var mTabWidth = 0f

    /** indicator  */
    private var mIndicatorColor = 0
    private var mIndicatorHeight = 0f
    private var mIndicatorWidth = 0f
    private var mIndicatorCornerRadius = 0f
    private var indicatorMarginLeft = 0f
    private var indicatorMarginTop = 0f
    private var indicatorMarginRight = 0f
    private var indicatorMarginBottom = 0f
    private var mIndicatorGravity = 0
    private var mIndicatorWidthEqualTitle = false

    /** underline  */
    private var mUnderlineColor = 0
    private var mUnderlineHeight = 0f
    private var mUnderlineGravity = 0

    /** divider  */
    private var mDividerColor = 0
    private var mDividerWidth = 0f
    private var mDividerPadding = 0f
    private var mTextSelectedSize = 0f
    private var mTextUnselectedSize = 0f
    private var mTextSelectColor = 0
    private var mTextUnselectColor = 0
    private var mTextBold = 0
    private var mTextSelectTypeFace = Typeface.DEFAULT
    private var mTextUnSelectTypeFace = Typeface.DEFAULT
    private var mLastScrollX = 0
    private var mSnapOnTabClick = false

    init {
        isFillViewport = true //设置滚动视图是否可以伸缩其内容以填充视口
        setWillNotDraw(false) //重写onDraw方法,需要调用这个方法来清除flag
        clipChildren = false
        clipToPadding = false
        mContext = context
        mTabsContainer = LinearLayout(context)
        addView(mTabsContainer)
        obtainAttributes(context, attrs)
    }

    fun setTextTypeFace(selected: Typeface?, unselected: Typeface?) {
        if (selected != null) {
            mTextSelectTypeFace = selected
        }
        if (unselected != null) {
            mTextUnSelectTypeFace = unselected
        }
        updateTabStyles()
    }

    private fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, styleable.SlidingTabLayout)
        mIndicatorStyle = ta.getInt(styleable.SlidingTabLayout_tl_indicator_style, STYLE_NORMAL)
        mIndicatorColor =
            ta.getColor(styleable.SlidingTabLayout_tl_indicator_color, Color.parseColor(if (mIndicatorStyle == STYLE_BLOCK) "#4B6A87" else "#ffffff"))
        mIndicatorHeight = ta.getDimension(
            styleable.SlidingTabLayout_tl_indicator_height,
            dp2px(if (mIndicatorStyle == STYLE_TRIANGLE) 4f else (if (mIndicatorStyle == STYLE_BLOCK) -1f else 2f))
        )
        mIndicatorWidth = ta.getDimension(
            styleable.SlidingTabLayout_tl_indicator_width, dp2px(if (mIndicatorStyle == STYLE_TRIANGLE) 10f else -1f)
        )
        mIndicatorCornerRadius = ta.getDimension(
            styleable.SlidingTabLayout_tl_indicator_corner_radius, dp2px(if (mIndicatorStyle == STYLE_BLOCK) -1f else 0f)
        )
        indicatorMarginLeft = ta.getDimension(styleable.SlidingTabLayout_tl_indicator_margin_left, 0f)
        indicatorMarginTop = ta.getDimension(
            styleable.SlidingTabLayout_tl_indicator_margin_top, dp2px(if (mIndicatorStyle == STYLE_BLOCK) 7f else 0f)
        )
        indicatorMarginRight = ta.getDimension(styleable.SlidingTabLayout_tl_indicator_margin_right, 0f)
        indicatorMarginBottom = ta.getDimension(
            styleable.SlidingTabLayout_tl_indicator_margin_bottom, dp2px(if (mIndicatorStyle == STYLE_BLOCK) 7f else 0f)
        )
        mIndicatorGravity = ta.getInt(styleable.SlidingTabLayout_tl_indicator_gravity, Gravity.BOTTOM)
        mIndicatorWidthEqualTitle = ta.getBoolean(styleable.SlidingTabLayout_tl_indicator_width_equal_title, false)
        mUnderlineColor = ta.getColor(styleable.SlidingTabLayout_tl_underline_color, Color.parseColor("#ffffff"))
        mUnderlineHeight = ta.getDimension(styleable.SlidingTabLayout_tl_underline_height, 0f)
        mUnderlineGravity = ta.getInt(styleable.SlidingTabLayout_tl_underline_gravity, Gravity.BOTTOM)
        mDividerColor = ta.getColor(styleable.SlidingTabLayout_tl_divider_color, Color.parseColor("#ffffff"))
        mDividerWidth = ta.getDimension(styleable.SlidingTabLayout_tl_divider_width, 0f)
        mDividerPadding = ta.getDimension(styleable.SlidingTabLayout_tl_divider_padding, dp2px(12f))
        mTextSelectedSize = ta.getDimension(styleable.SlidingTabLayout_tl_textSelectSize, dp2px(14f))
        mTextUnselectedSize = ta.getDimension(styleable.SlidingTabLayout_tl_textUnselectSize, dp2px(14f))
        mTextSelectColor = ta.getColor(styleable.SlidingTabLayout_tl_textSelectColor, Color.parseColor("#ffffff"))
        mTextUnselectColor = ta.getColor(styleable.SlidingTabLayout_tl_textUnselectColor, Color.parseColor("#AAffffff"))
        mTabSpaceEqual = ta.getBoolean(styleable.SlidingTabLayout_tl_tab_space_equal, false)
        mTabWidth = ta.getDimension(styleable.SlidingTabLayout_tl_tab_width, dp2px(-1f))
        mTabPadding = ta.getDimension(
            styleable.SlidingTabLayout_tl_tab_padding, if (mTabSpaceEqual || mTabWidth > 0) 0f else dp2px(20f)
        )
        ta.recycle()
    }

    /** 关联ViewPager  */
    fun setViewPager(vp: ViewPager) {
        mViewPager = vp
        mViewPager?.removeOnPageChangeListener(this)
        mViewPager?.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }

    /** 关联ViewPager,用于不想在ViewPager适配器中设置titles数据的情况  */
    fun setViewPager(vp: ViewPager, titles: Array<String>) {
        mViewPager = vp
        mTitles = arrayListOf()
        mTitles?.addAll(titles)
        mViewPager?.removeOnPageChangeListener(this)
        mViewPager?.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }

    /** 更新数据  */
    fun notifyDataSetChanged() {
        mTabsContainer.removeAllViews()
        tabCount = mTitles?.size ?: (mViewPager?.adapter?.count ?: 0)
        for (i in 0 until tabCount) {
            val tabView = inflate(mContext, R.layout.guix_layout_tab, null)
            val pageTitle = if (mTitles == null) mViewPager?.adapter?.getPageTitle(i)?.toString() ?: "" else mTitles?.get(i) ?: ""
            addTab(i, pageTitle, tabView)
        }
        updateTabStyles()
    }

    /** 创建并添加tab  */
    private fun addTab(position: Int, title: String, tabView: View) {
        val tvTabTitle = tabView.findViewById<TextView>(R.id.tvTabTitle)
        tvTabTitle?.text = title
        tabView.setOnClickListener { v ->
            val index = mTabsContainer.indexOfChild(v)
            if (index != -1) {
                if (mViewPager?.currentItem != position) {
                    if (mSnapOnTabClick) {
                        mViewPager?.setCurrentItem(position, false)
                    } else {
                        mViewPager?.currentItem = position
                    }
                }
            }
        }
        /** 每一个Tab的布局参数  */
        var lpTab = if (mTabSpaceEqual) LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f) else LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT
        )
        if (mTabWidth > 0) {
            lpTab = LinearLayout.LayoutParams(mTabWidth.toInt(), LayoutParams.MATCH_PARENT)
        }
        mTabsContainer.addView(tabView, position, lpTab)
    }

    private fun updateTabStyles() {
        for (i in 0 until tabCount) {
            val view = mTabsContainer.getChildAt(i)
            val tvTabTitle = view?.findViewById<TextView>(R.id.tvTabTitle)
            if (tvTabTitle != null) {
                tvTabTitle.setTextColor(if (i == mCurrentTab) mTextSelectColor else mTextUnselectColor)
                tvTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (i == mCurrentTab) mTextSelectedSize else mTextUnselectedSize)
                tvTabTitle.typeface = if (i == mCurrentTab) mTextSelectTypeFace else mTextUnSelectTypeFace
                tvTabTitle.setPadding(mTabPadding.toInt(), 0, mTabPadding.toInt(), 0)
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        /**
         * position:当前View的位置
         * mCurrentPositionOffset:当前View的偏移量比例.[0,1)
         */
        mCurrentTab = position
        mCurrentPositionOffset = positionOffset
        scrollToCurrentTab()
        invalidate()
    }

    override fun onPageSelected(position: Int) {
        updateTabSelection(position)
        mListener?.onTabSelect(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}

    /** HorizontalScrollView滚到当前tab,并且居中显示  */
    private fun scrollToCurrentTab() {
        if (tabCount <= 0 || mTabsContainer.getChildAt(mCurrentTab) == null) {
            return
        }
        val curTab = mTabsContainer.getChildAt(mCurrentTab)
        val offset = (mCurrentPositionOffset * curTab.width).toInt()

        /**当前Tab的left+当前Tab的Width乘以positionOffset */
        var newScrollX = curTab.left + offset
        if (mCurrentTab > 0 || offset > 0) {
            /**HorizontalScrollView移动到当前tab,并居中 */
            newScrollX -= width / 2 - paddingLeft
            calcIndicatorRect()
            newScrollX += (mTabRect.right - mTabRect.left) / 2
        }
        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX
            /** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
             * x:表示离起始位置的x水平方向的偏移量
             * y:表示离起始位置的y垂直方向的偏移量
             */
            scrollTo(newScrollX, 0)
        }
    }

    private fun updateTabSelection(position: Int) {
        for (i in 0 until tabCount) {
            val tabView = mTabsContainer.getChildAt(i)
            val isSelect = i == position
            val tvTabTitle = tabView.findViewById<TextView>(R.id.tvTabTitle)
            if (tvTabTitle != null) {
                tvTabTitle.setTextColor(if (isSelect) mTextSelectColor else mTextUnselectColor)
                tvTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (isSelect) mTextSelectedSize else mTextUnselectedSize)
                tvTabTitle.typeface = if (isSelect) mTextSelectTypeFace else mTextUnSelectTypeFace
            }
        }
    }

    private var margin = 0f
    private fun calcIndicatorRect() {
        val currentTabView = mTabsContainer.getChildAt(mCurrentTab) ?: return

        var left = currentTabView.left.toFloat()
        var right = currentTabView.right.toFloat()

        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            val tvTabTitle = currentTabView.findViewById<TextView>(R.id.tvTabTitle)
            mTextPaint.textSize = mTextSelectedSize
            val textWidth = mTextPaint.measureText(tvTabTitle.text?.toString() ?: "")
            margin = (right - left - textWidth) / 2
        }
        if (mCurrentTab < tabCount - 1) {
            val nextTabView = mTabsContainer.getChildAt(mCurrentTab + 1)
            val nextTabLeft = nextTabView.left
            val nextTabRight = nextTabView.right
            left += mCurrentPositionOffset * (nextTabLeft - left)
            right += mCurrentPositionOffset * (nextTabRight - right)

            //for mIndicatorWidthEqualTitle
            if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
                val nextTabTitle = nextTabView.findViewById<TextView>(R.id.tvTabTitle)
                mTextPaint.textSize = mTextSelectedSize
                val nextTextWidth = mTextPaint.measureText(nextTabTitle.text?.toString() ?: "")
                val nextMargin = (nextTabRight - nextTabLeft - nextTextWidth) / 2
                margin += mCurrentPositionOffset * (nextMargin - margin)
            }
        }
        mIndicatorRect.left = left.toInt()
        mIndicatorRect.right = right.toInt()
        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            mIndicatorRect.left = (left + margin - 1).toInt()
            mIndicatorRect.right = (right - margin - 1).toInt()
        }
        mTabRect.left = left.toInt()
        mTabRect.right = right.toInt()
        if (mIndicatorWidth >= 0) {
            //indicatorWidth大于0时,圆角矩形以及三角形
            var indicatorLeft = currentTabView.left + (currentTabView.width - mIndicatorWidth) / 2
            if (mCurrentTab < tabCount - 1) {
                val nextTab = mTabsContainer.getChildAt(mCurrentTab + 1)
                indicatorLeft += mCurrentPositionOffset * (currentTabView.width / 2f + nextTab.width / 2f)
            }
            mIndicatorRect.left = indicatorLeft.toInt()
            mIndicatorRect.right = (mIndicatorRect.left + mIndicatorWidth).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode || tabCount <= 0) {
            return
        }
        val height = height
        val paddingLeft = paddingLeft
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.strokeWidth = mDividerWidth
            mDividerPaint.color = mDividerColor
            for (i in 0 until tabCount - 1) {
                val tab = mTabsContainer.getChildAt(i)
                canvas.drawLine(
                    (paddingLeft + tab.right).toFloat(), mDividerPadding, (paddingLeft + tab.right).toFloat(), height - mDividerPadding, mDividerPaint
                )
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.color = mUnderlineColor
            if (mUnderlineGravity == Gravity.BOTTOM) {
                canvas.drawRect(
                    paddingLeft.toFloat(), height - mUnderlineHeight, (mTabsContainer.width + paddingLeft).toFloat(), height.toFloat(), mRectPaint
                )
            } else {
                canvas.drawRect(paddingLeft.toFloat(), 0f, (mTabsContainer.width + paddingLeft).toFloat(), mUnderlineHeight, mRectPaint)
            }
        }

        //draw indicator line
        calcIndicatorRect()
        if (mIndicatorStyle == STYLE_TRIANGLE) {
            if (mIndicatorHeight > 0) {
                mTrianglePaint.color = mIndicatorColor
                mTrianglePath.reset()
                mTrianglePath.moveTo((paddingLeft + mIndicatorRect.left).toFloat(), height.toFloat())
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.left / 2f + mIndicatorRect.right / 2f, height - mIndicatorHeight)
                mTrianglePath.lineTo((paddingLeft + mIndicatorRect.right).toFloat(), height.toFloat())
                mTrianglePath.close()
                canvas.drawPath(mTrianglePath, mTrianglePaint)
            }
        } else if (mIndicatorStyle == STYLE_BLOCK) {
            if (mIndicatorHeight < 0) {
                mIndicatorHeight = height - indicatorMarginTop - indicatorMarginBottom
            }
            if (mIndicatorHeight > 0) {
                if (mIndicatorCornerRadius < 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
                    mIndicatorCornerRadius = mIndicatorHeight / 2
                }
                mIndicatorDrawable.setColor(mIndicatorColor)
                mIndicatorDrawable.setBounds(
                    paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                    indicatorMarginTop.toInt(),
                    (paddingLeft + mIndicatorRect.right - indicatorMarginRight).toInt(),
                    (indicatorMarginTop + mIndicatorHeight).toInt()
                )
                mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                mIndicatorDrawable.draw(canvas)
            }
        } else {
            if (mIndicatorHeight > 0) {
                mIndicatorDrawable.setColor(mIndicatorColor)
                if (mIndicatorGravity == Gravity.BOTTOM) {
                    mIndicatorDrawable.setBounds(
                        paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                        height - mIndicatorHeight.toInt() - indicatorMarginBottom.toInt(),
                        paddingLeft + mIndicatorRect.right - indicatorMarginRight.toInt(),
                        height - indicatorMarginBottom.toInt()
                    )
                } else {
                    mIndicatorDrawable.setBounds(
                        paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                        indicatorMarginTop.toInt(),
                        paddingLeft + mIndicatorRect.right - indicatorMarginRight.toInt(),
                        mIndicatorHeight.toInt() + indicatorMarginTop.toInt()
                    )
                }
                mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                mIndicatorDrawable.draw(canvas)
            }
        }
    }

    fun setCurrentTab(currentTab: Int, smoothScroll: Boolean) {
        mCurrentTab = currentTab
        mViewPager!!.setCurrentItem(currentTab, smoothScroll)
    }

    fun setIndicatorGravity(indicatorGravity: Int) {
        mIndicatorGravity = indicatorGravity
        invalidate()
    }

    fun setIndicatorMargin(indicatorMarginLeft: Float, indicatorMarginTop: Float, indicatorMarginRight: Float, indicatorMarginBottom: Float) {
        this.indicatorMarginLeft = dp2px(indicatorMarginLeft)
        this.indicatorMarginTop = dp2px(indicatorMarginTop)
        this.indicatorMarginRight = dp2px(indicatorMarginRight)
        this.indicatorMarginBottom = dp2px(indicatorMarginBottom)
        invalidate()
    }

    fun setIndicatorWidthEqualTitle(indicatorWidthEqualTitle: Boolean) {
        mIndicatorWidthEqualTitle = indicatorWidthEqualTitle
        invalidate()
    }

    fun setUnderlineGravity(underlineGravity: Int) {
        mUnderlineGravity = underlineGravity
        invalidate()
    }

    fun setTextSize(selected: Float, unselected: Float) {
        mTextSelectedSize = selected
        mTextUnselectedSize = unselected
        updateTabStyles()
    }

    fun setSnapOnTabClick(snapOnTabClick: Boolean) {
        mSnapOnTabClick = snapOnTabClick
    }

    //setter and getter
    var currentTab: Int
        get() = mCurrentTab
        set(currentTab) {
            mCurrentTab = currentTab
            mViewPager!!.currentItem = currentTab
        }
    var indicatorStyle: Int
        get() = mIndicatorStyle
        set(indicatorStyle) {
            mIndicatorStyle = indicatorStyle
            invalidate()
        }
    var tabPadding: Float
        get() = mTabPadding
        set(tabPadding) {
            mTabPadding = dp2px(tabPadding).toFloat()
            updateTabStyles()
        }
    var isTabSpaceEqual: Boolean
        get() = mTabSpaceEqual
        set(tabSpaceEqual) {
            mTabSpaceEqual = tabSpaceEqual
            updateTabStyles()
        }
    var tabWidth: Float
        get() = mTabWidth
        set(tabWidth) {
            mTabWidth = dp2px(tabWidth)
            updateTabStyles()
        }
    var indicatorColor: Int
        get() = mIndicatorColor
        set(indicatorColor) {
            mIndicatorColor = indicatorColor
            invalidate()
        }
    var indicatorHeight: Float
        get() = mIndicatorHeight
        set(indicatorHeight) {
            mIndicatorHeight = dp2px(indicatorHeight)
            invalidate()
        }
    var indicatorWidth: Float
        get() = mIndicatorWidth
        set(indicatorWidth) {
            mIndicatorWidth = dp2px(indicatorWidth)
            invalidate()
        }
    var indicatorCornerRadius: Float
        get() = mIndicatorCornerRadius
        set(indicatorCornerRadius) {
            mIndicatorCornerRadius = dp2px(indicatorCornerRadius)
            invalidate()
        }
    var underlineColor: Int
        get() = mUnderlineColor
        set(underlineColor) {
            mUnderlineColor = underlineColor
            invalidate()
        }
    var underlineHeight: Float
        get() = mUnderlineHeight
        set(underlineHeight) {
            mUnderlineHeight = dp2px(underlineHeight)
            invalidate()
        }
    var dividerColor: Int
        get() = mDividerColor
        set(dividerColor) {
            mDividerColor = dividerColor
            invalidate()
        }
    var dividerWidth: Float
        get() = mDividerWidth
        set(dividerWidth) {
            mDividerWidth = dp2px(dividerWidth)
            invalidate()
        }
    var dividerPadding: Float
        get() = mDividerPadding
        set(dividerPadding) {
            mDividerPadding = dp2px(dividerPadding)
            invalidate()
        }
    var textSelectColor: Int
        get() = mTextSelectColor
        set(textSelectColor) {
            mTextSelectColor = textSelectColor
            updateTabStyles()
        }
    var textUnselectedColor: Int
        get() = mTextUnselectColor
        set(textUnselectedColor) {
            mTextUnselectColor = textUnselectedColor
            updateTabStyles()
        }
    var textBold: Int
        get() = mTextBold
        set(textBold) {
            mTextBold = textBold
            updateTabStyles()
        }

    fun getTitleView(tab: Int): TextView {
        val tabView = mTabsContainer.getChildAt(tab)
        return tabView.findViewById(R.id.tvTabTitle)
    }

    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 显示/隐藏未读红点
     *
     * @param position 显示tab位置
     */
    fun updateRedDot(position: Int, show: Boolean) {
        if (position < 0 || position >= tabCount) {
            return
        }
        val tabView = mTabsContainer.getChildAt(position)
        val tipView = tabView.findViewById<View>(R.id.redDot)
        tipView?.visibility = if (show) VISIBLE else GONE
    }

    private var mListener: OnTabSelectListener? = null
    fun setOnTabSelectListener(listener: OnTabSelectListener?) {
        mListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putInt("mCurrentTab", mCurrentTab)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var saveState: Parcelable? = state
        if (state is Bundle) {
            mCurrentTab = state.getInt("mCurrentTab")
            saveState = state.getParcelable("instanceState")
            if (mCurrentTab != 0 && mTabsContainer.childCount > 0) {
                updateTabSelection(mCurrentTab)
                scrollToCurrentTab()
            }
        }
        super.onRestoreInstanceState(saveState)
    }

    private fun dp2px(dp: Float): Float {
        val scale = mContext.resources.displayMetrics.density
        return dp * scale + 0.5f
    }
}