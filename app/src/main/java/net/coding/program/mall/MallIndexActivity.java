package net.coding.program.mall;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_mall)
@OptionsMenu(R.menu.menu_mall_index)
public class MallIndexActivity extends RefreshBaseActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    WechatTab mallTab;

    @ViewById
    ViewPager viewpager;

    @ViewById
    View blankLayout;

    @OptionsItem
    void action_order() {
        MallOrderDetailActivity_.intent(this).start();
    }

    @AfterViews
    void initView() {
        setActionBarTitle("商城");

        setupViewPager(viewpager);
        mallTab.setViewPager(viewpager);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    private void setupViewPager(ViewPager mViewPager) {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(
                MallListFragment_.builder().mType(MallListFragment.Type.all_goods).build(),
                "全部商品");
        adapter.addFragment(
                MallListFragment_.builder().mType(MallListFragment.Type.can_change).build(),
                "可兑换商品");
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
    }

    static class MyPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<>();

        private final List<String> mFragmentTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
