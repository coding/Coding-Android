package net.coding.program.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.maopao.MaopaoRequestTag;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.util.DensityUtil;
import net.coding.program.common.widget.ListItem1;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.project.detail.file.LocalProjectFileActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_user_detail)
public class UserDetailActivity extends UserDetailCommonActivity {

    public final String HOST_FOLLOW = getHostFollow();

    public final String HOST_UNFOLLOW = getHostUnfollow();

    public final int RESULT_EDIT = 0;

    final String HOST_USER_INFO = Global.HOST_API + "/user/key/";
    @Extra
    String globalKey;
    @ViewById
    ImageView icon;
    @StringArrayRes
    String[] user_detail_activity_list_first;
    @StringArrayRes
    String[] user_detail_list_first;
    String[] user_detail_list_second;
    boolean isMe = false;
    boolean mNeedUpdate = false;

    @NonNull
    public static String getHostFollow() {
        return Global.HOST_API + "/user/follow?";
    }

    @NonNull
    public static String getHostUnfollow() {
        return Global.HOST_API + "/user/unfollow?";
    }

    public static SpannableString createSpan(Context context, String s) {
        SpannableString itemContent = new SpannableString(s);
        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.font_count));

        itemContent.setSpan(colorSpan, 2, itemContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        itemContent.setSpan(new AbsoluteSizeSpan(15, true), 2, itemContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        itemContent.setSpan(new StyleSpan(Typeface.BOLD), 2, itemContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return itemContent;
    }

    @AfterViews
    protected final void initUserDetailActivity() {
        //initListFirst();

        if (globalKey != null) {
            if (globalKey.equals(GlobalData.sUserObject.global_key)) {
                isMe = true;

                CodingCompat.instance().launchMyDetailActivity(this);
                finish();
                return;
            }
            bindViewType();
            getNetwork(HOST_USER_INFO + globalKey, HOST_USER_INFO);
        } else {
            try {
                String name = getIntent().getData().getQueryParameter("name");
                if (name.equals(GlobalData.sUserObject.name)) {
                    isMe = true;

                    CodingCompat.instance().launchMyDetailActivity(this);
                    finish();
                    return;
                }
                bindViewType();
                getNetwork(HOST_USER_INFO + name, HOST_USER_INFO);
            } catch (Exception e) {
                Global.errorLog(e);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mUserObject = (UserObject) savedInstanceState.getSerializable("mUserObject");
            isMe = savedInstanceState.getBoolean("isMe", false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUserObject != null) {
            outState.putSerializable("mUserObject", mUserObject);
            outState.putBoolean("isMe", isMe);
        }
    }

    // 自己的和他人的显示项目有所不同
    private void bindViewType() {
        if (isMe) {
            setActionBarTitle("个人主页");
            ((ListItem1) findViewById(R.id.clickProject)).setText("我的项目");
            ((ListItem1) findViewById(R.id.clickMaopao)).setText("我的冒泡");
            ((ListItem1) findViewById(R.id.clickTopic)).setText("我的话题");
        } else {
            findViewById(R.id.divideLocal).setVisibility(View.GONE);
            findViewById(R.id.clickLocal).setVisibility(View.GONE);
        }
        findViewById(R.id.pointDivide).setVisibility(isMe ? View.VISIBLE : View.GONE);
        findViewById(R.id.clickPointRecord).setVisibility(isMe ? View.VISIBLE : View.GONE);
    }

    @OptionsItem
    void action_edit() {
        UserDetailEditActivity_
                .intent(this)
                .startForResult(RESULT_EDIT);
    }

    @OptionsItem
    public final void action_more_detail() {
        UserDetailMoreActivity_.intent(this)
                .mUserObject(mUserObject)
                .start();
    }

    @OptionsItem
    public final void action_copy_link() {
        String link = Global.HOST + mUserObject.path;
        Global.copy(this, link);
        showButtomToast("已复制链接 " + link);
    }

    @OnActivityResult(RESULT_EDIT)
    void onResult() {
        getNetwork(HOST_USER_INFO + mUserObject.global_key, HOST_USER_INFO);
    }

    void displayUserinfo() {

        bindUI(mUserObject);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rl_follow_state.getLayoutParams();
        lp.setMargins(0, 0, DensityUtil.dip2px(UserDetailActivity.this, 5), 0);
        rl_follow_state.setLayoutParams(lp);

        findViewById(R.id.first).setOnClickListener(v -> {
            UserDetailMoreActivity_.intent(this)
                    .mUserObject(mUserObject)
                    .start();
        });
        findViewById(R.id.first).setVisibility(View.VISIBLE);
        rl_message.setVisibility(View.VISIBLE);
        rl_message.setOnClickListener(v -> {
            if (!dataIsLoaded()) {
                return;
            }

            Intent intent = new Intent(this, MessageListActivity_.class);
            intent.putExtra("mUserObject", mUserObject);
            startActivity(intent);
        });

        // 自己的页面不显示 关注
        if (!isMe) {
            if (mUserObject.follow && mUserObject.followed) {
                tv_follow_state.setText("互相关注");
                tv_follow_state.setTextColor(getResources().getColor(R.color.font_1));
                tv_follow_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow_state3, 0, 0, 0);
            } else if (!mUserObject.follow && mUserObject.followed) {
                tv_follow_state.setText("已关注");
                tv_follow_state.setTextColor(getResources().getColor(R.color.font_1));
                tv_follow_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow_state2, 0, 0, 0);
            } else {
                tv_follow_state.setText("关注");
                tv_follow_state.setTextColor(getResources().getColor(R.color.font_green));
                tv_follow_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow_state1, 0, 0, 0);
            }

            rl_follow_state.setOnClickListener(v -> {
                RequestParams params = new RequestParams();
                params.put("users", mUserObject.global_key);
                if (!mUserObject.followed) {
                    postNetwork(HOST_FOLLOW, params, MaopaoRequestTag.TAG_HOST_FOLLOW);
                } else {
                    postNetwork(HOST_UNFOLLOW, params, MaopaoRequestTag.TAG_HOST_UNFOLLOW);
                }
            });
        }
    }

    private void followState(String value, int txtColor, int bgColor, int logo) {
        tv_follow_state.setText(value);
        tv_follow_state.setTextColor(getResources().getColor(txtColor));
        tv_follow_state.setCompoundDrawablesWithIntrinsicBounds(logo, 0, 0, 0);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_USER_INFO)) {
            if (code == 0) {
                mUserObject = new UserObject(respanse.getJSONObject("data"));
                displayUserinfo();
            } else {
                showButtomToast("获取用户信息错误");
                onBackPressed();
            }
        } else if (tag.equals(MaopaoRequestTag.TAG_HOST_FOLLOW)) {
            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast(R.string.follow_success);
                mUserObject.followed = true;
            } else {
                showButtomToast(R.string.follow_fail);
            }
            displayUserinfo();
        } else if (tag.equals(MaopaoRequestTag.TAG_HOST_UNFOLLOW)) {
            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast(R.string.unfollow_success);
                mUserObject.followed = false;
            } else {
                showButtomToast(R.string.unfollow_fail);
            }
            displayUserinfo();
        }
        openActivenessResult(code, respanse, tag);
    }


    @Override
    public void onBackPressed() {
        if (mNeedUpdate) {
            Intent intent = new Intent();
            intent.putExtra("data", mUserObject);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    @Click
    public void clickProject() {
        if (!dataIsLoaded()) {
            return;
        }

        UserProjectActivity_.intent(this).mUserObject(mUserObject).start();
    }

    @Click
    public void clickLocal() {
        LocalProjectFileActivity_.intent(this).start();
    }

    @Click
    public void clickMaopao() {
        if (!dataIsLoaded()) {
            return;
        }

        Intent intent = new Intent(UserDetailActivity.this, UserMaopaoActivity.class);
        intent.putExtra(UserMaopaoActivity.PARAM_ID, mUserObject.id);
        startActivity(intent);
    }

    @Click
    public void clickPointRecord() {
        if (!dataIsLoaded()) {
            return;
        }

        UserPointActivity_.intent(this).start();
    }

    @Click
    public void clickTopic() {
        if (!dataIsLoaded()) {
            return;
        }
        UserTopicActivity_.intent(this).mUserObject(mUserObject).start();

    }

    private boolean dataIsLoaded() {
        return mUserObject != null;
    }

    public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                ((View) imageView.getParent()).setVisibility(View.VISIBLE);
                FadeInBitmapDisplayer.animate((View) imageView.getParent(), 300);
            }
        }
    }
}
