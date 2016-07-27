package com.example.abc.qbsample.Utils;

import android.content.Context;

import com.astuetz.PagerSlidingTabStrip;

/**
 * Created by abc on 7/26/2016.
 */
public class MyPagerSlidingTabStrip extends PagerSlidingTabStrip {
    private Context mContext;
    public MyPagerSlidingTabStrip(Context context) {
        super(context);
        this.mContext = context;
    }

    public interface IconTabProvider {
        public int getPageIconResId(int position);
    }
}
