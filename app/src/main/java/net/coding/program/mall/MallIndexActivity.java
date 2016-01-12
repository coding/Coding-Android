package net.coding.program.mall;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.MallBannerObject;
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

    final String BANNER_URL = Global.HOST_API + "/gifts/sliders";

    final String TAG_BANNER = "TAG_BANNER";

    private ArrayList<MallBannerObject> mBannerData = new ArrayList<>();

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
//        getNetwork(BANNER_URL, TAG_BANNER);
    }

    static class MyPagerAdapter extends SaveFragmentPagerAdapter {

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

//    private void initBannerData() {
//        ((ViewPager) banner.findViewById(R.id.cbLoopViewPager)).setOnPageChangeListener(
//                new ViewPager.OnPageChangeListener() {
//                    @Override
//                    public void onPageScrolled(int position, float positionOffset,
//                            int positionOffsetPixels) {
//                    }
//
//                    @Override
//                    public void onPageSelected(int position) {
//                        bannerIndicator.setSelect(position);
//                    }
//
//                    @Override
//                    public void onPageScrollStateChanged(int state) {
//
//                    }
//                });
//    }
//
//    private void updateBannerData() {
//        if (mBannerData.isEmpty()) {
//            mBannerData.add(new MallBannerObject());
//        }
//        banner.setPages(new CBViewHolderCreator() {
//            @Override
//            public Object createHolder() {
//                return new LocalImageHolder();
//            }
//        }, mBannerData);
//
//        int bannerStartPos = 0;
//        bannerIndicator.setCount(mBannerData.size(), bannerStartPos);
//    }
//
//    class LocalImageHolder implements CBPageAdapter.Holder<MallBannerObject> {
//
//        ImageView imageView;
//
//        @Override
//        public View createView(Context context) {
//            imageView = new ImageView(MallIndexActivity.this);
//            imageView.setLayoutParams(
//                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.MATCH_PARENT));
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            return imageView;
//        }
//
//        @Override
//        public void UpdateUI(Context context, int position, MallBannerObject data) {
//            imageView.setTag(R.id.image, position);
//            getImageLoad().loadImage(imageView, mBannerData.get(position).getImage(),
//                    ImageLoadTool.bannerOptions);
//        }
//    }

}
