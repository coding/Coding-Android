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
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.umeng.UmengEvent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

@EActivity(R.layout.activity_enterprise_add_member)
public class EnterpriseAddMemberActivity extends BaseEnterpriseUserListActivity {

    private static final String TAG_HOST_ADD_USER = "TAG_HOST_ADD_USER";
    private static final String TAG_HOST_DELETE_USER = "TAG_HOST_DELETE_USER";

    ArrayList<UserObject> pickedData = new ArrayList<>();

    @Extra
    ProjectObject projectObject;

    @Extra
    ArrayList<String> pickedGlobalKeys;

    @ViewById
    protected ListView listView;

    @AfterViews
    void initEnterpriseAddMemberActivity() {
        listView.setAdapter(adapter);
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

        Collections.sort(allListData, (o1, o2) -> {
            if (pickedData.contains(o1)) {
                if (pickedData.contains(o2)) {
                    return o1.compareTo(o2);
                } else {
                    return -1;
                }
            } else if (pickedData.contains(o2)) {
                return 1;
            } else {
                return o1.compareTo(o2);
            }
        });

        listData.clear();
        listData.addAll(allListData);

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void searchItem(String s) {
        super.searchItem(s);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_ADD_USER)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "添加成员");

                pickedData.add((UserObject) data);

                showMiddleToast(String.format("添加项目成员 %s 成功", ((UserObject) data).name));
            } else {
                showErrorMsg(code, respanse);
            }
            adapter.notifyDataSetChanged();

        } else if (tag.equals(TAG_HOST_DELETE_USER)) {
            if (code == 0) {
                pickedData.remove((UserObject) data);

                showMiddleToast(String.format("移除项目成员 %s 成功", ((UserObject) data).name));
            } else {
                showErrorMsg(code, respanse);
            }
            adapter.notifyDataSetChanged();

        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    private View.OnClickListener clickMutual = v -> {
        UserObject user = (UserObject) v.getTag(R.id.followed);
        if (((CheckBox) v).isChecked()) {
            final String urlAddUser = Global.HOST_API + "/project/" + projectObject.id + "/members/gk/add";
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
                holder.bottomDivideLine = convertView.findViewById(R.id.bottomDivideLine);
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
            int drawableId = isPicked ? R.drawable.checkbox_follow_follow : R.drawable.checkbox_follow_fans;
            holder.mutual.setButtonDrawable(drawableId);
            holder.mutual.setChecked(isPicked);
            holder.mutual.setTag(R.id.followed, data);

            holder.bottomDivideLine.setVisibility(position == (getCount() - 1) ? View.GONE : View.VISIBLE);

            return convertView;
        }
    };

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
        View bottomDivideLine;
    }
}
