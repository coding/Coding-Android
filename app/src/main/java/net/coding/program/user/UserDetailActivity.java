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

import net.coding.program.BaseActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.UserObject;

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
public class UserDetailActivity extends BaseActivity {

    UserObject mUserObject;

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

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initListFirst();

        if (globalKey != null) {
            if (globalKey.equals(MyApp.sUserObject.global_key)) {
                setTitleMyPage();
                resizeHead();
            }
            getNetwork(HOST_USER_INFO + globalKey, HOST_USER_INFO);
        } else {
            String name = getIntent().getData().getQueryParameter("name");
            if (name.equals(MyApp.sUserObject.name)) {
                setTitleMyPage();
                resizeHead();
            }

            getNetwork(HOST_USER_INFO + name, HOST_USER_INFO);
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

    private void setTitleMyPage() {
        getSupportActionBar().setTitle("个人主页");
        ((TextView) findViewById(R.id.titleProject)).setText("我的项目");
        ((TextView) findViewById(R.id.titleMaopao)).setText("我的冒泡");
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

    public final int RESULT_EDIT = 0;

    @OnActivityResult(RESULT_EDIT)
    void onResult() {
        getNetwork(HOST_USER_INFO + mUserObject.global_key, HOST_USER_INFO);
    }

    private void resizeHead() {
        isMe = true;
        invalidateOptionsMenu();

        followCheckbox.setVisibility(View.GONE);
        findViewById(R.id.sendMessageLayout).setVisibility(View.GONE);
    }


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

    int sexs[] = new int[]{
            R.drawable.ic_sex_boy,
            R.drawable.ic_sex_girl,
            android.R.color.transparent
    };

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

    private final int[] items = new int[]{
            R.id.pos0,
            R.id.pos1,
            R.id.pos2
    };

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
                second.setTextColor(getResources().getColor(R.color.font_gray));
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
        Intent intent = new Intent(this, MessageListActivity_.class);
        intent.putExtra("mUserObject", mUserObject);
        startActivity(intent);
    }

    final String HOST_USER_INFO = Global.HOST + "/api/user/key/";
    public static final String HOST_FOLLOW = Global.HOST + "/api/user/follow?";
    public static final String HOST_UNFOLLOW = Global.HOST + "/api/user/unfollow?";

    boolean mNeedUpdate = false;

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
        setResult(mNeedUpdate ? RESULT_OK : RESULT_CANCELED);
        super.onBackPressed();
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Click
    public void clickProject() {
        UserProjectActivity_.intent(this).mUserObject(mUserObject).start();
    }

    @Click
    public void clickMaopao() {
        Intent intent = new Intent(UserDetailActivity.this, UserMaopaoActivity.class);
        intent.putExtra(UserMaopaoActivity.PARAM_ID, mUserObject.id);
        startActivity(intent);
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
