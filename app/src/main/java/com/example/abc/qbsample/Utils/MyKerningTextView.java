package com.example.abc.qbsample.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.andrognito.kerningview.KerningTextView;
import com.example.abc.qbsample.R;

/**
 * Created by abc on 6/16/2016.
 */
public class MyKerningTextView extends KerningTextView {
    public MyKerningTextView(Context context) {
        super(context);
    }

    public MyKerningTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public MyKerningTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.MyTextView);
        String customFont = a.getString(R.styleable.MyTextView_textViewCustomFont);
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
