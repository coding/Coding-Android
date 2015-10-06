package net.coding.program.subject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.model.Subject;
import net.coding.program.subject.loop.AutoScrollLoopViewPager;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-20.
 * 话题墙
 */
@EActivity(R.layout.activity_subject_wall)
public class SubjectWallActivity extends BaseActivity {

    @ViewById
    AutoScrollLoopViewPager loopViewPager;
    @ViewById
    WechatTab tabs;
    @ViewById
    ViewPager pager;
    @ViewById(R.id.topic_my_container)
    LinearLayout mTopicMyContainer;
    @ViewById(R.id.topic_hot_container)
    FrameLayout mTopicHotContainer;
    private String mTweetAdUrl = Global.HOST_API + "/tweet_topic/marketing_ad";
    private String mTweetAdTag = "marketing_ad";
    private List<Subject.SubjectDescObject> mHotTweetDescObjects = new ArrayList<>();
    private MySpinnerAdapter mSpinnerAdapter;
    private PagerAdapter mAdPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (mHotTweetDescObjects == null) {
                return 0;
            }
            return mHotTweetDescObjects.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            ImageView imageView = new ImageView(SubjectWallActivity.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setTag(position);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubjectDetailActivity_.intent(SubjectWallActivity.this).subjectDescObject(mHotTweetDescObjects.get(position)).start();
                }
            });
            getImageLoad().loadImage(imageView, mHotTweetDescObjects.get(position).image_url, ImageLoadTool.bannerOptions);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }
    };

    @AfterViews
    protected final void initSubjectWallActivity() {
        initTitleBar();
        showMyTopic();
        showHotTopic();
        getTweetTopicAdFromServer();
    }

    @OptionsItem(android.R.id.home)
    protected final void annotaionClose() {
        finish();
    }

    private void initTitleBar() {
        mSpinnerAdapter = new MySpinnerAdapter(getLayoutInflater());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);
        supportActionBar.setHomeButtonEnabled(true);
        supportActionBar.setTitle("");
        supportActionBar.setDisplayShowCustomEnabled(true);

        supportActionBar.setCustomView(R.layout.actionbar_custom_spinner);
        Spinner spinner = (Spinner) supportActionBar.getCustomView().findViewById(R.id.spinner);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpinnerAdapter.setCheckPos(position);
                changePageShow(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showHotTopic() {
        Fragment fragment = SubjectListFragment_.builder()
                .userKey(MyApp.sUserObject.global_key)
                .mType(SubjectListFragment.Type.hot)
                .build();
        getSupportFragmentManager().beginTransaction().replace(R.id.topic_hot_container, fragment).commit();
    }

    private void showMyTopic() {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
    }

    private void initLooperViewPager() {
        loopViewPager.setAdapter(mAdPagerAdapter);
        loopViewPager.setSmoothScrollDurationRatio(3);
        loopViewPager.startAutoScroll();
    }

    private void getTweetTopicAdFromServer() {
        getNetwork(mTweetAdUrl, mTweetAdTag);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (mTweetAdTag.equals(tag)) {
            JSONArray dataArr = respanse.optJSONArray("data");
            if (dataArr != null) {
                Subject.SubjectDescObject tweetDescObject = null;
                mHotTweetDescObjects.clear();
                for (int i = 0; i < dataArr.length(); i++) {
                    tweetDescObject = new Subject.SubjectDescObject(dataArr.optJSONObject(i));
                    mHotTweetDescObjects.add(tweetDescObject);
                }
                initLooperViewPager();
                mAdPagerAdapter.notifyDataSetChanged();
            }
            return;
        }
    }

    private void changePageShow(int pos) {
        if (pos == 1) {
            mTopicHotContainer.setVisibility(View.GONE);
            mTopicMyContainer.setVisibility(View.VISIBLE);
        } else {
            mTopicHotContainer.setVisibility(View.VISIBLE);
            mTopicMyContainer.setVisibility(View.GONE);
        }
    }

    class MySpinnerAdapter extends BaseAdapter {

        int checkPos = 0;
        private LayoutInflater inflater;
        private String[] titles = new String[]{"热门话题", "我的话题"};

        public MySpinnerAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        public void setCheckPos(int pos) {
            checkPos = pos;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_head, parent, false);
            }

            ((TextView) convertView).setText(titles[position]);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(titles[position]);

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setVisibility(View.GONE);

            if (checkPos == position) {
                convertView.setBackgroundColor(getResources().getColor(R.color.divide));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
            return convertView;
        }
    }

    class MyPagerAdapter extends SaveFragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "我关注的";
            return "我参与的";
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            final SubjectListFragment.Type types[] = new SubjectListFragment.Type[]{
                    SubjectListFragment.Type.follow,
                    SubjectListFragment.Type.join
            };

            Fragment fragment = SubjectListFragment_.builder()
                    .userKey(MyApp.sUserObject.global_key)
                    .mType(types[position])
                    .build();

            saveFragment(fragment);
            return fragment;
        }
    }
}
