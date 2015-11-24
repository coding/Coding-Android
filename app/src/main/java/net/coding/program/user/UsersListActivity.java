package net.coding.program.user;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.melnykov.fab.FloatingActionButton;

import net.coding.program.common.HtmlContent;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.message.MessageListActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.third.sidebar.IndexableListView;
import net.coding.program.third.sidebar.StringMatcher;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/*
 * 粉丝，关注的人列表
 */
@EActivity(R.layout.activity_users_list)
public class UsersListActivity extends BackActivity implements FootUpdate.LoadMore {

    public static final String HOST_FOLLOW = Global.HOST_API + "/user/follow?";
    public static final String HOST_UNFOLLOW = Global.HOST_API + "/user/unfollow?";
    public static final String TAG_USER_FOLLOWS = "TAG_USER_FOLLOWS";
    public static final String TAG_USER_FANS = "TAG_USER_FANS";
    public static final String RESULT_EXTRA_NAME = "name";
    public static final String RESULT_EXTRA_USESR = "RESULT_EXTRA_USESR";
    private static final String TAG_RELAY_MESSAGE = "TAG_RELAY_MESSAGE";
    final String HOST_FOLLOWS = Global.HOST_API + "/user/friends?pageSize=500";
    final String HOST_FANS = Global.HOST_API + "/user/followers?pageSize=500";
    final int RESULT_REQUEST_ADD = 1;
    final int RESULT_REQUEST_DETAIL = 2;
    @Extra
    Friend type;

    @Extra
    boolean select;

    @Extra
    boolean hideFollowButton; // 隐藏互相关注按钮，用于发私信选人的界面

    @Extra
    String titleName = ""; // 设置title

    @Extra
    String relayString = "";

    @Extra
    String statUrl; // 收藏项目的人

    @Extra
    UserParams mUserParam;
    ArrayList<UserObject> mData = new ArrayList<>();
    ArrayList<UserObject> mSearchData = new ArrayList<>();

    @ViewById
    IndexableListView listView;

    @ViewById
    FloatingActionButton floatButton;
    UserAdapter adapter = new UserAdapter();

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle();

//        mFootUpdate.init(listView, mInflater, this);
        adapter.initSection();
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setFastScrollAlwaysVisible(true);
        loadMore();

        if (type == Friend.Follow && isMyFriendList()) {
            floatButton.attachToListView(listView);
        } else {
            floatButton.setVisibility(View.GONE);
        }

        if (select) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    UserObject user = (UserObject) parent.getItemAtPosition(position);
                    intent.putExtra(RESULT_EXTRA_NAME, user.name);
                    intent.putExtra(RESULT_EXTRA_USESR, user);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        } else if (!relayString.isEmpty()) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    final UserObject user = (UserObject) parent.getItemAtPosition(position);
                    showDialog("转发", "转发给" + user.name, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Global.MessageParse messageParse = HtmlContent.parseMessage(relayString);
//                            String urls = "";
//                            for (String item : messageParse.uris) {
//                                urls += item + "/n";
//                            }

                            RequestParams params = new RequestParams();
                            String text = messageParse.text;
                            for (String url : messageParse.uris) {
                                String photoTemplate = "\n![图片](%s)";
                                text += String.format(photoTemplate, url);

                            }
                            params.put("content", text);
                            params.put("receiver_global_key", user.global_key);
                            postNetwork(MessageListActivity.HOST_MESSAGE_SEND, params, TAG_RELAY_MESSAGE);
                            showProgressBar(true, "发送中...");
                        }
                    });
                }
            });
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String globalKey = ((UserObject) parent.getItemAtPosition(position)).global_key;
                    UserDetailActivity_.intent(UsersListActivity.this)
                            .globalKey(globalKey)
                            .startForResult(RESULT_REQUEST_DETAIL);
                }
            });
        }
    }

    private boolean isMyFriendList() {
        return (mUserParam == null ||
                mUserParam.mUser.global_key.equals(MyApp.sUserObject.global_key)) &&
                statUrl == null;
    }

    @Override
    public void loadMore() {
        if (statUrl != null) {
            getNetwork(statUrl, statUrl);
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

    void setTitle() {
        if (!titleName.isEmpty()) {
            getSupportActionBar().setTitle(titleName);
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

        getSupportActionBar().setTitle(title);
    }

    private Friend getType() {
        Friend friendType = type;
        if (friendType == null) {
            friendType = mUserParam.mType;
        }
        return friendType;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(type == Friend.Follow ? R.menu.users_follow : R.menu.users_fans,
                menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setIcon(R.drawable.ic_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        try { // 更改搜索按钮的icon
            int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
            ImageView v = (ImageView) searchView.findViewById(searchImgId);
            v.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
            Global.errorLog(e);
        }

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

    @Click
    public final void floatButton() {
        action_add();
    }

    @OptionsItem
    void action_add() {
        startActivityForResult(new Intent(this, AddFollowActivity_.class), RESULT_REQUEST_ADD);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FOLLOWS) ||
                tag.equals(HOST_FANS) ||
                tag.equals(TAG_USER_FANS) ||
                tag.equals(TAG_USER_FOLLOWS)) {

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
//                Message.MessageObject item = new Message.MessageObject(respanse.getJSONObject("data"));
                showMiddleToast("发送成功");
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(statUrl)) {
            showProgressBar(false);
            hideProgressDialog();
            if (code == 0) {
                JSONArray json = respanse.optJSONArray("data");
                for (int i = 0; i < json.length(); ++i) {
                    UserObject userObject = new UserObject(json.getJSONObject(i));
                    mData.add(userObject);
                }

                Collections.sort(mData);
                mSearchData = new ArrayList<>(mData);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    public enum Friend {
        Follow, Fans
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
    }

    class UserAdapter extends BaseAdapter implements SectionIndexer, StickyListHeadersAdapter {

        private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
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

            holder.name.setText(data.name);
            iconfromNetwork(holder.icon, data.avatar);

            if (!hideFollowButton) {
                int drawableId = data.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
                holder.mutual.setButtonDrawable(drawableId);
                holder.mutual.setChecked(data.followed);
                holder.mutual.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RequestParams params = new RequestParams();
                        params.put("users", data.global_key);
                        if (((CheckBox) v).isChecked()) {
                            postNetwork(HOST_FOLLOW, params, HOST_FOLLOW, position, null);
                        } else {
                            postNetwork(HOST_UNFOLLOW, params, HOST_UNFOLLOW, position, null);
                        }
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

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.fragment_project_dynamic_list_head, parent, false);
                holder.mHead = (TextView) convertView.findViewById(R.id.head);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            holder.mHead.setText(mSectionTitle.get(getSectionForPosition(position)));
            return convertView;
        }

        @Override
        public long getHeaderId(int i) {
            return getSectionForPosition(i);
        }

        class HeaderViewHolder {
            TextView mHead;
        }
    }
}
