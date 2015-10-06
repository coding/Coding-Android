package net.coding.program.maopao;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.DynamicObject;
import net.coding.program.user.UserDetailActivity;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_like_users_list)
public class LikeUsersListActivity extends BackActivity {

    @Extra
    int id;

    @ViewById
    ListView listView;

    public static final String HOST_LIKES_USER = Global.HOST_API + "/tweet/%s/likes?pageSize=500";
    public String UriLikeUsers = Global.HOST_API + "/tweet/%s/likes?pageSize=500";

    private ArrayList<DynamicObject.User> mData = new ArrayList<>();
    BaseAdapter baseAdapter = new BaseAdapter() {
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
                convertView = mInflater.inflate(R.layout.activity_like_users_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followMutual);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DynamicObject.User data = mData.get(position);

            holder.name.setText(data.name);
            iconfromNetwork(holder.icon, data.avatar);

            if (MyApp.sUserObject.global_key.equals(data.global_key)) {
                holder.mutual.setVisibility(View.INVISIBLE);

            } else {
                holder.mutual.setVisibility(View.VISIBLE);

                int drawableId = data.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
                holder.mutual.setButtonDrawable(drawableId);
                holder.mutual.setChecked(data.followed);

                holder.mutual.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RequestParams params = new RequestParams();
                        params.put("users", data.global_key);
                        if (((CheckBox) v).isChecked()) {
                            postNetwork(UserDetailActivity.HOST_FOLLOW, params, UserDetailActivity.HOST_FOLLOW, position, null);
                        } else {
                            postNetwork(UserDetailActivity.HOST_UNFOLLOW, params, UserDetailActivity.HOST_UNFOLLOW, position, null);
                        }
                    }
                });
            }

            return convertView;
        }
    };

    @AfterViews
    protected final void initLikeUsersListActivity() {
        UriLikeUsers = String.format(UriLikeUsers, id);

        listView.setAdapter(baseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(LikeUsersListActivity.this, UserDetailActivity_.class);
                intent.putExtra("globalKey", mData.get((int) id).global_key);
                startActivity(intent);
            }
        });

        getNetwork(UriLikeUsers, UriLikeUsers);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(UriLikeUsers)) {
            if (code == 0) {
                JSONObject jsonData = respanse.getJSONObject("data");
                JSONArray jsonArray = jsonData.getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    DynamicObject.User user = new DynamicObject.User(jsonArray.getJSONObject(i));
                    mData.add(user);
                }
                baseAdapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
    }
}
