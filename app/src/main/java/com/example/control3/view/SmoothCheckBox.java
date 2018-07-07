package com.example.control3.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SmoothCheckBox extends View {

    private static final String TAG = "SmoothCheckBox";

    private static final String CHECK_VALUE = "check_value";

    private static final String INSTANCE_STATE = "instance_state";

    private static final float MIN_SCALE = 0.8f;//变化大小
    /**
     * 圆环和背景的画笔
     */
    private Paint mCirclePaint = null;

    private float mMinScale = MIN_SCALE;
    /**
     * 对号的画笔
     */
    private Paint mCorrectPaint = null;

    /**
     * 半径, 根据设置的宽高
     */
    private float mRadius;

    private float mCurrentValue;
    private float mCurrentProgress;
    private boolean mIsInDrawBackground;
    /**
     * 中心点
     */
    private float centerX, centerY;
    private float[] mPoint;

    /**
     * 动画时长
     */
    private int mCircleDuration;
    private int mCorrectDuration;

    /**
     * 背景颜色， 对勾颜色
     */
    private int mBgCheckedColor;
    private int mBgUnCheckedColor;
    private int mCorrectColor;

    /**
     * 对勾的宽度
     */
    private int mCorrectWidth;

    /**
     * 当前是否为选中状态
     */
    private boolean mChecked;

    private boolean misInAnim;

    private AnimatorSet set = null;

    private ArgbEvaluator argbEvaluator = null;

    /**
     * 是否在动画完成后调用OnCheckChange
     */
    private boolean mIsActionAfterAnim;

    private OnCheckedChangeListener mListener;

    public interface OnCheckedChangeListener{

        void OnCheckChange(SmoothCheckBox smoothCheckBox, boolean checked);
    }

    public SmoothCheckBox(Context context) {
        this(context, null);
    }

    public SmoothCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmoothCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private void init(Context context) {

        mChecked = false;

        misInAnim = false;

        mIsActionAfterAnim = true;

        mIsInDrawBackground = true;

        mCircleDuration = 150;
        mCorrectDuration = 150;

        mPoint = new float[6];

        mCorrectWidth = dip2px(1);
        mBgCheckedColor = Color.BLUE;//颜色设定
        mBgUnCheckedColor = Color.LTGRAY;
        mCorrectColor = Color.WHITE;

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mBgUnCheckedColor);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mCorrectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCorrectPaint.setColor(mCorrectColor);
        mCorrectPaint.setStyle(Paint.Style.FILL);
        mCorrectPaint.setStrokeWidth(mCorrectWidth);
        argbEvaluator = new ArgbEvaluator();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!misInAnim) {
                    setChecked(!mChecked);
                }
            }
        });

        mCurrentValue = 0f;
    }


    public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
        mListener = listener;
    }


    public void setChecked(boolean checked){

        if(mChecked != checked) {

            mChecked = checked;
            start();
        }

    }

    public boolean IsChecked(){
        return mChecked;
    }

    private void start(){

        if(!mIsActionAfterAnim
                && mListener != null){

            mListener.OnCheckChange(this, mChecked);
        }

        showAnimation();
    }

    private void showAnimation() {

        final ValueAnimator bgAnimator = showBackground();

        final ValueAnimator correctAnimator = showCorrectView();

        if(misInAnim && set != null){
            set.cancel();
            if(mIsActionAfterAnim && mListener != null){
                mListener.OnCheckChange(SmoothCheckBox.this, !mChecked);
            }
        }

        set = new AnimatorSet();

        if(mChecked)
        {
            mIsInDrawBackground = true;
            set.playSequentially(bgAnimator, correctAnimator);

        }else{

            mIsInDrawBackground = false;
            set.playSequentially(correctAnimator, bgAnimator);
        }

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                misInAnim = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mIsActionAfterAnim && mListener != null){
                    mListener.OnCheckChange(SmoothCheckBox.this, mChecked);
                }
                misInAnim = false;
            }
        });
        set.start();
    }

    /**
     * 画对勾
     * @return
     */
    private ValueAnimator showCorrectView() {
        ValueAnimator animation = mChecked
                ? ValueAnimator.ofFloat(0, 1f)
                : ValueAnimator.ofFloat(1, 0f);

        animation.setDuration(mCorrectDuration);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                mCurrentProgress = (float)animation.getAnimatedValue();

                invalidate();
            }
        });
        return animation;
    }

    /**
     * 画背景
     * @return
     */
    private ValueAnimator showBackground() {

        final float endScale_1 = 1f;
        final float endScale_2 = 0f;
        ValueAnimator animation = mChecked
                ? ValueAnimator.ofFloat(endScale_2, endScale_1)
                : ValueAnimator.ofFloat(endScale_1, endScale_2);

        animation.setDuration(mCircleDuration);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentValue = (float)animation.getAnimatedValue();
                invalidate();

                if(mChecked && mCurrentValue == endScale_1){
                    mIsInDrawBackground = false;
                }else if(!mChecked && mCurrentValue == endScale_2){
                    mIsInDrawBackground = true;
                }
            }
        });
        return animation;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mCirclePaint.setColor((int)argbEvaluator.evaluate(mCurrentValue, (Object) mBgUnCheckedColor, (Object) mBgCheckedColor));

        canvas.drawCircle(centerX, centerY, mRadius * (mMinScale + (1 - mMinScale) * mCurrentValue), mCirclePaint);

        if(!mIsInDrawBackground){

            if (mCurrentProgress < 1 / 3f) {

                float x = mPoint[0] + (mPoint[2] - mPoint[0]) * mCurrentProgress * 3;
                float y = mPoint[1] + (mPoint[3] - mPoint[1]) * mCurrentProgress * 3;

                canvas.drawLine(mPoint[0], mPoint[1], x, y, mCorrectPaint);

            } else {

                float x = mPoint[2] + (mPoint[4] - mPoint[2]) * (mCurrentProgress - 1 / 3f) * 1.5f;
                float y = mPoint[3] + (mPoint[5] - mPoint[3]) * (mCurrentProgress - 1 / 3f) * 1.5f;

                canvas.drawLine(mPoint[0], mPoint[1], mPoint[2], mPoint[3], mCorrectPaint);
                canvas.drawLine(mPoint[2], mPoint[3], x, y, mCorrectPaint);
            }
        }

    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2f;

        centerX = mRadius;
        centerY = mRadius;

        mPoint[0] = centerX / 2f;
        mPoint[1] = centerY;
        mPoint[2] = centerX * 5 / 6f;
        mPoint[3] = centerY + centerX / 3f;
        mPoint[4] = centerX * 3 / 2f;
        mPoint[5] = centerY - centerX / 3f;
    }

    public void setBackgroundCheckedColor(int mBackgroundColor) {
        this.mBgCheckedColor = mBackgroundColor;
    }

    public void setBackgroundUnCheckedColor(int mBackgroundColor) {
        this.mBgUnCheckedColor = mBackgroundColor;
    }

    public void setCorrectColor(int mCorrectColor) {
        this.mCorrectColor = mCorrectColor;
        mCorrectPaint.setColor(mCorrectColor);
    }

    public void setCorrectWidth(int dp) {
        this.mCorrectWidth = dip2px(dp);
        mCorrectPaint.setStrokeWidth(mCorrectWidth);
    }


    public void setCircleDuration(int mCircleDuration) {
        this.mCircleDuration = mCircleDuration;
    }

    public void setCorrectDuration(int mCorrectDuration) {
        this.mCorrectDuration = mCorrectDuration;
    }

    public void setMinScale(float scale){
        if(scale <= 0.5f && scale >= 0.0f)
            this.mMinScale = scale;
    }
    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putBoolean(CHECK_VALUE, mChecked);
        b.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            mChecked = bundle.getBoolean(CHECK_VALUE);
            if(!mChecked){
                mCurrentValue = 0f;
                invalidate();
            }else{
                mCurrentValue = 1f;
                mIsInDrawBackground = false;
                mCurrentProgress = 1f;
                invalidate();
            }
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
        }else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    private void release() {

        if(mCirclePaint != null){
            mCirclePaint = null;
        }

        if(mCorrectPaint != null){
            mCorrectPaint = null;
        }

        if(set != null){
            set.cancel();
            set = null;
        }

        if(argbEvaluator != null){
            argbEvaluator = null;
        }
    }
}