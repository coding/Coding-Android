package net.coding.program.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;
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

@EActivity(R.layout.activity_add_follow)
public class AddFollowActivity extends BaseActivity {

    String HOST_SEARCH_USER = Global.HOST + "/api/user/search?key=%s";

    String urlAddUser = "";

    ArrayList<UserObject> mData = new ArrayList<UserObject>();

    boolean mNeedUpdate = false;

    @Extra
    ProjectObject mProjectObject;

    @ViewById
    ListView listView;

    @ViewById
    EditText name;

    int flag = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == flag) {
                getNetwork(String.format(HOST_SEARCH_USER, (String) msg.obj), HOST_SEARCH_USER);
            }
        }
    };

    BaseAdapter baseAdapter;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (mProjectObject == null) {
            baseAdapter = new FollowAdapter();
        } else {
            urlAddUser = String.format(Global.HOST + "/api/project/%s/members/add?", mProjectObject.id);
            getActionBar().setTitle("添加项目成员");
            baseAdapter = new AddProjectAdapter();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final int pos = (int) id;
                    final UserObject data = mData.get((int) id);

                    AlertDialog.Builder builder = new AlertDialog.Builder(AddFollowActivity.this);
                    AlertDialog dialog = builder.setMessage(String.format("添加项目成员 %s ?", data.name))
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RequestParams params = new RequestParams();
                                    params.put("users", data.id);
                                    postNetwork(urlAddUser, params, urlAddUser, (int) pos, data);
                                }
                            })
                            .setNegativeButton("取消", null).show();
                    dialogTitleLineColor(dialog);
                }
            });
        }
        listView.setAdapter(baseAdapter);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
                mNeedUpdate = true;
                showButtomToast("关注成功");
                mData.get(pos).followed = true;
            } else {
                showButtomToast("关注失败");
            }
            baseAdapter.notifyDataSetChanged();
        } else if (tag.equals(UsersListActivity.HOST_UNFOLLOW)) {
            if (code == 0) {
                mNeedUpdate = true;
                showButtomToast("取消关注成功");
                mData.get(pos).followed = false;
            } else {
                showButtomToast("取消关注失败");
            }
            baseAdapter.notifyDataSetChanged();
        } else if (tag.equals(urlAddUser)) {
            if (code == 0) {
                mNeedUpdate = true;
                showMiddleToast(String.format("添加项目成员 %s 成功", ((UserObject) data).name));
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    void search(String s) {
        int flagHandler = ++flag;
        Message message = Message.obtain(mHandler, flagHandler, s);
        mHandler.sendMessageDelayed(message, 1000);
    }

    class AddProjectAdapter extends BaseAdapter {
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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_add_follow_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followed);
                holder.mutual.setVisibility(View.INVISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final UserObject data = (UserObject) getItem(position);

            iconfromNetwork(holder.icon, data.avatar);
            holder.name.setText(String.format("%s - %s", data.name, data.global_key));

            return convertView;
        }
    }

    class FollowAdapter extends BaseAdapter {

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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_add_follow_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followed);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final UserObject data = (UserObject) getItem(position);

            iconfromNetwork(holder.icon, data.avatar);
            holder.name.setText(String.format("%s - %s", data.name, data.global_key));

            int drawableId = data.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
            holder.mutual.setButtonDrawable(drawableId);
            holder.mutual.setChecked(data.followed);

            holder.mutual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestParams params = new RequestParams();
                    params.put("users", data.global_key);
                    if (((CheckBox) v).isChecked()) {
                        postNetwork(UsersListActivity.HOST_FOLLOW, params, UsersListActivity.HOST_FOLLOW, position, null);
                    } else {
                        postNetwork(UsersListActivity.HOST_UNFOLLOW, params, UsersListActivity.HOST_UNFOLLOW, position, null);
                    }
                }
            });

            return convertView;
        }
    }

    ;

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        setResult(mNeedUpdate ? RESULT_OK : RESULT_CANCELED);
        super.onBackPressed();
    }
}
