package com.example.abc.qbsample.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by abc on 5/10/2016.
 */
public class Profile_ImageView extends ImageView {
    public static float radius = 350.0f;
    private int margin = 15;
    public Profile_ImageView(Context context) {
        super(context);
    }

    public Profile_ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Profile_ImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

/*
    @Override
    protected void onDraw(Canvas canvas) {
        //float radius = 36.0f;
        Path clipPath = new Path();
        Path.FillType ft = Path.FillType.WINDING;
        clipPath.setFillType(ft);
        RectF rect = new RectF(margin, margin, this.getWidth()-margin, this.getHeight()-margin);
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
*/

}
