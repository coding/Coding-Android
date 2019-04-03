package net.coding.program.user;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.model.project.ProjectServiceInfo;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.network.constant.Friend;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
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
    private static final int RESULT_ADD_PROJECT_MEMBER = 1;
    private static final String TAG_SERVICE_INFO = "TAG_SERVICE_INFO";
    String HOST_SEARCH_USER = Global.HOST_API + "/user/search?key=%s";
    String urlAddUser = "";
    ArrayList<UserObject> mData = new ArrayList<>();
    boolean mNeedUpdate = false;

    @Extra
    ProjectObject mProjectObject;

    @ViewById
    ListView listView;

    @ViewById
    View friendLayout, userCountLine;

    @ViewById
    TextView maxUserCount;

    int flag = 0;

    Handler mHandler;

    BaseAdapter baseAdapter;

    ProjectServiceInfo serviceInfo;

    public static void bindData(TextView maxUserCount, ProjectServiceInfo serviceInfo) {
        if (TextUtils.isEmpty(GlobalData.getEnterpriseGK())) {
            maxUserCount.setVisibility(View.VISIBLE);
            int count = serviceInfo.maxmember - serviceInfo.member;
            if (count > 0) {
                maxUserCount.setText(String.format("你还可以添加 %s 个项目成员", count));
                maxUserCount.setTextColor(CodingColor.font2);
            } else {
                maxUserCount.setText("已达到成员最大数，不能再继续添加成员！");
                maxUserCount.setTextColor(CodingColor.fontRed);
            }
        } else {
            maxUserCount.setVisibility(View.GONE);
        }
    }

    @AfterViews
    protected final void initAddFollowActivity() {
        mHandler = new WeakRefHander(this);

        if (mProjectObject == null) {
            friendLayout.setVisibility(View.GONE);
            maxUserCount.setVisibility(View.GONE);
            userCountLine.setVisibility(View.GONE);
            baseAdapter = new FollowAdapter(this, true, mData);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                UserObject userObject = mData.get((int) id);
                CodingCompat.instance().launchUserDetailActivity(this, userObject.global_key,
                        RESULT_USER_DETAIL);
            });
        } else {
            urlAddUser = Global.HOST_API + mProjectObject.getProjectPath() + "/members/gk/add";
            setActionBarTitle("添加项目成员");
            friendLayout.setVisibility(View.VISIBLE);
            maxUserCount.setVisibility(View.GONE);
            userCountLine.setVisibility(View.GONE);
            baseAdapter = new FollowAdapter(this, false, mData);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                final UserObject data = mData.get((int) id);
                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setMessage(String.format("添加项目成员 %s ?", data.name))
                        .setPositiveButton("确定", (dialog, which) -> {
                            RequestParams params = new RequestParams();
                            params.put("users", data.global_key);
                            postNetwork(urlAddUser, params, urlAddUser, -1, data);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        }
        listView.setAdapter(baseAdapter);
        loadServiceInfo();
    }

    private void loadServiceInfo() {
        if (mProjectObject == null) {
            return;
        }

        final String url = mProjectObject.getHttpProjectApi() + "/service_info";
        getNetwork(url, TAG_SERVICE_INFO);
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

        } else if (tag.equals(UsersListActivity.getHostFollow())) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "关注他人");
                mNeedUpdate = true;
                showButtomToast(R.string.follow_success);
                ((UserObject) data).followed = true;
            } else {
                showButtomToast(R.string.follow_fail);
            }
            baseAdapter.notifyDataSetChanged();
        } else if (tag.equals(UsersListActivity.getHostUnfollow())) {
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
                serviceInfo.member++;
                bindData(maxUserCount, serviceInfo);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_SERVICE_INFO)) {
            if (code == 0) {
                serviceInfo = new ProjectServiceInfo(respanse.optJSONObject("data"));
                bindData(maxUserCount, serviceInfo);
                userCountLine.setVisibility(View.VISIBLE);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Click
    void listItemFollow() {
        UsersListActivity.UserParams userParams = new UsersListActivity.UserParams(GlobalData.sUserObject,
                Friend.Follow);

        UsersListActivity_
                .intent(this)
                .mUserParam(userParams)
                .type(Friend.Follow)
                .hideFollowButton(true)
                .projectObject(mProjectObject)
                .projectServiceInfo(serviceInfo)
                .startForResult(RESULT_ADD_PROJECT_MEMBER);
    }

    @Click
    void listItemFans() {
        UsersListActivity.UserParams userParams = new UsersListActivity.UserParams(GlobalData.sUserObject,
                Friend.Fans);

        UsersListActivity_
                .intent(this)
                .mUserParam(userParams)
                .type(Friend.Fans)
                .projectObject(mProjectObject)
                .projectServiceInfo(serviceInfo)
                .hideFollowButton(true)
                .startForResult(RESULT_ADD_PROJECT_MEMBER);
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
        searchView.setQueryHint("用户名，邮箱，昵称");
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

    protected void search(String s) {
        if (s == null || s.replaceAll(" ", "").replaceAll("　", "").isEmpty()) {
            if (mProjectObject != null) {
                friendLayout.setVisibility(View.VISIBLE);
            }

            mData.clear();
            baseAdapter.notifyDataSetChanged();
            return;
        }

        friendLayout.setVisibility(View.GONE);

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

    @OnActivityResult(RESULT_ADD_PROJECT_MEMBER)
    void onResultAddProjectMember(int result) {
        loadServiceInfo();
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
                                postNetwork(UsersListActivity.getHostFollow(), params, UsersListActivity.getHostFollow(), -1, user);
                            } else {
                                postNetwork(UsersListActivity.getHostUnfollow(), params, UsersListActivity.getHostUnfollow(), -1, user);
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
            holder.name.setText(String.format("%s", data.name));

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
