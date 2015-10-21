package net.coding.program.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.widget.ListItem1;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.UserObject;
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
public class UserDetailActivity extends BackActivity {

    public static final String HOST_FOLLOW = Global.HOST_API + "/user/follow?";
    public static final String HOST_UNFOLLOW = Global.HOST_API + "/user/unfollow?";
    public final int RESULT_EDIT = 0;
    final String HOST_USER_INFO = Global.HOST_API + "/user/key/";
    private final int[] items = new int[]{
            R.id.pos0,
            R.id.pos1,
            R.id.pos2
    };
    @Extra
    String globalKey;
    @ViewById
    ImageView icon;
    @ViewById
    TextView name;
    @ViewById
    View sendMessage;
    @ViewById
    View icon_sharow;
    @ViewById
    CheckBox followCheckbox;
    @ViewById
    ImageView userBackground;
    @ViewById
    ImageView sex;
    @StringArrayRes
    String[] user_detail_activity_list_first;
    @StringArrayRes
    String[] user_detail_list_first;
    String[] user_detail_list_second;
    boolean isMe = false;
    int sexs[] = new int[]{
            R.drawable.ic_sex_boy,
            R.drawable.ic_sex_girl,
            android.R.color.transparent
    };
    boolean mNeedUpdate = false;
    private UserObject mUserObject;
    View.OnClickListener onClickFans = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UsersListActivity.UserParams userParams = new UsersListActivity.UserParams(mUserObject,
                    UsersListActivity.Friend.Fans);

            UsersListActivity_
                    .intent(UserDetailActivity.this)
                    .mUserParam(userParams)
                    .type(UsersListActivity.Friend.Fans)
                    .start();
        }
    };
    View.OnClickListener onClickFollow = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UsersListActivity.UserParams userParams = new UsersListActivity.UserParams(mUserObject,
                    UsersListActivity.Friend.Follow);

            UsersListActivity_
                    .intent(UserDetailActivity.this)
                    .mUserParam(userParams)
                    .type(UsersListActivity.Friend.Follow)
                    .start();
        }
    };

    @AfterViews
    protected final void initUserDetailActivity() {
        initListFirst();

        if (globalKey != null) {
            if (globalKey.equals(MyApp.sUserObject.global_key)) {
                isMe = true;
            }
            bindViewType();


            getNetwork(HOST_USER_INFO + globalKey, HOST_USER_INFO);
        } else {
            try {
                String name = getIntent().getData().getQueryParameter("name");
                if (name.equals(MyApp.sUserObject.name)) {
                    isMe = true;
                }
                bindViewType();

                getNetwork(HOST_USER_INFO + name, HOST_USER_INFO);
            } catch (Exception e) {
                Global.errorLog(e);
                finish();
                return;
            }
        }

        userBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams lp = userBackground.getLayoutParams();
                if (lp.width > 0) {
                    lp.height = lp.width * 560 / 1080;
                    userBackground.setLayoutParams(lp);
                    userBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
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
            getSupportActionBar().setTitle("个人主页");
            ((ListItem1) findViewById(R.id.clickProject)).setText("我的项目");
            ((ListItem1) findViewById(R.id.clickMaopao)).setText("我的冒泡");
            ((ListItem1) findViewById(R.id.clickTopic)).setText("我的话题");

        } else {
            findViewById(R.id.divideLocal).setVisibility(View.GONE);
            findViewById(R.id.clickLocal).setVisibility(View.GONE);
        }

        followCheckbox.setVisibility(isMe ? View.GONE : View.VISIBLE);
        findViewById(R.id.sendMessageLayout).setVisibility(isMe ? View.GONE : View.VISIBLE);
        findViewById(R.id.layoutLocal).setVisibility(isMe ? View.GONE : View.VISIBLE);

        findViewById(R.id.pointDivide).setVisibility(isMe ? View.VISIBLE : View.GONE);
        findViewById(R.id.clickPointRecord).setVisibility(isMe ? View.VISIBLE : View.GONE);




        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (isMe) {
            inflater.inflate(R.menu.user_detail_me, menu);
        } else {
            inflater.inflate(R.menu.user_detail, menu);
        }

        return super.onCreateOptionsMenu(menu);
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
        String dayToNow = Global.dayToNow(mUserObject.created_at);
        String lastActivity = Global.dayToNow(mUserObject.last_activity_at);
        user_detail_list_second = new String[]{
                dayToNow,
                lastActivity,
                mUserObject.global_key,
                mUserObject.company,
                mUserObject.job_str,
                mUserObject.location,
                mUserObject.tags_str
        };

        iconfromNetwork(icon, mUserObject.avatar, new AnimateFirstDisplayListener());
        icon.setTag(new MaopaoListFragment.ClickImageParam(mUserObject.avatar));
        icon.setOnClickListener(new ClickSmallImage(this));

        sex.setImageResource(sexs[mUserObject.sex]);

        name.setText(mUserObject.name);

        // 自己的页面不显示 关注
        if (!isMe) {
            int followId = mUserObject.follow ? R.drawable.checkbox_fans_big : R.drawable.checkbox_follow_big;
            followCheckbox.setVisibility(View.VISIBLE);
            followCheckbox.setButtonDrawable(followId);
            followCheckbox.setChecked(mUserObject.followed);
            followCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestParams params = new RequestParams();
                    params.put("users", mUserObject.global_key);
                    if (((CheckBox) v).isChecked()) {
                        postNetwork(HOST_FOLLOW, params, HOST_FOLLOW);
                    } else {
                        postNetwork(HOST_UNFOLLOW, params, HOST_UNFOLLOW);
                    }
                }
            });
        }

        TextView fans = (TextView) findViewById(R.id.fans);
        fans.setText(createSpan(String.format("%d  粉丝", mUserObject.fans_count)));
        fans.setOnClickListener(onClickFans);

        TextView follows = (TextView) findViewById(R.id.follows);
        follows.setText(createSpan(String.format("%d  关注", mUserObject.follows_count)));
        follows.setOnClickListener(onClickFollow);

        setListData();
    }

    private void initListFirst() {
        for (int i = 0; i < items.length; ++i) {
            View parent = findViewById(items[i]);
            TextView first = (TextView) parent.findViewById(R.id.first);
            first.setText(user_detail_activity_list_first[i]);
        }
    }

    private void setListData() {
        String[] secondContents = new String[]{
                mUserObject.location,
                mUserObject.slogan,
                mUserObject.tags_str
        };

        for (int i = 0; i < items.length; ++i) {
            View parent = findViewById(items[i]);
            TextView second = (TextView) parent.findViewById(R.id.second);
            String contentString = secondContents[i];
            if (contentString.isEmpty()) {
                contentString = "未填写";
                second.setTextColor(getResources().getColor(R.color.font_black_9));
            }

            second.setText(contentString);
        }
    }

    private SpannableString createSpan(String s) {
        SpannableString itemContent = new SpannableString(s);
        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.font_green));
        itemContent.setSpan(colorSpan, 0, itemContent.length() - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return itemContent;
    }

    @Click
    void sendMessage() {
        if (!dataIsLoaded()) {
            return;
        }

        Intent intent = new Intent(this, MessageListActivity_.class);
        intent.putExtra("mUserObject", mUserObject);
        startActivity(intent);
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
        } else if (tag.equals(HOST_FOLLOW)) {
            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast(R.string.follow_success);
                mUserObject.followed = true;
            } else {
                showButtomToast(R.string.follow_fail);
            }
            displayUserinfo();
        } else if (tag.equals(HOST_UNFOLLOW)) {
            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast(R.string.unfollow_success);
                mUserObject.followed = false;
            } else {
                showButtomToast(R.string.unfollow_fail);
            }
            displayUserinfo();
        }
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
        LocalProjectFileActivity_.intent(this)
                .start();
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

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

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
