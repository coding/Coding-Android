package net.coding.program.project.detail;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.TaskObject;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_members)
public class MembersActivity extends BackActivity implements FootUpdate.LoadMore {

    @Extra
    int mProjectObjectId;

    @Extra
    ArrayList<UserObject> mWatchUsers = new ArrayList<>();

    @Extra
    boolean mPickWatch = false;

    String getProjectMembers = "getProjectMembers";
    String urlMembers = "";

    ArrayList<TaskObject.Members> mMembersArray = new ArrayList<>();
    @ViewById
    ListView listView;
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mMembersArray.size();
        }

        @Override
        public Object getItem(int position) {
            return mMembersArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_members_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.watchCheck = convertView.findViewById(R.id.watchCheck);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TaskObject.Members data = mMembersArray.get(position);
            holder.name.setText(data.user.name);
            iconfromNetwork(holder.icon, data.user.avatar);

            updateChecked(holder, data);

            if (position == mMembersArray.size() - 1) {
                loadMore();
            }

            return convertView;
        }

        private void updateChecked(ViewHolder holder, TaskObject.Members data) {
            if (!mPickWatch) {
                return;
            }

            for (UserObject item : mWatchUsers) {
                if (data.user.id == item.id) {
                    holder.watchCheck.setVisibility(View.VISIBLE);
                    return;
                }
            }

            holder.watchCheck.setVisibility(View.INVISIBLE);
        }
    };

    @AfterViews
    protected final void initMembersActivity() {
        final String format = Global.HOST_API + "/project/%d/members?";
        urlMembers = String.format(format, mProjectObjectId);

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mPickWatch) {
                    Intent intent = new Intent();
                    TaskObject.Members members = mMembersArray.get(position);
                    intent.putExtra("members", members);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    TaskObject.Members members = mMembersArray.get(position);
                    for (int i = 0; i < mWatchUsers.size(); ++i) {
                        UserObject item = mWatchUsers.get(i);
                        if (members.user.id == item.id) {
                            mWatchUsers.remove(i);
                            adapter.notifyDataSetChanged();
                            return;
                        }
                    }

                    mWatchUsers.add(members.user);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        loadMore();
    }

    @Override
    public void onBackPressed() {
        if (mPickWatch) {
            Intent intent = new Intent();
            intent.putExtra("data", mWatchUsers);
            setResult(Activity.RESULT_OK, intent);
        }

        super.onBackPressed();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(urlMembers, getProjectMembers);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(getProjectMembers)) {
            if (code == 0) {
                ArrayList<TaskObject.Members> usersInfo = new ArrayList<>();

                JSONArray members = respanse.getJSONObject("data").getJSONArray("list");

                for (int i = 0; i < members.length(); ++i) {
                    mMembersArray.add(new TaskObject.Members(members.getJSONObject(i)));
                }

                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @OptionsItem
    public void action_add() {

    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        View watchCheck;
    }
}
