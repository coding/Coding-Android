package net.coding.program.maopao.banner;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by chenchao on 15/8/7.
 * 循环广告条
 */
public class CirculatePagerView extends FragmentStatePagerAdapter {

    public CirculatePagerView(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
