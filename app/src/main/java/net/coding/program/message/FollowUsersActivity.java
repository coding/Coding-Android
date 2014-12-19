package net.coding.program.message;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.user.UsersListActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_friend)
//@OptionsMenu(R.menu.friend)
public class FollowUsersActivity extends BaseActivity implements FootUpdate.LoadMore {

    final String HOST_FOLLOWS = Global.HOST + "/api/user/friends?pageSize=1000";

    ArrayList<UserObject> mData = new ArrayList<UserObject>();

    @ViewById
    ListView listView;

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mData = AccountInfo.loadFriends(this, UsersListActivity.Friend.Follow);
        if (mData.isEmpty()) {
            showDialogLoading();
        }

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FollowUsersActivity.this, MessageListActivity_.class);
                intent.putExtra("mUserObject", mData.get((int) id));
                startActivity(intent);
            }
        });

        loadMore();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(HOST_FOLLOWS, HOST_FOLLOWS);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FOLLOWS)) {
            if (code == 0) {
                mData.clear();
                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    UserObject user = new UserObject(array.getJSONObject(i));
                    mData.add(user);
                }
                AccountInfo.saveFriends(this, mData, UsersListActivity.Friend.Follow);

            } else {
                showErrorMsg(code, respanse);
            }

            hideProgressDialog();

            adapter.notifyDataSetChanged();
        }
    }

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
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
                holder.followMutual = convertView.findViewById(R.id.followMutual);
                holder.followMutual.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final UserObject data = mData.get(position);

            holder.name.setText(data.name);
            iconfromNetwork(holder.icon, data.avatar);

            if (position == (mData.size() - 1)) {
                loadMore();
            }

            return convertView;
        }
    };

    static class ViewHolder {
        ImageView icon;
        TextView name;
        View followMutual;
    }

}
