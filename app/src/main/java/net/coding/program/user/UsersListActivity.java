package net.coding.program.user;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
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

@EActivity(R.layout.activity_users_list)
public class UsersListActivity extends BaseActivity implements FootUpdate.LoadMore {

    public static enum Friend {
        Follow, Fans
    };

    @Extra
    Friend type;

    @Extra
    boolean select;

    @Extra
    UserParams mUserParam;

    public static class UserParams implements Serializable {
        private Friend mType;
        public UserObject mUser;

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

    final String HOST_FOLLOWS = Global.HOST + "/api/user/friends?pageSize=500";
    final String HOST_FANS = Global.HOST + "/api/user/followers?pageSize=500";

    public static final String HOST_FOLLOW = Global.HOST + "/api/user/follow?";
    public static final String HOST_UNFOLLOW = Global.HOST + "/api/user/unfollow?";

    public static final String TAG_USER_FOLLOWS = "TAG_USER_FOLLOWS";
    public static final String TAG_USER_FANS = "TAG_USER_FANS";


    ArrayList<UserObject> mData = new ArrayList<UserObject>();
    ArrayList<UserObject> mSearchData = new ArrayList<UserObject>();

    @ViewById
    ListView listView;

    @Override
    protected void initSetting() {
        super.initSetting();
        mData.clear();
        mSearchData.clear();
        adapter.notifyDataSetChanged();
    }

    @AfterViews
    void init() {
        if (mUserParam != null && mUserParam.mUser.global_key.equals("coding")) {
            showButtomToast("这个不能看：）");
            finish();
        }

        if (displayMyFriend()) {
            mData = AccountInfo.loadFriends(this, getType());
            mSearchData = new ArrayList<UserObject>(mData);
        }

        if (mData.isEmpty()) {
            showDialogLoading();
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle();

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        loadMore();

        if (select) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    String name = ((UserObject) parent.getItemAtPosition(position)).name;
                    intent.putExtra("name", name);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
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

    private boolean displayMyFriend() {
        if (mUserParam != null
                && !mUserParam.mUser.global_key.equals(MyApp.sUserObject.global_key)) {
            return false;
        }

        return true;
    }

    @Override
    public void loadMore() {
        if (mUserParam == null) {
            if (type == Friend.Fans) {
                getNextPageNetwork(HOST_FANS, HOST_FANS);
            } else {
                getNextPageNetwork(HOST_FOLLOWS, HOST_FOLLOWS);
            }
        } else {

            if (mUserParam.isFans()) {
                String url = String.format(Global.HOST + "/api/user/followers/%s?pageSize=500", mUserParam.mUser.global_key);
                getNextPageNetwork(url, TAG_USER_FANS);
            } else {
                String url = String.format(Global.HOST + "/api/user/friends/%s?pageSize=500", mUserParam.mUser.global_key);
                getNextPageNetwork(url, TAG_USER_FOLLOWS);
            }
        }
    }

    void setTitle() {
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

        getActionBar().setTitle(title);
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
        if (type == Friend.Follow) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.users_follow, menu);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            searchItem.setIcon(R.drawable.ic_menu_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            try { // 更改搜索按钮的icon
                int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
                ImageView v = (ImageView) searchView.findViewById(searchImgId);
                v.setImageResource(R.drawable.ic_menu_search);
            } catch (Exception e) {
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

        return super.onCreateOptionsMenu(menu);
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

    final int RESULT_REQUEST_ADD = 1;
    final int RESULT_REQUEST_DETAIL = 2;

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

                if (displayMyFriend()) {
                    AccountInfo.saveFriends(this, mData, getType());
                }

                mSearchData = new ArrayList<UserObject>(mData);
            } else {
                showErrorMsg(code, respanse);
            }

            adapter.notifyDataSetChanged();
            hideProgressDialog();
        } else if (tag.equals(HOST_FOLLOW)) {
            if (code == 0) {
                showButtomToast("成功");
                mSearchData.get(pos).followed = true;
            } else {
                showButtomToast("失败");
            }
            adapter.notifyDataSetChanged();
        } else if (tag.equals(HOST_UNFOLLOW)) {
            if (code == 0) {
                showButtomToast("成功");
                mSearchData.get(pos).followed = false;
            } else {
                showButtomToast("失败");
            }
            adapter.notifyDataSetChanged();
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    BaseAdapter adapter = new BaseAdapter() {
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
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final UserObject data = mSearchData.get(position);

            holder.name.setText(data.name);
            iconfromNetwork(holder.icon, data.avatar);

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

            return convertView;
        }
    };

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
    }
}
