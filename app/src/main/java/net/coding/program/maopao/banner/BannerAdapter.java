package net.coding.program.maopao.banner;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.coding.program.model.BannerObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 15/7/30.
 * 广告 Fragment，用于左右滑动
 */
public class BannerAdapter extends FragmentPagerAdapter {

    List<BannerObject> mData = new ArrayList<>();

    public BannerAdapter(FragmentManager fm, List<BannerObject> data) {
        super(fm);
        mData = data;
    }

    @Override
    public Fragment getItem(int position) {
        BannerObject banner = mData.get(position);
        return BannerItemFragment_.builder()
                .data(banner)
                .build();
    }

    @Override
    public int getCount() {
        return mData.size();
    }
}
