package net.coding.program.mall;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.UserObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by libo on 2015/11/22.
 */
@EActivity(R.layout.activity_mall_order_detail)
public class MallOrderDetailActivity extends BackActivity {

    @ViewById
    WechatTab mall_order_detail_tab;

    @ViewById
    ViewPager viewpager;

    String[] tabTitle;

    @AfterViews
    void initView() {
        tabTitle = getResources().getStringArray(R.array.mall_order_detail_tab);
        UserObject userObject = MyApp.sUserObject;

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewpager.setAdapter(adapter);
        mall_order_detail_tab.setViewPager(viewpager);
    }

    class MyPagerAdapter extends SaveFragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitle[position];
        }

        @Override
        public int getCount() {
            return tabTitle.length;
        }

        @Override
        public Fragment getItem(int position) {
            final MallOrderDetailFragment.Type types[] = new MallOrderDetailFragment.Type[]{
                    MallOrderDetailFragment.Type.all_order,
                    MallOrderDetailFragment.Type.un_send,
                    MallOrderDetailFragment.Type.already_send
            };
            Fragment fragment = MallOrderDetailFragment_.builder()
                    .mType(types[position])
                    .build();
            saveFragment(fragment);
            return fragment;
        }
    }
}
