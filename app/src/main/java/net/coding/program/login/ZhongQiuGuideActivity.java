package net.coding.program.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.guide.IndicatorView;

/*
 * 用来显示特别的版本或活动, 比如中秋节, 比如 4.0 大更新
 */
public class ZhongQiuGuideActivity extends BaseActivity {

    ViewPager mViewPager;
    IndicatorView mIndicatorView;
    View entranceButton;
    View jumpButton;
    int[] mBackgroundResId = new int[]{
            R.drawable.guide_zhongqiu_0,
//            R.drawable.guide_zhongqiu_1,
    };
    FragmentPagerAdapter pagerAdapter;

//    public static void showHolidayGuide(Activity activity) {
//        if (AccountInfo.needDisplayGuide(activity)) {
//            AccountInfo.markGuideReaded(activity);
//            Intent intent1 = new Intent(activity, ZhongQiuGuideActivity.class);
//            activity.startActivity(intent1);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhong_qiu_guide);

        mIndicatorView = (IndicatorView) findViewById(R.id.indicatorView);
        jumpButton = findViewById(R.id.jump);
        entranceButton = findViewById(R.id.entrance);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new HolidayPager(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        jumpButton.setOnClickListener(v -> finish());
        entranceButton.setOnClickListener(v -> {
            AccountInfo.markGuideReaded(ZhongQiuGuideActivity.this);

            Intent intent;
            String mGlobalKey = AccountInfo.loadAccount(this).global_key;
            if (mGlobalKey.isEmpty()) {
                intent = new Intent(this, LoginActivity_.class);
            } else {
                intent = new Intent(this, CodingCompat.instance().getMainActivity());
            }

            startActivity(intent);
            overridePendingTransition(R.anim.entrance_fade_in, R.anim.entrance_fade_out);
            finish();
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int indicatorWidth = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("", String.format("scr1 %s %f %s", position, positionOffset, positionOffsetPixels));

                if (position == (mBackgroundResId.length - 1)) {
                    entranceButton.setVisibility(View.VISIBLE);
                } else {
                    entranceButton.setVisibility(View.INVISIBLE);
                }

                if (positionOffset > 0.5) {
                    mIndicatorView.setSelect(position + 1);
                } else {
                    mIndicatorView.setSelect(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("", String.format("scr2 %s", position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("", String.format("scr3 %s", state)); //, positionOffset, positionOffset));

            }

            private Fragment getFragment(int postion) {
                String name1 = makeFragmentName(mViewPager.getId(), postion);
                return getSupportFragmentManager().findFragmentByTag(name1);
            }

            private String makeFragmentName(int viewId, long id) {
                return "android:switcher:" + viewId + ":" + id;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 屏蔽后退键
    }

    public static class GuideFragment extends Fragment {

        public static final String ARGUMENT_IMAGE = "ARGUMENT_IMAGE";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.holiday_fragment, container, false);
            ImageView imageView = (ImageView) v.findViewById(R.id.image);
            int resId = getArguments().getInt(ARGUMENT_IMAGE, 0);
            if (resId != 0) {
                imageView.setImageResource(resId);
            }
            return v;
        }
    }

    class HolidayPager extends FragmentPagerAdapter {

        public HolidayPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            GuideFragment fragment = new GuideFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(GuideFragment.ARGUMENT_IMAGE, mBackgroundResId[position]);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mBackgroundResId.length;
        }
    }
}
