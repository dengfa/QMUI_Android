/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.qmuiteam.qmui.widget.crop.photoView;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.OverScroller;


/**
 * The component of {@link PhotoView} which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than AppCompatImageView and still
 * gain the functionality that {@link PhotoView} offers
 */
public class PhotoViewAttacher2 implements View.OnTouchListener, View.OnLayoutChangeListener {

    private static final String TAG = "PhotoViewAttacher2";
    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;
    private static float DEFAULT_MAX_SCALE = 3.0f;
    private static float DEFAULT_MID_SCALE = 1.75f;
    private static float DEFAULT_MIN_SCALE = 1.0f;
    private static int DEFAULT_ZOOM_DURATION = 200;

    private static int DEFAULT_TRANSLATE_DURATION = 500;
    private static int DEFAULT_REC_CHANGE_DURATION = 300;

    private static int SINGLE_TOUCH = 1;
    // These are set so we don't keep allocating them on the heap
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    private final float[] mMatrixValues = new float[9];
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private int mTranslateDuration = DEFAULT_TRANSLATE_DURATION;
    private int rectChangeDuration = DEFAULT_REC_CHANGE_DURATION;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;
    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;
    private ImageView mImageView;
    // Gesture Detectors
    private GestureDetector mGestureDetector;
    private CustomGestureDetector mScaleDragDetector;
    // Listeners
    private OnMatrixChangedListener mMatrixChangeListener;
    private OnPhotoTapListener mPhotoTapListener;
    private OnOutsidePhotoTapListener mOutsidePhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private View.OnClickListener mOnClickListener;
    private OnLongClickListener mLongClickListener;
    private OnScaleChangedListener mScaleChangeListener;
    private OnSingleFlingListener mSingleFlingListener;
    private OnViewDragListener mOnViewDragListener;

    private FlingAndScrollRunnable mCurrentFlingRunnable;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
    private float mBaseRotation;

    private boolean mZoomEnabled = true;
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    private boolean mDoubleTapEnabled = true;

    // BaseMatrix固定宽高，解决控件高度变化后初始缩放不符合预期的问题
    private int stableViewWidth;
    private int stableViewHeight;

    private int boundPaddingBottom;

    private View.OnTouchListener customTouchListener;
    private boolean customTouchHandle = false;

    private float[] deltaArray = new float[2];

    private int overDragPadding;

    private int overDragX, overDragY;

    private OnGestureListener onGestureListener = new OnGestureListener() {
        @Override
        public void onDrag(float dx, float dy) {
            if (mScaleDragDetector.isScaling()) {
                return; // Do not drag if we are already scaling
            }
            if (customTouchHandle) {
                return;
            }
            if (mOnViewDragListener != null) {
                mOnViewDragListener.onDrag(dx, dy);
            }
            mSuppMatrix.postTranslate(dx, dy);

            checkAndDisplayMatrix(true);

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             */
            ViewParent parent = mImageView.getParent();
            if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling() && !mBlockParentIntercept) {
                if (mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH || (mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT
                        && dx >= 1f) || (mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f) || (
                        mVerticalScrollEdge == VERTICAL_EDGE_TOP
                                && dy >= 1f) || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f)) {
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(false);
                    }
                }
            } else {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        }

        @Override
        public void onFling(float startX, float startY, float velocityX, float velocityY) {
//            if (overDragX == 0 && overDragY == 0 && mCurrentFlingRunnable == null) {
//                Log.d(TAG, "onFling");
//                mCurrentFlingRunnable = new FlingAndScrollRunnable(mImageView.getContext());
//                mCurrentFlingRunnable.fling(getImageViewWidth(mImageView), getImageViewHeight(mImageView) - boundPaddingBottom, (int) velocityX,
//                        (int) velocityY);
//                mImageView.post(mCurrentFlingRunnable);
//            }
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            if (customTouchHandle) {
                return;
            }
            if (getScale() < mMaxScale || scaleFactor < 1f) {
                if (mScaleChangeListener != null) {
                    mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
                }
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                checkAndDisplayMatrix();
            }
        }
    };

    public int getWindowHeight() {
        return stableViewHeight - boundPaddingBottom;
    }

    public int getWindowWidth() {
        return stableViewWidth;
    }

    public PhotoViewAttacher2(ImageView imageView) {
        mImageView = imageView;
        imageView.setOnTouchListener(this);
        imageView.addOnLayoutChangeListener(this);
        if (imageView.isInEditMode()) {
            return;
        }
        mBaseRotation = 0.0f;
        // Create Gesture Detectors...
        mScaleDragDetector = new CustomGestureDetector(imageView.getContext(), onGestureListener);
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {

            // forward long click listener
            @Override
            public void onLongPress(MotionEvent e) {
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClick(mImageView);
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (!mDoubleTapEnabled) {
                    return onSingleTap(e);
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "fling  mSingleFlingListener : " + mSingleFlingListener);
                if (mSingleFlingListener != null) {
                    if (getScale() > DEFAULT_MIN_SCALE) {
                        return false;
                    }
                    if (e1.getPointerCount() > SINGLE_TOUCH || e2.getPointerCount() > SINGLE_TOUCH) {
                        return false;
                    }
                    return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mDoubleTapEnabled) {
                    return onSingleTap(e);
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent ev) {
                if (mDoubleTapEnabled) {
                    try {
                        float scale = getScale();
                        float x = ev.getX();
                        float y = ev.getY();
                        if (scale < getMediumScale()) {
                            setScale(getMediumScale(), x, y, true);
                        } else {
                            setScale(getMinimumScale(), x, y, true);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // Can sometimes happen when getX() and getY() is called
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Wait for the confirmed onDoubleTap() instead
                return false;
            }
        });
    }

    private boolean onSingleTap(MotionEvent e) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(mImageView);
        }
        final RectF displayRect = getDisplayRect();
        final float x = e.getX(), y = e.getY();
        if (mViewTapListener != null) {
            mViewTapListener.onViewTap(mImageView, x, y);
        }
        if (displayRect != null) {
            // Check to see if the user tapped on the photo
            if (displayRect.contains(x, y)) {
                float xResult = (x - displayRect.left) / displayRect.width();
                float yResult = (y - displayRect.top) / displayRect.height();
                if (mPhotoTapListener != null) {
                    mPhotoTapListener.onPhotoTap(mImageView, xResult, yResult);
                }
                return true;
            } else {
                if (mOutsidePhotoTapListener != null) {
                    mOutsidePhotoTapListener.onOutsidePhotoTap(mImageView);
                }
            }
        }
        return false;
    }

    public void setChangeListener(MatrixWithListener matrixWithListener) {
        mSuppMatrix = matrixWithListener;
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        this.mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangeListener) {
        this.mScaleChangeListener = onScaleChangeListener;
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        this.mSingleFlingListener = onSingleFlingListener;
    }

    public void setCustomTouchListener(View.OnTouchListener listener) {
        this.customTouchListener = listener;
    }

    @Deprecated
    public boolean isZoomEnabled() {
        return mZoomEnabled;
    }

    public boolean isInProgress() {
        return mScaleDragDetector != null && mScaleDragDetector.isInProgress();
    }

    public void setDoubleTapEnable(boolean enable) {
        this.mDoubleTapEnabled = enable;
    }

    public void setStableViewSize(int width, int height) {
        Log.d(TAG, "setStableViewSize: " + width + ", " + height);
        this.stableViewWidth = width;
        this.stableViewHeight = height;
    }

    public void setOverDragPadding(int size) {
        this.overDragPadding = size;
    }

    public void setBoundPaddingBottom(int size) {
        Log.d(TAG, "setBoundPaddingBottom:" + size);
        this.boundPaddingBottom = size;
    }

    public int getBoundPaddingBottom() {
        return this.boundPaddingBottom;
    }

    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (mImageView.getDrawable() == null) {
            return false;
        }
        mSuppMatrix.set(finalMatrix);
        checkAndDisplayMatrix();
        return true;
    }

    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        update();
        setRotationBy(mBaseRotation);
        checkAndDisplayMatrix();
    }

    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public float getMinimumScale() {
        return mMinScale;
    }

    public void setMinimumScale(float minimumScale) {
        Util.checkZoomLevels(minimumScale, mMidScale, mMaxScale);
        mMinScale = minimumScale;
    }

    public float getMediumScale() {
        return mMidScale;
    }

    public void setMediumScale(float mediumScale) {
        Util.checkZoomLevels(mMinScale, mediumScale, mMaxScale);
        mMidScale = mediumScale;
    }

    public float getMaximumScale() {
        return mMaxScale;
    }

    public void setMaximumScale(float maximumScale) {
        Util.checkZoomLevels(mMinScale, mMidScale, maximumScale);
        mMaxScale = maximumScale;
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(
                getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        if (Util.isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;
            update();
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                               int oldBottom) {
        // Update our base matrix, as the bounds have changed
        Log.d(TAG, "onLayoutChange");
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            updateBaseMatrix(mImageView.getDrawable());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;
        if (mZoomEnabled && Util.hasDrawable((ImageView) v)) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ViewParent parent = v.getParent();
                    // First, disable the Parent from intercepting the touch
                    // event
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    // If we're flinging, and the user presses down, cancel
                    // fling
                    cancelFling();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    // If the user has zoomed less than min scale, zoom back
                    // to min scale
                    if (getScale() < mMinScale) {
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMinScale, rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    } else if (getScale() > mMaxScale) {
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMaxScale, rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    } else if (overDragX != 0 || overDragY != 0){
                        cancelFling();
                        Log.d(TAG, "over drag recover");
                        mCurrentFlingRunnable = new FlingAndScrollRunnable(mImageView.getContext());
                        mCurrentFlingRunnable.scroll(overDragX, overDragY, mTranslateDuration, true);
                        mImageView.post(mCurrentFlingRunnable);
                        overDragX = 0;
                        overDragY = 0;
                    }
                    break;
            }
            boolean wasScaling = mScaleDragDetector != null && mScaleDragDetector.isScaling();
            if (!wasScaling && customTouchListener != null && customTouchListener.onTouch(v, ev)) {
                customTouchHandle = true;
            } else  {
                customTouchHandle = false;
            }
            // Try the Scale/Drag detector
            if (mScaleDragDetector != null) {
                boolean wasDragging = mScaleDragDetector.isDragging();
                handled = mScaleDragDetector.onTouchEvent(ev);
                boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
                boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();
                mBlockParentIntercept = didntScale && didntDrag;
            }
            // Check to see if the user double tapped
            if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
                handled = true;
            }
        }
        return handled;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        Util.checkZoomLevels(minimumScale, mediumScale, maximumScale);
        mMinScale = minimumScale;
        mMidScale = mediumScale;
        mMaxScale = maximumScale;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener mOutsidePhotoTapListener) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        mOnViewDragListener = listener;
    }

    public void setScale(float scale, boolean animate) {
        setScale(scale, (mImageView.getRight()) / 2, (mImageView.getBottom()) / 2, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        // Check to see if the scale is within bounds
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
        }
        if (animate) {
            mImageView.post(new AnimatedZoomRunnable(getScale(), scale, focalX, focalY));
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY);
            checkAndDisplayMatrix();
        }
    }

    public void translate(int dx, int dy, boolean animate) {
        translate(dx, dy, mTranslateDuration, animate);
    }

    public void translate(int dx, int dy, int duration, boolean animate) {
        if (animate) {
            cancelFling();
            mCurrentFlingRunnable = new FlingAndScrollRunnable(mImageView.getContext());
            mCurrentFlingRunnable.scroll(dx, dy, duration);
            mImageView.post(mCurrentFlingRunnable);
        } else  {
            mSuppMatrix.postTranslate(dx, dy);
        }
    }

    /**
     * Set the zoom interpolator
     *
     * @param interpolator the zoom interpolator
     */
    public void setZoomInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public boolean isZoomable() {
        return mZoomEnabled;
    }

    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    public void update() {
        Log.d(TAG, "update");
        if (mZoomEnabled) {
            // Update the base matrix using the current drawable
            updateBaseMatrix(mImageView.getDrawable());
        } else {
            // Reset the Matrix...
            resetMatrix();
        }
    }

    /**
     * Get the display matrix
     *
     * @param matrix target matrix to copy to
     */
    public void getDisplayMatrix(Matrix matrix) {
        matrix.set(getDrawMatrix());
    }

    /**
     * Get the current support matrix
     */
    public void getSuppMatrix(Matrix matrix) {
        matrix.set(mSuppMatrix);
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
    }

    public void setTranslateDuration(int milliseconds) {
        this.mTranslateDuration = milliseconds;
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        checkMatrixBounds();
        setImageViewMatrix(getDrawMatrix());
    }

    private void setImageViewMatrix(Matrix matrix) {
        Log.d(TAG, "call setImageViewMatrix: " + matrix.toShortString() + ", base=" + mBaseMatrix.toShortString());
        mImageView.setImageMatrix(matrix);
        // Call MatrixChangedListener if needed
        if (mMatrixChangeListener != null) {
            RectF displayRect = getDisplayRect(matrix);
            if (displayRect != null) {
                mMatrixChangeListener.onMatrixChanged(displayRect);
                mMatrixChangeListener.onMatrixChanged(displayRect, matrix);
            }
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        checkAndDisplayMatrix(false);
    }
    private void checkAndDisplayMatrix(boolean allowOverDrag) {
        if (checkMatrixBounds(allowOverDrag)) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = mImageView.getDrawable();
        if (d != null) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    /**
     * Calculate Matrix for FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     */
    private void updateBaseMatrix(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        Log.d(TAG, "updateBaseMatrix: " + drawable.getIntrinsicWidth() + ", " + drawable.getIntrinsicHeight());
        if (drawable.getIntrinsicWidth() <= 0 && drawable.getIntrinsicHeight() <= 0) {
            return;
        }
        updateScaleType(drawable);
        final float viewWidth;
        final float viewHeight;
        if (stableViewHeight > 0 && stableViewWidth > 0) {
            viewWidth = stableViewWidth;
            viewHeight = stableViewHeight;
        } else {
            viewWidth = getImageViewWidth(mImageView);
            viewHeight = getImageViewHeight(mImageView);
        }
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        mBaseMatrix.reset();
        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F, (viewHeight - drawableHeight) / 2F);
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            final float imageWhRadio = drawableWidth * 1f / drawableHeight;
            final float windowRadio;
            if (viewHeight > 0) {
                windowRadio = viewWidth / viewHeight;
            } else {
                windowRadio = imageWhRadio;
            }

            if (imageWhRadio < windowRadio){
                mBaseMatrix.postTranslate(0f, 0f);
            }else {
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                        (viewHeight - drawableHeight * scale) / 2F);
            }

        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);
        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
            }
            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }

    private void updateScaleType(Drawable drawable) {
        if (drawable == null || drawable.getIntrinsicHeight() == 0 || drawable.getIntrinsicWidth() == 0) {
            return;
        }
        final float viewWidth;
        final float viewHeight;
        if (stableViewHeight > 0 && stableViewWidth > 0) {
            viewWidth = stableViewWidth;
            viewHeight = stableViewHeight;
        } else {
            viewWidth = getImageViewWidth(mImageView);
            viewHeight = getImageViewHeight(mImageView);
        }
        if (viewWidth <= 0) {
            return;
        }
        final float imageRatio = (float) drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
        final float viewRatio = viewHeight / viewWidth;
        if (imageRatio >= viewRatio) {
            mScaleType = ScaleType.CENTER_CROP;
        } else {
            mScaleType = ScaleType.FIT_CENTER;
        }
    }

    public void animateRectToRect(RectF src, RectF dst) {
        if (dst == null || src == null) {
            return;
        }
        boolean update = checkMatrixBounds(dst, deltaArray, false);
        if (update) {
            RectF newDst = new RectF(dst);
            newDst.offset(deltaArray[0], deltaArray[1]);
            RectChangeRunnable r = new RectChangeRunnable(new RectF(src), newDst);
            mImageView.post(r);
        }
    }

    //  SUSION FIX  判断当前 photo view 是否显示在图片的顶部
    public boolean displyRectIsFromTop() {
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) return true;
        return rect.top >= 0;
    }

    private boolean checkMatrixBounds() {
        return checkMatrixBounds(false);
    }

    private boolean checkMatrixBounds(boolean allowOverDrag) {
        final RectF rect = getDisplayRect(getDrawMatrix());
        boolean update = checkMatrixBounds(rect, deltaArray, allowOverDrag);
        if (update) {
            mSuppMatrix.postTranslate(deltaArray[0], deltaArray[1]);
            Log.d(TAG, "call checkMatrixBounds: " + deltaArray[0] + ", " +  deltaArray[1]);
        } else {
            Log.d(TAG, "call checkMatrixBounds: false");
        }
        return update;
    }

    private boolean checkMatrixBounds(RectF rect, float[] deltaArray, boolean allowOverDrag) {
        if (rect == null) {
            return false;
        }
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = getImageViewHeight(mImageView) - boundPaddingBottom;

        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
        } else if (rect.top > 0) {
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;
            deltaY = -rect.top;
            if (allowOverDrag && overDragPadding > 0) {
                if (Math.abs(deltaY) >= overDragPadding) {
                    overDragY = overDragPadding;
                } else {
                    overDragY = -(int) deltaY;
                }
                deltaY = deltaY + overDragY;
            }
        } else if (rect.bottom < viewHeight) {
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
            deltaY = viewHeight - rect.bottom;
            if (allowOverDrag && overDragPadding > 0) {
                if (Math.abs(deltaY) >= overDragPadding) {
                    overDragY = -overDragPadding;
                } else {
                    overDragY = -(int) deltaY;
                }
                deltaY = deltaY + overDragY;
            }
        } else {
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }

        final int viewWidth = getImageViewWidth(mImageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
        } else if (rect.left > 0) {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
            deltaX = -rect.left;
            if (allowOverDrag && overDragPadding > 0) {
                if (Math.abs(deltaX) >= overDragPadding) {
                    overDragX = overDragPadding;
                } else {
                    overDragX = -(int) deltaX;
                }
                deltaX = deltaX + overDragX;
            }
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;
            if (allowOverDrag && overDragPadding > 0) {
                if (Math.abs(deltaX) >= overDragPadding) {
                    overDragX = -overDragPadding;
                } else {
                    overDragX = -(int) deltaX;
                }
                deltaX = deltaX + overDragX;
            }
        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        // Finally actually translate the matrix
        deltaArray[0] = deltaX;
        deltaArray[1] = deltaY;
        return true;
    }

    private int getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            Log.e(TAG, "cancelFling", new Exception());
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom, final float focalX,
                                    final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingAndScrollRunnable implements Runnable {

        private final OverScroller mScroller;
        private int mCurrentX, mCurrentY;
        private boolean ignoreBoundCheck = false;

        public FlingAndScrollRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
            final RectF rect = getDisplayRect();
            if (rect == null) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;
            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
            }
        }

        public void scroll(int dx, int dy, int duration) {
            scroll(dx, dy, duration, ignoreBoundCheck);
        }

        public void scroll(int dx, int dy, int duration, boolean ignoreBoundCheck) {
            this.ignoreBoundCheck = ignoreBoundCheck;
            final RectF rect;
            if (ignoreBoundCheck) {
                rect = getDisplayRect(getDrawMatrix());
            } else {
                rect = getDisplayRect();
            }
            if (rect == null) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int startY = Math.round(-rect.top);
            mCurrentX = startX;
            mCurrentY = startY;
            if (duration > 0) {
                mScroller.startScroll(startX, startY, dx, dy, duration);
            } else {
                mScroller.startScroll(startX, startY, dx, dy);
            }
        }


        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }
            if (mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                if (ignoreBoundCheck) {
                    setImageViewMatrix(getDrawMatrix());
                } else {
                    checkAndDisplayMatrix();
                }
                mCurrentX = newX;
                mCurrentY = newY;
                // Post On animation
                Compat.postOnAnimation(mImageView, this);
            }
        }
    }

    private class RectChangeRunnable implements Runnable {
        private final long startTime;
        private final RectF src, dst, preRect, tmpRect;
        private final Matrix tmpMatrix = new Matrix();

        public RectChangeRunnable(RectF src, RectF dst) {
            startTime = System.currentTimeMillis();
            tmpRect = new RectF();
            preRect = new RectF();
            this.src = src;
            this.dst = dst;
            preRect.set(src);
        }

        @Override
        public void run() {
            float t = interpolate();
            tmpRect.set(
                    src.left + t * (dst.left - src.left),
                    src.top + t * (dst.top - src.top),
                    src.right + t * (dst.right - src.right),
                    src.bottom + t * (dst.bottom - src.bottom)
            );
            tmpMatrix.reset();
            tmpMatrix.setRectToRect(preRect, tmpRect, ScaleToFit.CENTER);
            mSuppMatrix.postConcat(tmpMatrix);
            setImageViewMatrix(getDrawMatrix());
            preRect.set(tmpRect);
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - startTime) / rectChangeDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }
}
