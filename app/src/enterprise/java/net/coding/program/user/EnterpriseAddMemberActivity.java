package net.coding.program.user;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.UserObject;
import net.coding.program.model.project.ProjectServiceInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_enterprise_add_member)
public class EnterpriseAddMemberActivity extends BaseEnterpriseUserListActivity {

    private static final String TAG_SERVICE_INFO = "TAG_SERVICE_INFO";
    private static final String TAG_HOST_ADD_USER = "TAG_HOST_ADD_USER";
    private static final String TAG_HOST_DELETE_USER = "TAG_HOST_DELETE_USER";

    ArrayList<UserObject> pickedData = new ArrayList<>();

    @Extra
    ProjectObject projectObject;

    @Extra
    ArrayList<String> pickedGlobalKeys;

    @ViewById
    TextView maxUserCount;

    @ViewById
    protected ListView listView;

    ProjectServiceInfo serviceInfo;

    @AfterViews
    void initEnterpriseAddMemberActivity() {
        listView.setAdapter(adapter);
        loadServiceInfo();
    }

    @Override
    protected void parseUserJson(JSONObject respanse) {
        super.parseUserJson(respanse);

        for (String item : pickedGlobalKeys) {
            for (UserObject user : listData) {
                if (item.equals(user.global_key)) {
                    pickedData.add(user);
                    break;
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_ADD_USER)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "添加成员");

                pickedData.add((UserObject) data);

                showMiddleToast(String.format("添加项目成员 %s 成功", ((UserObject) data).name));
                serviceInfo.member++;
                AddFollowActivity.bindData(maxUserCount, serviceInfo);
            } else {
                showErrorMsg(code, respanse);
            }
            adapter.notifyDataSetChanged();

        } else if (tag.equals(TAG_HOST_DELETE_USER)) {
            if (code == 0) {
                pickedData.remove((UserObject) data);

                showMiddleToast(String.format("移除项目成员 %s 成功", ((UserObject) data).name));
                serviceInfo.member--;
                AddFollowActivity.bindData(maxUserCount, serviceInfo);
            } else {
                showErrorMsg(code, respanse);
            }
            adapter.notifyDataSetChanged();

        } else if (tag.equals(TAG_SERVICE_INFO)) {
            if (code == 0) {
                serviceInfo = new ProjectServiceInfo(respanse.optJSONObject("data"));
                AddFollowActivity.bindData(maxUserCount, serviceInfo);
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    private void loadServiceInfo() {
        final String url = projectObject.getHttpProjectApi() + "/service_info";
        getNetwork(url, TAG_SERVICE_INFO);
    }

    private View.OnClickListener clickMutual = v -> {
        UserObject user = (UserObject) v.getTag(R.id.followed);
        if (((CheckBox) v).isChecked()) {
            final String urlAddUser = Global.HOST_API + projectObject.getProjectPath() + "/members/gk/add";
            RequestParams param = new RequestParams();
            param.put("users", user.global_key);
            postNetwork(urlAddUser, param, TAG_HOST_ADD_USER, -1, user);
        } else {
            final String urlDeleteUser = Global.HOST_API + "/project/%s/kickout/%s";
            String url = String.format(urlDeleteUser, projectObject.getId(), user.id);
            postNetwork(url, new RequestParams(), TAG_HOST_DELETE_USER, -1, user);
        }
    };

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.enterprise_add_member_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followed);
                holder.mutual.setVisibility(View.VISIBLE);
                holder.mutual.setOnClickListener(clickMutual);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final UserObject data = (UserObject) getItem(position);
            iconfromNetwork(holder.icon, data.avatar);
            holder.name.setText(data.name);

            boolean isPicked = pickedData.contains(data);
            int drawableId = isPicked ? R.mipmap.member_list_item_picked : R.mipmap.member_list_item_no_pick;
            holder.mutual.setButtonDrawable(drawableId);
            holder.mutual.setChecked(isPicked);
            holder.mutual.setTag(R.id.followed, data);

            return convertView;
        }
    };

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
    }
}
