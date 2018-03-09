package net.coding.program.user;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;

import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.maopao.MaopaoListFragment_;
import net.coding.program.subject.SubjectListFragment;
import net.coding.program.subject.SubjectListFragment_;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_my_detail)
public class MyDetailActivity extends UserDetailCommonActivity {

    public final int RESULT_EDIT = 0;
    private final String TAG_HOST_USER_INFO = "TAG_HOST_USER_INFO";

    @ViewById
    WechatTab tabs;

    @ViewById
    ViewPager viewPager;

    @AfterViews
    void initMyDetailActivity() {
        bindUI(GlobalData.sUserObject);
        tv_follow_state.setText("编辑资料");
        rl_follow_state.setOnClickListener(v -> {
            UserDetailEditActivity_
                    .intent(this)
                    .startForResult(RESULT_EDIT);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        final String HOST_USER_INFO = Global.HOST_API + "/current_user";
        getNetwork(HOST_USER_INFO, TAG_HOST_USER_INFO);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_USER_INFO)) {
            if (code == 0) {
                mUserObject = new UserObject(respanse.getJSONObject("data"));

                AccountInfo.saveAccount(this, mUserObject);
                GlobalData.sUserObject = mUserObject;
                AccountInfo.saveReloginInfo(this, mUserObject);
                bindUI(mUserObject);
            } else {
                showButtomToast("获取用户信息错误");
            }
        }
        openActivenessResult(code, respanse, tag);
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
        return GlobalCommon.dpToPx(48);
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
                        .userId(GlobalData.sUserObject.id)
                        .build();
            } else {
                return SubjectListFragment_.builder()
                        .userKey(GlobalData.sUserObject.global_key)
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
