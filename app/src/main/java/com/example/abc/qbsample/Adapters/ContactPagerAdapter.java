package com.example.abc.qbsample.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;
import com.example.abc.qbsample.CoreFragments.FriendContactListFragment;
import com.example.abc.qbsample.CoreFragments.RecentContactListFragment;
import com.example.abc.qbsample.R;

/**
 * Created by abc on 5/13/2016.
 */
public class ContactPagerAdapter extends FragmentStatePagerAdapter implements PagerSlidingTabStrip.IconTabProvider {
    PagerSlidingTabStrip tabStrip;
    ViewPager currentPager;

    public ContactPagerAdapter(FragmentManager fm, PagerSlidingTabStrip tabStrip, ViewPager currentPager) {
        super(fm);
        this.tabStrip = tabStrip;
        this.currentPager = currentPager;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RecentContactListFragment();
            case 1:
                return new FriendContactListFragment();
            /*case 2:
                return new AllContactListFragment();*/
            default:
                return new RecentContactListFragment();
        }
    }


    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public int getPageIconResId(int position) {
        switch (position) {
            case 0:
                if (currentPager.getCurrentItem() == position) {
                    return R.drawable.recent;
                } else {
                    return R.drawable.recent_hover;
                }

            case 1:
                if (currentPager.getCurrentItem() == position) {
                    return R.drawable.allcontact;
                } else {
                    return R.drawable.allcontact_hover;
                }
            default:
                return R.drawable.recent;
        }

    }
}
