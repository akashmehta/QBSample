package com.example.abc.qbsample.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.example.abc.qbsample.R;

/**
 * Created by abc on 5/6/2016.
 */
public class MyEditText extends EditText {
    int position = 3;
    public MyEditText(Context context) {
        super(context);

    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, attrs);
    }
    private void setCustomFont(final Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.MyEditText);
        String customFont = a.getString(R.styleable.MyEditText_editTextCustomFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(ctx.getAssets(), asset);
        } catch (Exception e) {
            return false;
        }

        setTypeface(tf);
        return true;
    }
}
