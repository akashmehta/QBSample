package com.example.abc.qbsample.Utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

/**
 * Created by abc on 6/4/2016.
 */
public class AnimationUtils {
    private static Animation increaseSize, decreaseSize;
    private static float dRate = 0.98f;
    private static int duration = 70;
    public static Animation getIncreaseAnimation(final View view){
        increaseSize = new ScaleAnimation(dRate,(float)1,dRate,(float)1);
        increaseSize.setFillEnabled(true);
        increaseSize.setFillAfter(true);
        increaseSize.setDuration(duration);
        return increaseSize;
    }
    public static Animation getDecreaseAnimation(final View view){
        decreaseSize = new ScaleAnimation((float)1.0,dRate ,(float) 1.0,dRate,150,view.getPivotY());
        decreaseSize.setFillEnabled(true);
        decreaseSize.setFillAfter(true);
        decreaseSize.setDuration(duration);
        return decreaseSize;
    }
    public static Animation callerAnimation(){
        Animation scaleAnimation = new ScaleAnimation(1,1.5f,1,1.5f) ;
        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(3000);

        Animation alphaAnimation = new AlphaAnimation(1,0.5f);
        alphaAnimation.setFillEnabled(true);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(3000);

        return null;
    }
}
