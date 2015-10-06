package net.coding.program.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_add_follow)
public class AddFollowActivity extends BackActivity implements Handler.Callback {

    public static final int RESULT_USER_DETAIL = 1000;

    String HOST_SEARCH_USER = Global.HOST_API + "/user/search?key=%s";
    String urlAddUser = "";
    ArrayList<UserObject> mData = new ArrayList<>();

    boolean mNeedUpdate = false;

    @Extra
    ProjectObject mProjectObject;

    @ViewById
    ListView listView;

    int flag = 0;

    Handler mHandler;

    BaseAdapter baseAdapter;

    @AfterViews
    protected final void initAddFollowActivity() {
        mHandler = new WeakRefHander(this);

        if (mProjectObject == null) {
            baseAdapter = new FollowAdapter(this, true, mData);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserObject userObject = mData.get((int) id);
                    UserDetailActivity_
                            .intent(AddFollowActivity.this)
                            .globalKey(userObject.global_key)
                            .startForResult(RESULT_USER_DETAIL);
                }
            });
        } else {
            urlAddUser = String.format(Global.HOST_API + "/project/%d/members/add?", mProjectObject.getId());
            getSupportActionBar().setTitle("添加项目成员");
            baseAdapter = new FollowAdapter(this, false, mData);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final UserObject data = mData.get((int) id);

                    AlertDialog.Builder builder = new AlertDialog.Builder(AddFollowActivity.this);
                    AlertDialog dialog = builder.setMessage(String.format("添加项目成员 %s ?", data.name))
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RequestParams params = new RequestParams();
                                    params.put("users", data.id);
                                    postNetwork(urlAddUser, params, urlAddUser, -1, data);
                                }
                            })
                            .setNegativeButton("取消", null).show();
                    dialogTitleLineColor(dialog);
                }
            });
        }
        listView.setAdapter(baseAdapter);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == flag) {
            getNetwork(String.format(HOST_SEARCH_USER, Global.encodeUtf8((String) msg.obj)), HOST_SEARCH_USER);
        }

        return true;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_SEARCH_USER)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONArray("data");
                mData.clear();
                for (int i = 0; i < jsonArray.length(); ++i) {
                    UserObject user = new UserObject(jsonArray.getJSONObject(i));
                    mData.add(user);
                }
            } else {
                showErrorMsg(code, respanse);
            }
            baseAdapter.notifyDataSetChanged();

        } else if (tag.equals(UsersListActivity.HOST_FOLLOW)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "关注他人");
                mNeedUpdate = true;
                showButtomToast(R.string.follow_success);
                ((UserObject) data).followed = true;
            } else {
                showButtomToast(R.string.follow_fail);
            }
            baseAdapter.notifyDataSetChanged();
        } else if (tag.equals(UsersListActivity.HOST_UNFOLLOW)) {
            umengEvent(UmengEvent.USER, "取消关注");

            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast("取消关注成功");
                ((UserObject) data).followed = false;
            } else {
                showButtomToast("取消关注失败");
            }
            baseAdapter.notifyDataSetChanged();
        } else if (tag.equals(urlAddUser)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "添加成员");

                mNeedUpdate = true;
                showMiddleToast(String.format("添加项目成员 %s 成功", ((UserObject) data).name));
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_follow_activity, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.expandActionView();
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.setQueryHint("用户名，email，个性后缀");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return false;
            }
        });

        return true;
    }

    void search(String s) {
        int flagHandler = ++flag;
        Message message = Message.obtain(mHandler, flagHandler, s);
        mHandler.sendMessageDelayed(message, 1000);
    }

    @Override
    public void onBackPressed() {
        setResult(mNeedUpdate ? RESULT_OK : RESULT_CANCELED);
        finish();
    }

    @OnActivityResult(RESULT_USER_DETAIL)
    protected final void resultUserDetail(int result, Intent data) {
        if (result == RESULT_OK) {
            Object object = data.getSerializableExtra("data");
            if (object instanceof UserObject) {
                UserObject user = (UserObject) object;

                for (int i = 0; i < mData.size(); ++i) {
                    if (user.global_key.equals(mData.get(i).global_key)) {
                        mData.add(i, user);
                        mData.remove(i + 1);
                        baseAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
    }

    private class FollowAdapter extends ArrayAdapter<UserObject> {

        boolean mShowFollowButton = true;

        public FollowAdapter(Context context, boolean showFollowButton, List<UserObject> objects) {
            super(context, 0, objects);
            mShowFollowButton = showFollowButton;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_add_follow_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followed);
                if (mShowFollowButton) {
                    holder.mutual.setVisibility(View.VISIBLE);
                    holder.mutual.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserObject user = (UserObject) v.getTag(R.id.followed);
                            RequestParams params = new RequestParams();
                            params.put("users", user.global_key);
                            if (((CheckBox) v).isChecked()) {
                                postNetwork(UsersListActivity.HOST_FOLLOW, params, UsersListActivity.HOST_FOLLOW, -1, user);
                            } else {
                                postNetwork(UsersListActivity.HOST_UNFOLLOW, params, UsersListActivity.HOST_UNFOLLOW, -1, user);
                            }
                        }
                    });
                } else {
                    holder.mutual.setVisibility(View.INVISIBLE);
                }

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final UserObject data = getItem(position);

            iconfromNetwork(holder.icon, data.avatar);
            holder.name.setText(String.format("%s - %s", data.name, data.global_key));

            if (mShowFollowButton) {
                int drawableId = data.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
                holder.mutual.setButtonDrawable(drawableId);
                holder.mutual.setChecked(data.followed);
                holder.mutual.setTag(R.id.followed, data);
            }

            return convertView;
        }
    }
}
