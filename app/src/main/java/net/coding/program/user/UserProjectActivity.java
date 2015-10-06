package net.coding.program.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.model.UserObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_user_project)
@OptionsMenu(R.menu.menu_user_project)
public class UserProjectActivity extends BackActivity {

    @Extra
    UserObject mUserObject;

    String[] fragmentTitles;

    @ViewById
    WechatTab tabs;

    @ViewById(R.id.pagerFragmentProgram)
    ViewPager pager;

    @AfterViews
    protected final void init() {
        ActionBar actionBar = getSupportActionBar();
        if (mUserObject.isMe()) {
            fragmentTitles = getResources().getStringArray(R.array.user_me_program_title);
            actionBar.setTitle("我的项目");
        } else {
            fragmentTitles = getResources().getStringArray(R.array.user_program_title);
            actionBar.setTitle("TA的项目");
        }

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
            return fragmentTitles[position];
        }

        @Override
        public int getCount() {
            return fragmentTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            final UserProjectListFragment.Type types[] = new UserProjectListFragment.Type[]{
                    UserProjectListFragment.Type.joined,
                    UserProjectListFragment.Type.stared
            };

            Fragment fragment = UserProjectListFragment_.builder()
                    .mUserObject(mUserObject)
                    .mType(types[position])
                    .build();

            saveFragment(fragment);
            return fragment;
        }
    }

}
