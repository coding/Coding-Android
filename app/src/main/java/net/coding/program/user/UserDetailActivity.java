package net.coding.program.user;

import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.UserInfoActivity_;
import net.coding.program.common.ClickSmallImage;
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
    ViewGroup layoutHead;

    @ViewById
    ImageView icon;

    @ViewById
    TextView name;

    @ViewById
    TextView slogan;

    @ViewById
    ImageView sendMessage;

    @ViewById
    CheckBox followCheckbox;

    @ViewById
    ImageView sex;

    @ViewById
    ListView listView;

    @StringArrayRes
    String[] user_detail_list_first;

    String[] user_detail_list_second;

    boolean isMe = false;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (globalKey != null) {
            if (globalKey.equals(MyApp.sUserObject.global_key)) {
                resizeHead();
            }

            getNetwork(HOST_USER_INFO + globalKey, HOST_USER_INFO);
        } else {
            String name = getIntent().getData().getQueryParameter("name");
            if (name.equals(MyApp.sUserObject.name)) {
                resizeHead();
            }

            getNetwork(HOST_USER_INFO + name, HOST_USER_INFO);
        }
    }

    @Click
    void layoutHead() {
        if (isMe) {
            action_edit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMe) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_detail_edit, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem
    void action_edit() {
        UserInfoActivity_
                .intent(this)
                .startForResult(RESULT_EDIT);
    }

    public final int RESULT_EDIT = 0;

    @OnActivityResult(RESULT_EDIT)
    void onResult() {
        getNetwork(HOST_USER_INFO + mUserObject.global_key, HOST_USER_INFO);
    }

    private void resizeHead() {
        isMe = true;
        invalidateOptionsMenu();

        sendMessage.setVisibility(View.GONE);
        followCheckbox.setVisibility(View.GONE);

        layoutHead.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 将head高度设置为150dp
                if (layoutHead.getHeight() > 0) {
                    ViewGroup.LayoutParams params = layoutHead.getLayoutParams();
                    params.height = Global.dpToPx(150);
                    layoutHead.setLayoutParams(params);
                    layoutHead.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });


    }

    View.OnClickListener onClickMaopao = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(UserDetailActivity.this, UserMaopaoActivity.class);
            intent.putExtra("id", mUserObject.id);
            startActivity(intent);
        }
    };

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

        iconfromNetwork(icon, mUserObject.avatar);
        icon.setTag(new MaopaoListFragment.ClickImageParam(mUserObject.avatar));
        icon.setOnClickListener(new ClickSmallImage(this));

        sex.setImageResource(sexs[mUserObject.sex]);

        name.setText(mUserObject.name);

        String sloganString = mUserObject.slogan;
        if (sloganString.isEmpty()) {
            sloganString = "未填写座右铭";
        }
        slogan.setText(sloganString);

        // 自己的页面不显示 关注
        if (!isMe) {
            int followId = mUserObject.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
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
        fans.setText(createSpan(String.format("粉丝  %d", mUserObject.fans_count)));
        fans.setOnClickListener(onClickFans);

        TextView follows = (TextView) findViewById(R.id.follows);
        follows.setText(createSpan(String.format("关注  %d", mUserObject.follows_count)));
        follows.setOnClickListener(onClickFollow);

        TextView maopao = (TextView) findViewById(R.id.maopao);
        maopao.setOnClickListener(onClickMaopao);
        maopao.setText(createSpan(String.format("冒泡  %d", mUserObject.tweets_count)));

        listView.setAdapter(baseAdapter);
    }

    private SpannableString createSpan(String s) {
        SpannableString itemContent = new SpannableString(s);
        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.font_green));
        itemContent.setSpan(colorSpan, 3, itemContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                showButtomToast("成功");
                mUserObject.followed = true;
            } else {
                showButtomToast("失败");
            }
            displayUserinfo();
        } else if (tag.equals(HOST_UNFOLLOW)) {
            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast("成功");
                mUserObject.followed = false;
            } else {
                showButtomToast("失败");
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


    BaseAdapter baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return user_detail_list_first.length;
        }

        @Override
        public Object getItem(int position) {
            return user_detail_list_first[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_2_text_divide_head, parent, false);
                holder = new ViewHolder();
                holder.first = (TextView) convertView.findViewById(R.id.first);
                holder.second = (TextView) convertView.findViewById(R.id.second);
                holder.headDivide = convertView.findViewById(R.id.headDivide);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.first.setText(user_detail_list_first[position]);

            String secondString = user_detail_list_second[position];
            if (secondString.isEmpty()) {
                secondString = "未填写";
            }
            holder.second.setText(secondString);

            if (position == 0 ||
                    position == 3 ||
                    position == 6) {
                holder.headDivide.setVisibility(View.VISIBLE);
            } else {
                holder.headDivide.setVisibility(View.GONE);
            }

            return convertView;
        }
    };

    static class ViewHolder {
        TextView first;
        TextView second;
        View headDivide;
    }
}
