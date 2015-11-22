package net.coding.program.mall;

import com.bigkoo.convenientbanner.CBPageAdapter;
import com.bigkoo.convenientbanner.CBViewHolderCreator;
import com.bigkoo.convenientbanner.ConvenientBanner;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.guide.IndicatorView;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.MallBannerObject;
import net.coding.program.model.MallItemObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libo on 2015/11/20.
 */

@EActivity(R.layout.activity_mall_index)
@OptionsMenu(R.menu.menu_mall_index)
public class MallIndexActivity extends RefreshBaseActivity
        implements MallListFragment.FragmentCallback {

    @ViewById
    WechatTab mallTab;

    String[] fragmentTitles;

    @ViewById
    ViewPager mallIndexViewpager;

    @ViewById
    View blankLayout;

    @ViewById(R.id.bannerViewPager)
    ConvenientBanner banner;

    @ViewById(R.id.indicatorView)
    IndicatorView bannerIndicator;

    ArrayList<MallItemObject> list1Data = new ArrayList<>();

    private ArrayList<MallBannerObject> mBannerData = new ArrayList<>();

    private double userPoint = 0.0;

    final String TAG_BANNER = "TAG_BANNER";

    final String TAG_MALL_LIST = "TAG_MALL_LIST";

    final String TAG_USER_POINT = "TAG_USER_POINT";

    static final String DATA_URL = Global.HOST_API + "/gifts";

    final String BANNER_URL = Global.HOST_API + "/gifts/sliders";

    final String USER_POINT_URL = Global.HOST_API + "/account/points";

    private List<Fragment> fragmentList = new ArrayList<>();

    private MyPageAdapter pageAdapter;

    @OptionsItem
    void action_order() {
        //todo lll fix target
        MallOrderDetailActivity_.intent(this).start();
    }

    @Override
    public void onRefresh() {
        initSetting();
        getNetwork(BANNER_URL, TAG_BANNER);
        getNetwork(DATA_URL, TAG_MALL_LIST);
        getNetwork(USER_POINT_URL, TAG_USER_POINT);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if (banner != null) {
            banner.startTurning(5000);
        }
    }

    @AfterViews
    protected void initMallIndexFragment() {
        init();

    }

    private void init() {
        initRefreshBaseActivity();
        fragmentTitles = getResources().getStringArray(R.array.mall_index_title);

        if (mBannerData.isEmpty()) {
            showDialogLoading();
        } else {
            setRefreshing(true);
        }

        mBannerData.addAll(AccountInfo.getMallBanners(this));
        initBannerData();
        updateBannerData();

        getNetwork(BANNER_URL, TAG_BANNER);
        getNetwork(DATA_URL, TAG_MALL_LIST);
        getNetwork(USER_POINT_URL, TAG_USER_POINT);
    }

    @Override
    public void fragmentCallBack(int viewHeight) {
        if (mallIndexViewpager != null) {
            ViewGroup.LayoutParams params = mallIndexViewpager.getLayoutParams();
            params.height = viewHeight;
            mallIndexViewpager.setLayoutParams(params);
        }
    }

    class MyPageAdapter extends SaveFragmentPagerAdapter {

        List<Fragment> fragmentList;

        public void replaceList(List<Fragment> list) {
            fragmentList.clear();
            fragmentList.addAll(list);
        }

        public MyPageAdapter(FragmentManager fm,
                List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws
            JSONException {
        hideProgressDialog();
        setRefreshing(false);

        if (tag.equals(TAG_BANNER)) {
            if (code == 0) {
                BlankViewDisplay
                        .setBlank(mBannerData.size(), this, true, blankLayout, onClickRetry);

                ArrayList<MallBannerObject> banners = new ArrayList<>();
                JSONArray jsonArray = response.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    banners.add(new MallBannerObject(jsonArray.getJSONObject(i)));
                }
                AccountInfo.saveMallBanners(this, banners);

                mBannerData.clear();
                mBannerData.addAll(banners);
                updateBannerData();
            } else {
                BlankViewDisplay
                        .setBlank(mBannerData.size(), this, false, blankLayout, onClickRetry);
                super.parseJson(code, response, tag, pos, data);
            }
        }
        if (tag.equals(TAG_USER_POINT)) {
            if (code == 0) {
                JSONObject jsonObject = response.getJSONObject("data");
                userPoint = jsonObject.optDouble("points_left");
            } else {
                super.parseJson(code, response, tag, pos, data);
            }
        }
        if (tag.equals(TAG_MALL_LIST)) {
            if (code == 0) {
                BlankViewDisplay
                        .setBlank(mBannerData.size(), this, true, blankLayout, onClickRetry);

                ArrayList<MallItemObject> mallItemObjects = new ArrayList<>();
                JSONObject obj = response.getJSONObject("data");
                JSONArray jsonArray = obj.getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    mallItemObjects.add(new MallItemObject(jsonArray.getJSONObject(i)));
                }

                ArrayList<String> strList = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    strList.add("str" + i);
                }

                Fragment mallListFragment = MallListFragment_.builder().rawListData(mallItemObjects)
                        .stringList(strList)
                        .testStr("1234")
                        .userPoint(userPoint)
                        .build();
                Fragment mallListFragment1 = MallListFragment_.builder()
                        .rawListData(mallItemObjects)
                        .stringList(strList)
                        .testStr("5678")
                        .userPoint(userPoint)
                        .build();
                fragmentList.clear();
                fragmentList.add(mallListFragment);
                fragmentList.add(mallListFragment1);

                pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragmentList);
                mallIndexViewpager.setAdapter(pageAdapter);

                mallTab.setViewPager(mallIndexViewpager);

                mallIndexViewpager.setCurrentItem(0);

            } else {
                BlankViewDisplay
                        .setBlank(mBannerData.size(), this, false, blankLayout, onClickRetry);
                super.parseJson(code, response, tag, pos, data);
            }
        }
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    private void updateBannerData() {
        if (mBannerData.isEmpty()) {
            mBannerData.add(new MallBannerObject());
        }
        banner.setPages(new CBViewHolderCreator() {
            @Override
            public Object createHolder() {
                return new LocalImageHolder();
            }
        }, mBannerData);

        int bannerStartPos = 0;
        bannerIndicator.setCount(mBannerData.size(), bannerStartPos);
    }

    class LocalImageHolder implements CBPageAdapter.Holder<MallBannerObject> {

        ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(MallIndexActivity.this);
            imageView.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, MallBannerObject data) {
            imageView.setTag(R.id.image, position);
            getImageLoad().loadImage(imageView, mBannerData.get(position).getImage(),
                    ImageLoadTool.bannerOptions);
        }
    }


    private void initBannerData() {
        ViewGroup.LayoutParams layoutParams = banner.getLayoutParams();

        banner.setLayoutParams(layoutParams);

        ((ViewPager) banner.findViewById(R.id.cbLoopViewPager)).setOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                            int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        bannerIndicator.setSelect(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
    }

}
