package net.coding.program.subject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;

import net.coding.program.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by david on 15-7-20.
 * 话题墙
 */
@EActivity(R.layout.activity_subject_wall)
public class SubjectWallActivity extends BackActivity {

    @ViewById
    WechatTab tabs;

    @ViewById(R.id.pagerFragmentProgram)
    ViewPager pager;

    @AfterViews
    protected final void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("话题墙");
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
    }

    class MyPagerAdapter extends SaveFragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "热门话题";
            return "我的话题";
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
