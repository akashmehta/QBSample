package com.skyfishjy.library;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by fyu on 11/3/14.
 */

public class RippleBackground extends RelativeLayout{

    private static final int DEFAULT_RIPPLE_COUNT=6;
    private static final int DEFAULT_DURATION_TIME=3000;
    private static final float DEFAULT_SCALE=6.0f;
    private static final int DEFAULT_FILL_TYPE=0;
    private int rippleColor;
    private float rippleStrokeWidth;
    private float rippleRadius;
    private int rippleDurationTime;
    private int rippleAmount;
    private int rippleDelay;
    private float rippleScale;
    private int rippleType;
    private Paint paint;
    private boolean animationRunning=false;
    private AnimatorSet animatorSet;
    private ArrayList<Animator> animatorList;
    private LayoutParams rippleParams;
    private View dottedView ,dottedView2;
    private ArrayList<RippleView> rippleViewList=new ArrayList<RippleView>();

    public RippleBackground(Context context) {
        super(context);

    }

    private class AnimListener implements Animation.AnimationListener{
        View view;
        @Override
        public void onAnimationStart(Animation animation) {
            view.setAlpha(0.5f);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setAlpha(0f);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    public RippleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {

        if (isInEditMode())
            return;

        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view,");
        }

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground);
        rippleColor=typedArray.getColor(R.styleable.RippleBackground_rb_color, getResources().getColor(R.color.rippelColor));
        rippleStrokeWidth=typedArray.getDimension(R.styleable.RippleBackground_rb_strokeWidth, getResources().getDimension(R.dimen.rippleStrokeWidth));
        rippleRadius=typedArray.getDimension(R.styleable.RippleBackground_rb_radius, getResources().getDimension(R.dimen.rippleRadius));
        rippleDurationTime=typedArray.getInt(R.styleable.RippleBackground_rb_duration, DEFAULT_DURATION_TIME);
        rippleAmount=typedArray.getInt(R.styleable.RippleBackground_rb_rippleAmount, DEFAULT_RIPPLE_COUNT);
        rippleScale=typedArray.getFloat(R.styleable.RippleBackground_rb_scale, DEFAULT_SCALE);
        rippleType=typedArray.getInt(R.styleable.RippleBackground_rb_type, DEFAULT_FILL_TYPE);
        typedArray.recycle();

        rippleDelay=rippleDurationTime/rippleAmount;

        AnimListener listener = new AnimListener(),listener2 = new AnimListener();

        dottedView = new DotView(context,300);
        dottedView.setAlpha(0);
        dottedView.setVisibility(INVISIBLE);
        addView(dottedView);
        AlphaAnimation alphaAnimator1 = new AlphaAnimation(0.5f,0);
        alphaAnimator1.setRepeatCount(ObjectAnimator.INFINITE);
        alphaAnimator1.setRepeatMode(ObjectAnimator.RESTART);
        alphaAnimator1.setStartOffset(2 * rippleDelay);
        alphaAnimator1.setDuration(rippleDurationTime);
        listener.view = dottedView;
        alphaAnimator1.setAnimationListener(listener);
        dottedView.startAnimation(alphaAnimator1);

        dottedView2 = new DotView(context,350);
        dottedView2.setAlpha(0);
        dottedView2.setVisibility(INVISIBLE);
        addView(dottedView2);
        AlphaAnimation alphaAnimator2 = new AlphaAnimation(0.5f,0);
        alphaAnimator2.setRepeatCount(ObjectAnimator.INFINITE);
        alphaAnimator2.setRepeatMode(ObjectAnimator.RESTART);
        alphaAnimator2.setStartOffset(3 * rippleDelay);
        alphaAnimator2.setDuration(rippleDurationTime);
        listener2.view = dottedView2;
        alphaAnimator2.setAnimationListener(listener2);
        dottedView2.startAnimation(alphaAnimator2);

        paint = new Paint();
        paint.setAntiAlias(true);
        if(rippleType==DEFAULT_FILL_TYPE){
            rippleStrokeWidth=0;
            paint.setStyle(Paint.Style.FILL);
        }else
            paint.setStyle(Paint.Style.STROKE);
        paint.setColor(rippleColor);

        rippleParams=new LayoutParams((int)(2*(rippleRadius+rippleStrokeWidth)),(int)(2*(rippleRadius+rippleStrokeWidth)));
        rippleParams.addRule(CENTER_IN_PARENT, TRUE);

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorList=new ArrayList<Animator>();

        for(int i=0;i<rippleAmount;i++){
            RippleView rippleView=new RippleView(getContext());
            addView(rippleView,rippleParams);
            rippleViewList.add(rippleView);
             final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale);
            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(i * rippleDelay);
            scaleXAnimator.setDuration(rippleDurationTime);
            animatorList.add(scaleXAnimator);
            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale);
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleYAnimator.setStartDelay(i * rippleDelay);
            scaleYAnimator.setDuration(rippleDurationTime);
            animatorList.add(scaleYAnimator);
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f);
            alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(i * rippleDelay);
            alphaAnimator.setDuration(rippleDurationTime);
            animatorList.add(alphaAnimator);
        }

        animatorSet.playTogether(animatorList);
    }

    private class DotView extends View{
        private int Radius;
        public DotView(Context context,int Radius){
            super(context);
            this.Radius = Radius;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint p = new Paint();
            p.setColor(Color.parseColor("#0071bb"));
            DashPathEffect pathEffect  = new DashPathEffect(new float[]{3,3},(float)10);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setPathEffect(pathEffect);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(getWidth()/2, getHeight()/2, Radius, p);
            invalidate();
        }
    }

    private class RippleView extends View{

        public RippleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int radius=(Math.min(getWidth(),getHeight()))/2;
            canvas.drawCircle(radius,radius,radius-rippleStrokeWidth,paint);
        }
    }

    public void startRippleAnimation(){
        if(!isRippleAnimationRunning()){
            for(RippleView rippleView:rippleViewList){
                rippleView.setVisibility(VISIBLE);
            }
            animatorSet.start();
            animationRunning=true;
        }
    }

    public void stopRippleAnimation(){
        if(isRippleAnimationRunning()){
            animatorSet.end();
            animationRunning=false;
        }
    }

    public boolean isRippleAnimationRunning(){
        return animationRunning;
    }
}
