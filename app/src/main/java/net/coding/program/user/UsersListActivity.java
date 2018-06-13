package net.coding.program.user;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.LoadMore;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.model.project.ProjectServiceInfo;
import net.coding.program.common.param.MessageParse;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.message.MessageListActivity;
import net.coding.program.network.constant.Friend;
import net.coding.program.third.sidebar.IndexableListView;
import net.coding.program.third.sidebar.StringMatcher;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/*
 * 粉丝，关注的人列表
 */
@EActivity(R.layout.activity_users_list)
public class UsersListActivity extends BackActivity implements LoadMore {

    public static final String TAG_USER_FOLLOWS = "TAG_USER_FOLLOWS";
    public static final String TAG_USER_FANS = "TAG_USER_FANS";
    public static final String RESULT_EXTRA_NAME = "name";
    public static final String RESULT_EXTRA_USESR = "RESULT_EXTRA_USESR";
    private static final String TAG_RELAY_MESSAGE = "TAG_RELAY_MESSAGE";
    private static final String TAG_ADD_PROJECT_MEMBER = "TAG_ADD_PROJECT_MEMBER";
    public final String HOST_FOLLOW = getHostFollow();
    public final String HOST_UNFOLLOW = getHostUnfollow();
    final String HOST_FOLLOWS = Global.HOST_API + "/user/friends?pageSize=20";
    final String HOST_FANS = Global.HOST_API + "/user/followers?pageSize=20";
    final int RESULT_REQUEST_ADD = 1;
    final int RESULT_REQUEST_DETAIL = 2;
    @Extra
    Friend type;
    @Extra
    boolean selectType;
    @Extra
    ProjectObject projectObject;
    @Extra
    boolean hideFollowButton; // 隐藏互相关注按钮，用于发私信选人的界面
    @Extra
    String titleName = ""; // 设置title
    @Extra
    String relayString = "";
    @Extra
    String statUrl; // 收藏项目的人
    @Extra
    ProjectServiceInfo projectServiceInfo;
    @Extra
    UserParams mUserParam;

    ArrayList<UserObject> mData = new ArrayList<>();
    ArrayList<UserObject> mSearchData = new ArrayList<>();

    @ViewById
    IndexableListView listView;
    @ViewById
    TextView maxUserCount;

    UserAdapter adapter = new UserAdapter();

    public static String getHostFollow() {
        return Global.HOST_API + "/user/follow?";
    }

    public static String getHostUnfollow() {
        return Global.HOST_API + "/user/unfollow?";
    }

    @Override
    protected void initSetting() {
        super.initSetting();
        mData.clear();
        mSearchData.clear();
        adapter.notifyDataSetChanged();
    }

    @AfterViews
    protected final void initUsersListActivity() {
        if (mUserParam != null && mUserParam.mUser.global_key.equals("coding")) {
            showButtomToast("这个不能看：）");
            finish();
        }

        if (isMyFriendList()) {
            mData = AccountInfo.loadFriends(this, getType());
            mSearchData = new ArrayList<>(mData);
        }

        if (mData.isEmpty()) {
            showDialogLoading();
        }

        if (projectObject != null) {
            AddFollowActivity.bindData(maxUserCount, projectServiceInfo);
        } else {
            maxUserCount.setVisibility(View.GONE);
        }

        initActionBar();

        if (type == Friend.Follow && isMyFriendList()) {
            View addFollowView = getLayoutInflater().inflate(R.layout.activity_users_list_item, listView, false);
            addFollowView.findViewById(R.id.divideTitle).setVisibility(View.GONE);
            addFollowView.findViewById(R.id.divide_line).setVisibility(View.GONE);
            addFollowView.findViewById(R.id.followMutual).setVisibility(View.GONE);
            ((ImageView) addFollowView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_message_add_user);
            ((TextView) addFollowView.findViewById(R.id.name)).setText("添加好友");
            addFollowView.findViewById(R.id.rootLayout).setOnClickListener(v -> actionAdd());

            listView.addHeaderView(addFollowView, null, false);
        }

        adapter.initSection();
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setFastScrollAlwaysVisible(true);
        loadMore();

        if (selectType) {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent();
                UserObject user = (UserObject) parent.getItemAtPosition(position);
                intent.putExtra(RESULT_EXTRA_NAME, user.name);
                intent.putExtra(RESULT_EXTRA_USESR, user);
                setResult(Activity.RESULT_OK, intent);
                finish();
            });
        } else if (projectObject != null) {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String urlAddUser = Global.HOST_API + projectObject.getProjectPath() + "/members/gk/add";
                final UserObject data = (UserObject) parent.getItemAtPosition(position);
                new AlertDialog.Builder(UsersListActivity.this, R.style.MyAlertDialogStyle)
                        .setMessage(String.format("添加项目成员 %s ?", data.name))
                        .setPositiveButton("确定", (dialog, which) -> {
                            RequestParams params = new RequestParams();
                            params.put("users", data.global_key);
                            postNetwork(urlAddUser, params, TAG_ADD_PROJECT_MEMBER, -1, data);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        } else if (!relayString.isEmpty()) {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                final UserObject user = (UserObject) parent.getItemAtPosition(position);
                showDialog("转发给" + user.name, (dialog, which) -> {
                    MessageParse messageParse = HtmlContent.parseMessage(relayString);
                    RequestParams params = new RequestParams();
                    String text = messageParse.text;
                    for (String url : messageParse.uris) {
                        String photoTemplate = "\n![图片](%s)";
                        text += String.format(photoTemplate, url);

                    }
                    params.put("content", text);
                    params.put("receiver_global_key", user.global_key);
                    postNetwork(MessageListActivity.getSendMessage(), params, TAG_RELAY_MESSAGE);
                    showProgressBar(true, "发送中...");
                });
            });
        } else {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String globalKey = ((UserObject) parent.getItemAtPosition(position)).global_key;
                CodingCompat.instance().launchUserDetailActivity(this,
                        globalKey, RESULT_REQUEST_DETAIL);
            });
        }
    }

    private boolean isMyFriendList() {
        return (mUserParam == null ||
                mUserParam.mUser.global_key.equals(GlobalData.sUserObject.global_key)) &&
                statUrl == null;
    }

    @Override
    public void loadMore() {
        if (statUrl != null) {
            getNextPageNetwork(statUrl, statUrl);
        } else if (mUserParam == null) {
            if (type == Friend.Fans) {
                getNextPageNetwork(HOST_FANS, HOST_FANS);
            } else {
                getNextPageNetwork(HOST_FOLLOWS, HOST_FOLLOWS);
            }
        } else {

            if (mUserParam.isFans()) {
                String url = String.format(Global.HOST_API + "/user/followers/%s?pageSize=500", mUserParam.mUser.global_key);
                getNextPageNetwork(url, TAG_USER_FANS);
            } else {
                String url = String.format(Global.HOST_API + "/user/friends/%s?pageSize=500", mUserParam.mUser.global_key);
                getNextPageNetwork(url, TAG_USER_FOLLOWS);
            }
        }
    }

    void initActionBar() {
        if (!titleName.isEmpty()) {
            setActionBarTitle(titleName);
            return;
        }

        String title;
        if (mUserParam == null) {
            if (type == Friend.Fans) {
                title = "我的粉丝";
            } else {
                title = "我关注的人";
            }
        } else {
            final String format = "%s%s";
            String type = mUserParam.isFans() ? "的粉丝" : "关注的人";
            title = String.format(format, mUserParam.mUser.name, type);
        }

        setActionBarTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchItem(s);
                return true;
            }
        });

        return true;
    }

    private Friend getType() {
        Friend friendType = type;
        if (friendType == null) {
            friendType = mUserParam.mType;
        }
        return friendType;
    }

    private void searchItem(String s) {
        s = s.toLowerCase();

        mSearchData.clear();
        for (UserObject item : mData) {
            if (item.global_key.toLowerCase().contains(s) ||
                    item.name.toLowerCase().contains(s)) {
                mSearchData.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @OnActivityResult(RESULT_REQUEST_ADD)
    void result(int result) {
        if (result == RESULT_OK) {
            initSetting();
            loadMore();
        }
    }

    @OnActivityResult(RESULT_REQUEST_DETAIL)
    void resultDETAIL(int result) {
        if (result == RESULT_OK) {
            initSetting();
            loadMore();
        }
    }

    void actionAdd() {
        startActivityForResult(new Intent(this, AddFollowActivity_.class), RESULT_REQUEST_ADD);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FOLLOWS) ||
                tag.equals(HOST_FANS) ||
                tag.equals(TAG_USER_FANS) ||
                tag.equals(TAG_USER_FOLLOWS) ||
                tag.equals(statUrl)) {

            if (code == 0) {
                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");

                mData.clear();
                for (int i = 0; i < array.length(); ++i) {
                    UserObject user = new UserObject(array.getJSONObject(i));
                    mData.add(user);
                }

                Collections.sort(mData);

                if (isMyFriendList()) {
                    AccountInfo.saveFriends(this, mData, getType());
                }

                mSearchData = new ArrayList<>(mData);
            } else {
                showErrorMsg(code, respanse);
            }

            adapter.notifyDataSetChanged();
            hideProgressDialog();
        } else if (tag.equals(HOST_FOLLOW)) {
            if (code == 0) {
                showButtomToast(R.string.follow_success);
                mSearchData.get(pos).followed = true;
            } else {
                showButtomToast(R.string.follow_fail);
            }
            adapter.notifyDataSetChanged();
        } else if (tag.equals(HOST_UNFOLLOW)) {
            if (code == 0) {
                showButtomToast(R.string.unfollow_success);
                mSearchData.get(pos).followed = false;
            } else {
                showButtomToast(R.string.unfollow_fail);
            }
            adapter.notifyDataSetChanged();
        } else if (tag.equals(TAG_RELAY_MESSAGE)) {
            showProgressBar(false);
            if (code == 0) {
                showMiddleToast("发送成功");
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
//        } else if (tag.equals(statUrl)) {
//            showProgressBar(false);
//            hideProgressDialog();
//            if (code == 0) {
//                JSONArray json = respanse.optJSONArray("data");
//                for (int i = 0; i < json.length(); ++i) {
//                    UserObject userObject = new UserObject(json.getJSONObject(i));
//                    mData.add(userObject);
//                }
//
//                Collections.sort(mData);
//                mSearchData = new ArrayList<>(mData);
//                adapter.notifyDataSetChanged();
//            } else {
//                showErrorMsg(code, respanse);
//            }
        } else if (tag.equals(TAG_ADD_PROJECT_MEMBER)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "添加成员");
                showMiddleToast(String.format("添加项目成员 %s 成功", ((UserObject) data).name));
                projectServiceInfo.member++;
                AddFollowActivity.bindData(maxUserCount, projectServiceInfo);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    public enum Type {
        Select
    }

    public static class UserParams implements Serializable {
        public UserObject mUser;
        private Friend mType;

        public UserParams(UserObject user, Friend mType) {
            this.mUser = user;
            this.mType = mType;
        }

        public String getName() {
            return mUser.name;
        }

        public boolean isFans() {
            return mType == Friend.Fans;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
        TextView divideTitle;
        View bottomLine;
    }

    class UserAdapter extends BaseAdapter implements SectionIndexer {

        private String mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
        private ArrayList<String> mSectionTitle = new ArrayList<>();
        private ArrayList<Integer> mSectionId = new ArrayList<>();

        public void initSection() {
            mSectionTitle.clear();
            mSectionId.clear();

            if (mData.size() > 0) {
                String lastLetter = "";

                for (int i = 0; i < mData.size(); ++i) {
                    UserObject item = mData.get(i);
                    if (!item.getFirstLetter().equals(lastLetter)) {
                        lastLetter = item.getFirstLetter();
                        mSectionTitle.add(item.getFirstLetter());
                        mSectionId.add(i);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return mSearchData.size();
        }

        @Override
        public Object getItem(int position) {
            return mSearchData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_users_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followMutual);
                holder.bottomLine = convertView.findViewById(R.id.divide_line);
                if (hideFollowButton) {
                    holder.mutual.setVisibility(View.INVISIBLE);
                }
                holder.divideTitle = (TextView) convertView.findViewById(R.id.divideTitle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final UserObject data = mSearchData.get(position);

            if (isSection(position)) {
                holder.divideTitle.setVisibility(View.VISIBLE);
                holder.divideTitle.setText(data.getFirstLetter());
            } else {
                holder.divideTitle.setVisibility(View.GONE);
            }

            int nextPosition = position + 1;
            if (nextPosition >= getCount() || isSection(nextPosition)) {
                holder.bottomLine.setVisibility(View.INVISIBLE);
            } else {
                holder.bottomLine.setVisibility(View.VISIBLE);
            }

            holder.name.setText(data.name);
            iconfromNetwork(holder.icon, data.avatar);

            if (!hideFollowButton) {
                int drawableId = data.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
                holder.mutual.setButtonDrawable(drawableId);
                holder.mutual.setChecked(data.followed);
                holder.mutual.setOnClickListener(v -> {
                    RequestParams params = new RequestParams();
                    params.put("users", data.global_key);
                    if (((CheckBox) v).isChecked()) {
                        postNetwork(HOST_FOLLOW, params, HOST_FOLLOW, position, null);
                    } else {
                        postNetwork(HOST_UNFOLLOW, params, HOST_UNFOLLOW, position, null);
                    }
                });
            }

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            initSection();
        }

        private boolean isSection(int pos) {
            if (getCount() == 0) {
                return true;
            }

            if (pos == 0) {
                return true;
            }

            String currentItem = mData.get(pos).getFirstLetter();
            String preItem = mData.get(pos - 1).getFirstLetter();
            return !currentItem.equals(preItem);
        }

        @Override
        public int getPositionForSection(int section) {
            // If there is no item for current section, previous section will be selected
            for (int i = section; i >= 0; i--) {
                for (int j = 0; j < getCount(); j++) {
                    if (i == 0) {
                        // For numeric section
                        for (int k = 0; k <= 9; k++) {
                            if (StringMatcher.match(((UserObject) getItem(j)).getFirstLetter().toUpperCase(), String.valueOf(k)))
                                return j;
                        }
                    } else {
                        if (StringMatcher.match(((UserObject) getItem(j)).getFirstLetter().toUpperCase(), String.valueOf(mSections.charAt(i))))
                            return j;
                    }
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            String[] sections = new String[mSections.length()];
            for (int i = 0; i < mSections.length(); i++)
                sections[i] = String.valueOf(mSections.charAt(i));
            return sections;
        }

    }
}
