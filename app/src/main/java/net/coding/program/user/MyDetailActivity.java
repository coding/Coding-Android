package net.coding.program.user;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.Global;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.maopao.MaopaoListFragment_;
import net.coding.program.subject.SubjectListFragment;
import net.coding.program.subject.SubjectListFragment_;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_my_detail)
public class MyDetailActivity extends UserDetailCommonActivity {

    public final int RESULT_EDIT = 0;

    @ViewById
    WechatTab tabs;

    @ViewById
    ViewPager viewPager;

    @AfterViews
    void initMyDetailActivity() {
        bindUI(MyApp.sUserObject);
        tv_follow_state.setText("编辑资料");
        rl_follow_state.setOnClickListener(v -> {
            UserDetailEditActivity_
                    .intent(this)
                    .startForResult(RESULT_EDIT);
        });
    }

    @Override
    protected void setViewPageData() {
        super.setViewPageData();
        viewPager.setAdapter(new MyDetailPagerAdapter(this, getSupportFragmentManager()));
        tabs.setViewPager(viewPager);
        ViewCompat.setElevation(tabs, 0);
        ViewCompat.setElevation(findViewById(R.id.appbarLayout), 0);
    }

    public int getActionBarSize() {
        return Global.dpToPx(48);
    }

    private static class MyDetailPagerAdapter extends FragmentPagerAdapter {

        String[] titles = new String[]{
                "冒泡",
                "话题"
        };

        Context context;

        public MyDetailPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return MaopaoListFragment_.builder()
                        .mType(MaopaoListFragment.Type.user)
                        .userId(MyApp.sUserObject.id)
                        .build();
            } else {
                return SubjectListFragment_.builder()
                        .userKey(MyApp.sUserObject.global_key)
                        .mType(SubjectListFragment.Type.join)
                        .build();
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

}
