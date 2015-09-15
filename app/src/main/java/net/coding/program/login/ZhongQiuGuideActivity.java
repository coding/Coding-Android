package net.coding.program.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.coding.program.BaseActivity;
import net.coding.program.R;

public class ZhongQiuGuideActivity extends BaseActivity {

    public static void showHolidayGuide(Activity activity) {
//        Intent intent1 = new Intent(activity, ZhongQiuGuideActivity.class);
//        activity.startActivity(intent1);
    }

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhong_qiu_guide);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new HolidayPager(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("", String.format("scr1 %d %f %d", position, positionOffset, positionOffsetPixels));
                if (position == 1 && positionOffset > 0) {
                    Fragment fragment1 = getFragment(1);
                    Fragment fragment2 = getFragment(2);
                    fragment1.getView().setAlpha(1 - positionOffset);
                    fragment2.getView().setAlpha(1 - positionOffset);
                } else if (position == 2) {
                    finish();
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("", String.format("scr2 %d", position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("", String.format("scr3 %d", state)); //, positionOffset, positionOffset));

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zhong_qiu_guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    int[] mBackgroundResId = new int[]{
            R.drawable.guide_zhongqiu_1,
            R.drawable.guide_zhongqiu_2,
            0
    };


    FragmentPagerAdapter pagerAdapter;

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


}
