package com.caocong.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caocong 2016-10-18 qq:648749695
 */
public class CircleProgressView extends View {
    private static final String TAG = CircleProgressView.class.getSimpleName();
    //边框宽度
    private static final int BORDER_WIDTH = 14;
    //三角形高度
    private static final int TRIANGLE_HEIGHT = 14;
    //初始颜色，背景色，三角形颜色，进度颜色
    private int mBorderColor, mTriAngleColor, mProgressColor;
    //进度文字颜色
    private int mProgressTextSize;
    //进度
    private float mProgress;
    //半径
    private int mRadius;
    //画笔,背景圆,椭圆,进度条
    private Paint mBorderPaint, mArcPaint, mProgressPaint, mTriAnglePaint, mTextPaint;
    //火箭的坐标
    private int mRocketBottom;
    //第一次
    private boolean firstTime = true;
    //火箭
    private Bitmap mRocketBitmap;
    //火箭宽高
    private int mRocketWidth, mRocketHeight;

    //火箭上下移动动画
    private RocketMoveAnimator mRocketMoveAnimtor;
    //火箭发射动画
    private AnimatorSet mRocketLaunchAnimatorSet;

    //计算进度的装配器
    private OnObtainProgressListener mObtainProgessListener;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initParams();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
        mBorderColor = array.getColor(R.styleable.CircleProgressView_borderColor, Color.argb(122, 255, 255, 255));
        mTriAngleColor = array.getColor(R.styleable.CircleProgressView_triangleColor, Color.WHITE);
        mProgressColor = array.getColor(R.styleable.CircleProgressView_progressCircleColor, Color.WHITE);
        mProgressTextSize = (int) array.getDimension(R.styleable.CircleProgressView_progressTextSize, 30.0f);
        array.recycle();
    }


    private void initParams() {

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(BORDER_WIDTH);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAntiAlias(true);

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.FILL);
        mArcPaint.setColor(Color.WHITE);
        mArcPaint.setColor(mBorderColor);
        mArcPaint.setAntiAlias(true);

        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(BORDER_WIDTH);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setAntiAlias(true);

        mTriAnglePaint = new Paint();
        mTriAnglePaint.setStyle(Paint.Style.FILL);
        mTriAnglePaint.setColor(mTriAngleColor);
        mTriAnglePaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(mProgressColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);

        mRocketBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rocket);
        mRocketWidth = mRocketBitmap.getWidth();
        mRocketHeight = mRocketBitmap.getHeight();


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                widthSize = measureRadius(getContext()) * 2;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                heightSize = measureRadius(getContext()) * 2;
                break;
        }
        mRadius = Math.min(widthSize, heightSize) / 2;
        widthSize = heightSize = getTotalDiamter();
        setMeasuredDimension(widthSize, heightSize);
        mRocketBottom = getCicleDiameter() + BORDER_WIDTH / 2;

        if (firstTime) {
            mRocketMoveAnimtor = new RocketMoveAnimator();
            mRocketMoveAnimtor.startAnim();
            firstTime = false;
        }
    }

    private int measureRadius(Context context) {
        Resources resources = context.getApplicationContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        return Math.min(width, height) / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getAdditionWidth(), getAdditionWidth());
        //画背景，不需要动的部分
        drawBackBorder(canvas);
        //绘制进度条
        drawProgress(canvas);
        //绘制三角形
        drawTriangle(canvas);
        //绘制文字
        drawText(canvas);
        //绘制火箭
        drawRocket(canvas);
    }

    private void drawBackBorder(Canvas canvas) {
        //画圆弧
        //circleCanvas.translate(getAdditionWidth(), getAdditionWidth());
        RectF circleRf = new RectF(0, 0, getCicleDiameter(), getCicleDiameter());
        canvas.drawArc(circleRf, 120, 300, false, mBorderPaint);
        //画椭圆
        canvas.save();
        canvas.translate(getCircleRadius(), getCircleRadius());
        float y = (float) Math.sin(60 * Math.PI / 180) * getCircleRadius() + 4;
        RectF ovalRf = new RectF(-(getCircleRadius() / 2) - 1, y - 30, (getCircleRadius()) / 2 + 1, y + 30);
        canvas.drawOval(ovalRf, mArcPaint);
        canvas.restore();

    }

    private void drawProgress(Canvas canvas) {
        //画圆弧
        RectF circleRf = new RectF(0, 0, getCicleDiameter(), getCicleDiameter());
        float sweepAngle = mProgress * 1.0f / 100 * 300;
        if (sweepAngle > 0) {
            canvas.drawArc(circleRf, 120, sweepAngle, false, mProgressPaint);
        }
    }

    private void drawTriangle(Canvas canvas) {
        canvas.save();
        canvas.translate(getCircleRadius(), getCircleRadius());
        float angle = mProgress * 1.0f / 100 * 300;
        canvas.rotate(30 + angle);
        Path path = new Path();
        path.moveTo(0, getCircleRadius() + BORDER_WIDTH / 2);
        path.lineTo(TRIANGLE_HEIGHT, getTotalRadius());
        path.lineTo(-TRIANGLE_HEIGHT, getTotalRadius());
        path.close();
        canvas.drawPath(path, mTriAnglePaint);
        canvas.restore();
    }


    private void drawText(Canvas canvas) {
        //画数字
        Rect rect = new Rect(0, 0, getCicleDiameter(), getCicleDiameter());
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mProgressTextSize);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int baseLineY = (int) (rect.centerY() - top / 2 - bottom / 2);
        String percent = String.valueOf((int) mProgress);
        canvas.drawText(percent, rect.centerX(), baseLineY, mTextPaint);
        //画%
        float textWidth = mTextPaint.measureText(percent);
        RectF rect2 = new RectF(getCircleRadius() + textWidth / 2 + 10, 0, getCicleDiameter(), getCicleDiameter());
        mTextPaint.setTextSize(30);
        canvas.drawText("%", rect2.left, baseLineY, mTextPaint);

    }

    private void drawRocket(Canvas canvas) {
        canvas.save();
        canvas.translate(getCircleRadius(), 0);
        canvas.drawBitmap(mRocketBitmap, -mRocketWidth / 2, mRocketBottom - mRocketHeight, null);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            handleActionUp();
        }
        return true;
    }

    /**
     * 点击后的处理
     */
    private void handleActionUp() {
        if (mRocketLaunchAnimatorSet != null && mRocketLaunchAnimatorSet.isRunning()) {
            return;
        }
        mRocketMoveAnimtor.stopAnim();
        List<Animator> launchAnim = getRocketLanuchAnimator();
        Animator progressAnim1 = getProgressAnimator(getProgress(), 0);
        Animator progressAnim2 = null;
        if (mObtainProgessListener != null) {
            progressAnim2 = getProgressAnimator(0, mObtainProgessListener.onObtainProgress());
        }

        List<Animator> combineAnim = new ArrayList<>(launchAnim);
        combineAnim.add(progressAnim1);
        if (progressAnim2 != null) {
            combineAnim.add(progressAnim2);
        }
        mRocketLaunchAnimatorSet = new AnimatorSet();
        mRocketLaunchAnimatorSet.playSequentially(combineAnim);
        mRocketLaunchAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRocketMoveAnimtor.reStart();
            }
        });
        mRocketLaunchAnimatorSet.start();
    }

    /**
     * 获得进度
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * 设置进度
     */
    public void setProgress(float progress) {
        setProgress(progress, false);
    }


    /**
     * 设置进度，是否使用动画
     */
    public void setProgress(float progress, boolean useAnim) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("progress is wrong");
        }
        if (useAnim) {
            startProgressAnimator(progress);

        } else {
            mProgress = progress;
            invalidate();
        }

    }


    /**
     * 属性动画，刷新进度
     */
    private void startProgressAnimator(final float progress) {
        ValueAnimator mProgressAnimator = getProgressAnimator(mProgress, progress);
        mProgressAnimator.start();
    }

    /**
     * 获得进度动画
     */
    private ValueAnimator getProgressAnimator(float fromProgress, float toProgress) {
        long animatorDuration = (long) Math.abs(fromProgress - toProgress) * 10;
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(fromProgress, toProgress).setDuration(animatorDuration);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                invalidate();
            }

        });
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        return progressAnimator;
    }

    /**
     * 火箭发射的动画,三个动画的组合
     */
    private List<Animator> getRocketLanuchAnimator() {
        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRocketBottom = (int) animation.getAnimatedValue();
                invalidate();
            }
        };
        //向上
        int maxBottom = getTotalDiamter();
        int minBottom = getTotalDiamter() - 80;
        long toUpDur = Math.abs(mRocketBottom - minBottom) * 5;
        ValueAnimator toUpAnim = ValueAnimator.ofInt(mRocketBottom, minBottom).setDuration(toUpDur);
        toUpAnim.addUpdateListener(updateListener);
        //向下
        long toDownDur = Math.abs(minBottom - maxBottom) * 5;
        ValueAnimator toDownAnim = ValueAnimator.ofInt(minBottom, maxBottom).setDuration(toDownDur);
        toDownAnim.addUpdateListener(updateListener);
        //发射
        long toLaunchDur = Math.abs(minBottom - maxBottom) * 5;
        ValueAnimator launchAnim = ValueAnimator.ofInt(maxBottom, -(getAdditionWidth() + mRocketHeight + 10)).setDuration(toLaunchDur);
        launchAnim.addUpdateListener(updateListener);

        ArrayList<Animator> animators = new ArrayList<Animator>();
        animators.add(toUpAnim);
        animators.add(toDownAnim);
        animators.add(launchAnim);
        return animators;

    }


    /**
     * 获得圆直径
     */
    private int getCicleDiameter() {
        return getCircleRadius() * 2;
    }

    /**
     * 获得圆半径
     */
    private int getCircleRadius() {
        return mRadius;
    }

    /**
     * 获得总直径
     */
    private int getTotalDiamter() {
        return getTotalRadius() * 2;
    }

    /**
     * 获得总半径
     */
    private int getTotalRadius() {
        return mRadius + getAdditionWidth();
    }

    /**
     * 设置监听器
     */
    public void setOnObtainProgressListener(OnObtainProgressListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can not be null");
        }
        mObtainProgessListener = listener;
    }

    /**
     * 获得圆半径之外的宽度
     */
    private int getAdditionWidth() {
        return BORDER_WIDTH / 2 + TRIANGLE_HEIGHT;

    }


    /**
     * 火箭上下移动动画
     */
    private class RocketMoveAnimator extends ValueAnimator {
        //初始值，终止值
        private int from, to;
        private ValueAnimator anim;
        private boolean run = true;

        public RocketMoveAnimator() {
            super();
            int maxBottom = getCicleDiameter() + (BORDER_WIDTH / 2) + TRIANGLE_HEIGHT;
            int minBottom = getCicleDiameter() + (BORDER_WIDTH / 2) - 40;
            from = maxBottom;
            to = minBottom;
        }

        private void startAnim() {
            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (run) {
                        mRocketBottom = (int) animation.getAnimatedValue();
                        invalidate();
                    }
                }
            };
            AnimatorListenerAdapter adapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //切换初始值，终止值，重复动画
                    int k = from;
                    from = to;
                    to = k;
                    if (run) {
                        startAnim();
                    }

                }
            };

            anim = ValueAnimator.ofInt(from, to).setDuration(1000);
            anim.addUpdateListener(updateListener);
            anim.addListener(adapter);
            anim.start();

        }

        private void stopAnim() {
            run = false;
            if (isRunning()) {
                cancel();
            }
        }

        private void reStart() {
            run = true;
            startAnim();
        }

    }

    /**
     * 得到进度
     */
    public interface OnObtainProgressListener {
        /**
         * 计算进度
         */
        int onObtainProgress();
    }
}
