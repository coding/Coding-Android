package net.coding.program.maopao;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.CBPageAdapter;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.BannerObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.util.LoadGifUtil;
import net.coding.program.guide.IndicatorView;
import net.coding.program.route.URLSpanNoUnderline;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

@EFragment(R.layout.fragment_maopao_list)
public class MaopaoListFragment extends MaopaoListBaseFragment {

    final String friendUrl = Global.HOST_API + "/tweet/public_tweets?last_id=%s&sort=friends&filter=true";
    final String myUrl = Global.HOST_API + "/tweet/user_public?user_id=%s&last_id=%s";
    final String TAG_BANNER = "TAG_BANNER";
    @FragmentArg
    Type mType;
    @FragmentArg
    int userId;
    @ViewById
    FloatingActionButton floatButton;
    ConvenientBanner banner;
    ArrayList<BannerObject> mBannerDatas = new ArrayList<>();
    private IndicatorView bannerIndicator;
    private TextView bannerName;
    private TextView bannerTitle;

    @Override
    protected String getMaopaoUrlFormat() {
        return Global.HOST_API + "/tweet/public_tweets?last_id=%s&sort=%s";
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        if (banner != null) {
            banner.startTurning(5000);
        }
    }

    @AfterViews
    protected void initMaopaoListFragment() {
        initMaopaoListBaseFragmen(mType);
    }

    @Override
    protected void setActionTitle() {
        if (userId != 0) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                String actionBarTitle = actionBar.getTitle().toString();
                if (TextUtils.isEmpty(actionBarTitle)) {
                    if (userId == GlobalData.sUserObject.id) {
                        actionBar.setTitle("我的冒泡");
                    } else {
                        actionBar.setTitle("TA的冒泡");
                    }
                }
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_BANNER)) {
            if (code == 0) {
                ArrayList<BannerObject> banners = new ArrayList<>();
                JSONArray jsonArray = respanse.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    banners.add(new BannerObject(jsonArray.getJSONObject(i)));
                }
                AccountInfo.saveMaopaoBanners(getActivity(), banners);

                mBannerDatas.clear();
                mBannerDatas.addAll(banners);
//                initBannerData(mBannerDatas);
                updateBannerData();
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    @Override
    protected boolean getShowAnimator() {
        return true;
    }

    @Override
    protected void initMaopaoType() {
        if (mType == Type.friends) {
            id = Global.UPDATE_ALL_INT;
            lastTime = 0;
        }

        if (mType == Type.hot) {
            mNoMore = true;
        }

        if (mType != Type.user) {
            floatButton.attachToRecyclerView(listView.mRecyclerView);
        } else {
            floatButton.hide(false);
        }

        if (mType == Type.time) {
            mBannerDatas.addAll(AccountInfo.getMaopaoBanners(getActivity()));
            initBannerData();
            updateBannerData();
            getNetwork(BannerObject.getHttpBanners(), TAG_BANNER);
        }

        getNetwork(createUrl(), getMaopaoUrlFormat());
    }

//    private void addDoubleClickActionbar() {
//        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
//        View v = actionBar.getCustomView();
//        // 有些界面没有下拉刷新
//        if (v != null && v.getParent() != null) {
//            ((View) v.getParent()).setOnClickListener(new View.OnClickListener() {
//
//                final long DOUBLE_CLICK_TIME = 300;
//                long mLastTime = 0;
//
//                @Override
//                public void onClick(View v) {
//                    long lastTime = mLastTime;
//                    long nowTime = Calendar.getInstance().getTimeInMillis();
//                    mLastTime = nowTime;
//
//                    if (nowTime - lastTime < DOUBLE_CLICK_TIME) {
//                        if (listView.mSwipeRefreshLayout != null &&
//                                !listView.mSwipeRefreshLayout.isRefreshing()) {
//                            listView.setRefreshing(true);
//                            onRefresh();
//                        }
//                    }
//                }
//            });
//        }
//
//    }

    @Override
    public void onPause() {
        if (banner != null) {
            banner.stopTurning();
        }
        super.onPause();
    }

    protected void setRedPointStyle(int buttonId, RedPointTip.Type type) {
        View item = getView().findViewById(buttonId);
        View redPoint = item.findViewById(R.id.badge);
        boolean show = RedPointTip.show(getActivity(), type);
        redPoint.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateBannerData() {
        if (mBannerDatas.isEmpty()) {
            mBannerDatas.add(new BannerObject());
        }
        banner.setPages(() -> new LocalImageHolder(), mBannerDatas);

        int bannerStartPos = 0;
        bannerIndicator.setCount(mBannerDatas.size(), bannerStartPos);
        bannerName.setVisibility(View.VISIBLE);
        if (mBannerDatas.size() > 0) {
            bannerName.setText(mBannerDatas.get(bannerStartPos).getName());
            bannerTitle.setText(mBannerDatas.get(bannerStartPos).getTitle());
        }
    }

    private View bannerLayout;

    @Override
    public void showLoading(boolean show) {
        super.showLoading(show);
        if (!show && bannerLayout != null) {
            bannerLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initBannerData() {
        bannerLayout = mInflater.inflate(R.layout.maopao_banner_view_pager, listView, false);
        banner = (ConvenientBanner) bannerLayout.findViewById(R.id.bannerViewPager);
        bannerLayout.setVisibility(View.INVISIBLE);

        ViewGroup.LayoutParams layoutParams = banner.getLayoutParams();
        layoutParams.height = GlobalData.sWidthPix * 130 / 360;
        banner.setLayoutParams(layoutParams);

        bannerIndicator = (IndicatorView) bannerLayout.findViewById(R.id.indicatorView);
        bannerName = (TextView) bannerLayout.findViewById(R.id.bannerName);
        bannerTitle = (TextView) bannerLayout.findViewById(R.id.bannerTitle);

        listView.setNormalHeader(bannerLayout);

        ((ViewPager) banner.findViewById(R.id.cbLoopViewPager)).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                BannerObject bannerData = mBannerDatas.get(position);

                String name = bannerData.getName();
                if (name.isEmpty()) {
                    bannerName.setVisibility(View.INVISIBLE);
                } else {
                    bannerName.setVisibility(View.VISIBLE);
                    bannerName.setText(name);
                }
                bannerTitle.setText(bannerData.getTitle());
                bannerIndicator.setSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                listView.enableDefaultSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
    }

    @Click
    protected final void floatButton() {
        Intent intent = new Intent(getActivity(), MaopaoAddActivity_.class);
        startActivityForResult(intent, RESULT_EDIT_MAOPAO);
    }

    @Override
    protected String createUrl() {
        if (mType == Type.friends) {
            return String.format(friendUrl, id);
        } else if (mType == Type.my) {
            UserObject my = AccountInfo.loadAccount(getActivity());
            return String.format(myUrl, my.id, id);
        } else if (mType == Type.user) {
            return String.format(myUrl, userId, id);
        } else {
            String url = String.format(getMaopaoUrlFormat(), id, mType) + "&last_time=";
            if (lastTime > 0) {
                url += lastTime;
            }
            return url;
        }
    }

    @Override
    protected void initData() {
    }

    protected void hideSoftkeyboard() {
        super.hideSoftkeyboard();

        floatButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void popComment(View v) {
        super.popComment(v);
        floatButton.hide(false);
        floatButton.setVisibility(View.INVISIBLE);

    }

    // user 某个用户，friend 好友圈，time 冒泡广场
    public enum Type {
        user, friends, hot, my, time
    }

    private class LocalImageHolder implements CBPageAdapter.Holder<BannerObject> {
        GifImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new GifImageView(getActivity());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(v -> {
                int position = (int) v.getTag(R.id.image);
                BannerObject bannerObject = mBannerDatas.get(position);
                URLSpanNoUnderline.openActivityByUri(getActivity(), bannerObject.getLink(), false, true, true);
            });
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, BannerObject data) {
            imageView.setTag(R.id.image, position);
            getImageLoad().loadImage(imageView, mBannerDatas.get(position).getImage(), ImageLoadTool.bannerOptions, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    if (imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
                        if (imageUri.endsWith(".gif")) {
                            LoadGifUtil gifUtil = new LoadGifUtil(getActivity());
                            gifUtil.getGifImage(imageView, imageUri);
                        }
                    }
                }
            });
        }
    }

}
