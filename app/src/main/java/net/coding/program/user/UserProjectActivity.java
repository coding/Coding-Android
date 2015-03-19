package net.coding.program.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.model.UserObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

@EActivity(R.layout.activity_user_project)
@OptionsMenu(R.menu.menu_user_project)
public class UserProjectActivity extends BaseActivity {

    @Extra
    UserObject mUserObject;

    @StringArrayRes
    String[] user_program_title;

    @ViewById
    WechatTab tabs;

    @ViewById(R.id.pagerFragmentProgram)
    ViewPager pager;

    @AfterViews
    protected final void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
    }

    @OptionsItem(android.R.id.home)
    protected final void back() {
        finish();
    }

    class MyPagerAdapter extends SaveFragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return user_program_title[position];
        }

        @Override
        public int getCount() {
            return user_program_title.length;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = UserProjectListFragment_.builder()
                    .mUserObject(mUserObject)
                    .mType(position)
                    .build();

            saveFragment(fragment);
            return fragment;
        }
    }

}
