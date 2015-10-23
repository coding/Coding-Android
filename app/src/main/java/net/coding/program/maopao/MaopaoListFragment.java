package net.coding.program.maopao;


import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.CBPageAdapter;
import com.bigkoo.convenientbanner.CBViewHolderCreator;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.melnykov.fab.FloatingActionButton;
import com.twotoasters.jazzylistview.effects.SlideInEffect;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.guide.IndicatorView;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.BannerObject;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

@EFragment(R.layout.fragment_maopao_list)
@OptionsMenu(R.menu.menu_fragment_maopao)
public class MaopaoListFragment extends MaopaoListBaseFragment {

    final String friendUrl = Global.HOST_API + "/activities/user_tweet?last_id=%s";
    final String myUrl = Global.HOST_API + "/tweet/user_public?user_id=%s&last_id=%s";

    @Override
    protected String getMaopaoUrlFormat() {
        return Global.HOST_API + "/tweet/public_tweets?last_id=%s&sort=%s";
    }

    final String TAG_BANNER = "TAG_BANNER";

    @FragmentArg
    Type mType;
    @FragmentArg
    int userId;

    @ViewById
    FloatingActionButton floatButton;

    ConvenientBanner banner;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        if (banner != null) {
            banner.startTurning(5000);
        }
    }

    @OptionsItem
    void action_search() {
        MaopaoSearchActivity_.intent(this).start();
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

    }

    private IndicatorView bannerIndicator;
    private TextView bannerName;
    private TextView bannerTitle;


    @AfterViews
    protected void initMaopaoListFragment() {
        initMaopaoListBaseFragmen();

    }

    @Override
    protected void setActionTitle() {
        if (userId != 0) {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            if (userId == MyApp.sUserObject.id) {
                actionBar.setTitle("我的冒泡");
            } else {
                actionBar.setTitle("TA的冒泡");
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
    protected void initMaopaoType() {
        listView.setTransitionEffect(new UpSlideInEffect());

        if (mType == Type.friends) {
            id = UPDATE_ALL_INT;
        }

        if (mType == Type.hot) {
            mNoMore = true;
        }

        if (mType != Type.user) {
            floatButton.attachToListView(listView);
        } else {
            floatButton.hide(false);
        }

        if (mType == Type.time) {
            mBannerDatas.addAll(AccountInfo.getMaopaoBanners(getActivity()));
            initBannerData();
            updateBannerData();
            getNetwork(BannerObject.getHttpBanners(), TAG_BANNER);
        }

        addDoubleClickActionbar();


        getNetwork(createUrl(), getMaopaoUrlFormat());
    }

    private void addDoubleClickActionbar() {
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        View v = actionBar.getCustomView();
        // 有些界面没有下拉刷新
        if (v != null && v.getParent() != null) {
            ((View) v.getParent()).setOnClickListener(new View.OnClickListener() {

                final long DOUBLE_CLICK_TIME = 300;
                long mLastTime = 0;

                @Override
                public void onClick(View v) {
                    long lastTime = mLastTime;
                    long nowTime = Calendar.getInstance().getTimeInMillis();
                    mLastTime = nowTime;

                    if (nowTime - lastTime < DOUBLE_CLICK_TIME) {
                        if (!isRefreshing()) {
                            setRefreshing(true);
                            onRefresh();
                        }
                    }
                }
            });
        }

    }

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
        banner.setPages(new CBViewHolderCreator() {
            @Override
            public Object createHolder() {
                return new LocalImageHolder();
            }
        }, mBannerDatas);

        int bannerStartPos = 0;
        bannerIndicator.setCount(mBannerDatas.size(), bannerStartPos);
        bannerName.setVisibility(View.VISIBLE);
        if (mBannerDatas.size() > 0) {
            bannerName.setText(mBannerDatas.get(bannerStartPos).getName());
            bannerTitle.setText(mBannerDatas.get(bannerStartPos).getTitle());
        }
    }

    private void initBannerData() {
        View bannerLayout = mInflater.inflate(R.layout.maopao_banner_view_pager, null);
        banner = (ConvenientBanner) bannerLayout.findViewById(R.id.bannerViewPager);

        ViewGroup.LayoutParams layoutParams = banner.getLayoutParams();
        layoutParams.height = (int) ((MyApp.sWidthPix - getResources().getDimensionPixelSize(R.dimen.padding_12) * 2) * 0.3);
        banner.setLayoutParams(layoutParams);

        bannerIndicator = (IndicatorView) bannerLayout.findViewById(R.id.indicatorView);
        bannerName = (TextView) bannerLayout.findViewById(R.id.bannerName);
        bannerTitle = (TextView) bannerLayout.findViewById(R.id.bannerTitle);

        listView.addHeaderView(bannerLayout);

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
                enableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
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
            return String.format(getMaopaoUrlFormat(), id, mType);
        }
    }


    @Override
    protected void initData() {
    }

    // 如果不设延时的话会闪一下
    @UiThread(delay = 1500)
    void showFloatButton() {
        floatButton.hide(false);
        floatButton.setVisibility(View.VISIBLE);
    }


    protected void hideSoftkeyboard() {
        super.hideSoftkeyboard();

        floatButton.hide(false);
        showFloatButton();
    }

    @Override
    protected void popComment(View v) {
        super.popComment(v);
        floatButton.setVisibility(View.INVISIBLE);
    }

    // user 某个用户，friend 好友圈，time 冒泡广场
    public enum Type {
        user, friends, hot, my, time
    }

    public static class ClickImageParam {
        public ArrayList<String> urls;
        public int pos;
        public boolean needEdit;

        public ClickImageParam(ArrayList<String> urlsParam, int posParam, boolean needEditParam) {
            urls = urlsParam;
            pos = posParam;
            needEdit = needEditParam;
        }

//        public ClickImageParam(ArrayList<PhotoPickActivity.ImageInfo> urlsParam, int posParam) {
//            urls = new ArrayList<>();
//            for (PhotoPickActivity.ImageInfo item : urlsParam) {
//                urls.add(item.path);
//            }
//
//            pos = posParam;
//            needEdit = true;
//        }

        public ClickImageParam(String url) {
            urls = new ArrayList<>();
            urls.add(url);
            pos = 0;
            needEdit = false;
        }
    }

    ArrayList<BannerObject> mBannerDatas = new ArrayList<>();

    // listview 向上滑才有动画
    class UpSlideInEffect extends SlideInEffect {
        @Override
        public void initView(View item, int position, int scrollDirection) {
            if (scrollDirection > 0) {
                super.initView(item, position, scrollDirection);
            }
        }

        @Override
        public void setupAnimation(View item, int position, int scrollDirection, ViewPropertyAnimator animator) {
            if (scrollDirection > 0) {
                super.setupAnimation(item, position, scrollDirection, animator);
            }
        }
    }

    class LocalImageHolder implements CBPageAdapter.Holder<BannerObject> {
        ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag(R.id.image);
                    BannerObject bannerObject = mBannerDatas.get(position);
                    URLSpanNoUnderline.openActivityByUri(getActivity(), bannerObject.getLink(), false);
                }
            });
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, BannerObject data) {
            imageView.setTag(R.id.image, position);
            getImageLoad().loadImage(imageView, mBannerDatas.get(position).getImage(), ImageLoadTool.bannerOptions);
        }
    }
}
