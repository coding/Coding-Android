package net.coding.program.user;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyco.roundview.RoundTextView;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.DensityUtil;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.maopao.MaopaoListFragment_;
import net.coding.program.model.UserObject;
import net.coding.program.subject.SubjectListFragment;
import net.coding.program.subject.SubjectListFragment_;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_my_detail)
public class MyDetailActivity extends BackActivity {

    public final int RESULT_EDIT = 0;

    @ViewById
    WechatTab tabs;

    @ViewById
    ViewPager viewPager;

    @ViewById
    ImageView icon;
    @ViewById
    TextView name;

    @ViewById
    TextView location;

    @ViewById
    TextView introduction;

    @ViewById
    View icon_sharow;
    //@ViewById
    //CheckBox followCheckbox;
    //@ViewById
    //ImageView userBackground;

    @ViewById
    TextView fans, follows;

    int sexs[] = new int[]{
            R.drawable.ic_sex_boy,
            R.drawable.ic_sex_girl,
            android.R.color.transparent
    };

    @OptionsItem
    void action_edit() {
        UserDetailEditActivity_
                .intent(this)
                .startForResult(RESULT_EDIT);
    }

    View.OnClickListener onClickFans = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UsersListActivity.UserParams userParams = new UsersListActivity.UserParams(MyApp.sUserObject,
                    UsersListActivity.Friend.Fans);

            UsersListActivity_
                    .intent(MyDetailActivity.this)
                    .mUserParam(userParams)
                    .type(UsersListActivity.Friend.Fans)
                    .start();
        }
    };
    View.OnClickListener onClickFollow = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UsersListActivity.UserParams userParams = new UsersListActivity.UserParams(MyApp.sUserObject,
                    UsersListActivity.Friend.Follow);

            UsersListActivity_
                    .intent(MyDetailActivity.this)
                    .mUserParam(userParams)
                    .type(UsersListActivity.Friend.Follow)
                    .start();
        }
    };

    @AfterViews
    void initMyDetailActivity() {
        MyDetailPagerAdapter adapter = new MyDetailPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
//        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
//                .getDisplayMetrics());
//        viewPager.setPageMargin(pageMargin);
        tabs.setViewPager(viewPager);
        ViewCompat.setElevation(tabs, 0);
        ViewCompat.setElevation(findViewById(R.id.appbarLayout), 0);

        bindUI();

//        tabs.post(new Runnable() {
//            @Override
//            public void run() {
//                int viewPagerHeight = MyApp.sHeightPix - Global.dpToPx(258 + 15);
//                ViewGroup.LayoutParams lp = viewPager.getLayoutParams();
//                lp.height = viewPagerHeight;
//                viewPager.setLayoutParams(lp);
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_detail_me, menu);
        return true;
    }

    private void bindUI() {
        UserObject mUserObject = MyApp.sUserObject;
        iconfromNetwork(icon, mUserObject.avatar, new UserDetailActivity.AnimateFirstDisplayListener());
        icon.setTag(new MaopaoListFragment.ClickImageParam(mUserObject.avatar));
        icon.setOnClickListener(new ClickSmallImage(this));

        name.setCompoundDrawablesWithIntrinsicBounds(0, 0, sexs[mUserObject.sex], 0);
        name.setText(mUserObject.name);

        initTextData(name, mUserObject.name);
        initTextData(location, mUserObject.location);
        initTextData(introduction, mUserObject.slogan);

        if (TextUtils.isEmpty(mUserObject.tags_str)) {
            findViewById(R.id.hsl_main).setVisibility(View.GONE);
        } else {
            LinearLayout llTags = (LinearLayout) findViewById(R.id.ll_tags);

            String[] split = mUserObject.tags_str.split(",");
            for (String tag : split) {
                if (TextUtils.isEmpty(tag)) {
                    continue;
                }
                RoundTextView roundTextView = (RoundTextView) getLayoutInflater().inflate(R.layout.view_tag, null);
                roundTextView.setText(tag);
                llTags.addView(roundTextView);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, DensityUtil.dip2px(MyDetailActivity.this, 5), 0);
                roundTextView.setLayoutParams(lp);
            }
        }


        //findViewById(R.id.sendMessageLayout).setVisibility(View.GONE);

        fans.setText(UserDetailActivity.createSpan(this, String.format("粉丝   %d", mUserObject.fans_count)));
        fans.setOnClickListener(onClickFans);

        follows.setText(UserDetailActivity.createSpan(this, String.format("关注   %d", mUserObject.follows_count)));
        follows.setOnClickListener(onClickFollow);
    }

    private void initTextData(TextView textView, String data) {
        if (TextUtils.isEmpty(data)) {
            isShow(false, textView);
            return;
        }
        isShow(true, textView);
        textView.setText(data);
    }

    private void isShow(boolean isShow, View view) {
        if (view == null) {
            return;
        }
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
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
