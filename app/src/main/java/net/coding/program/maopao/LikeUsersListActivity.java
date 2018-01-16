package net.coding.program.maopao;

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
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.DynamicObject;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.network.constant.VIP;
import net.coding.program.user.UserDetailActivity;

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

    public final String HOST_LIKES_USER = getHostLikesUser();
    public String UriLikeUsers = HOST_LIKES_USER;
    @Extra
    int id;
    @ViewById
    ListView listView;
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
                holder.vip = (ImageView) convertView.findViewById(R.id.vip);
                holder.mutual = (CheckBox) convertView.findViewById(R.id.followMutual);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DynamicObject.User data = mData.get(position);

            holder.name.setText(data.name);
            holder.icon.setTag(LikeUserImage.TAG, data);
            iconfromNetwork(holder.icon, data.avatar);

            if (data.vip == VIP.diamond) {
                holder.vip.setVisibility(View.VISIBLE);
                holder.vip.setImageResource(R.drawable.member_diamond);
            } else if (data.vip == VIP.gold) {
                holder.vip.setVisibility(View.VISIBLE);
                holder.vip.setImageResource(R.drawable.member_gold);
            } else {
                holder.vip.setVisibility(View.INVISIBLE);
            }

            if (GlobalData.sUserObject.global_key.equals(data.global_key)) {
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
                            postNetwork(UserDetailActivity.getHostFollow(), params, UserDetailActivity.getHostFollow(), position, null);
                        } else {
                            postNetwork(UserDetailActivity.getHostUnfollow(), params, UserDetailActivity.getHostUnfollow(), position, null);
                        }
                    }
                });
            }

            return convertView;
        }
    };

    public static String getHostLikesUser() {
        return Global.HOST_API + "/tweet/%s/allLikesAndRewards?pageSize=5000";
    }

    @AfterViews
    protected final void initLikeUsersListActivity() {
        UriLikeUsers = String.format(UriLikeUsers, id);

        listView.setAdapter(baseAdapter);
        listView.setOnItemClickListener((parent, view, position, id1) -> {
            CodingCompat.instance().launchUserDetailActivity(LikeUsersListActivity.this,
                    mData.get((int) id1).global_key);
        });

        getNetwork(UriLikeUsers, UriLikeUsers);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(UriLikeUsers)) {
            if (code == 0) {
                JSONObject jsonData = respanse.getJSONObject("data");
                if (jsonData.has("list")) {
                    JSONArray jsonArray = jsonData.getJSONArray("list");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        DynamicObject.User user = new DynamicObject.User(jsonArray.getJSONObject(i));
                        mData.add(user);
                    }
                } else {
                    parseUser(jsonData, "rewardUsers", Maopao.Like_user.Type.Reward);
                    parseUser(jsonData, "likeUsers", Maopao.Like_user.Type.Like);
                }
                baseAdapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void parseUser(JSONObject jsonData, String KEY_REWARD, Maopao.Like_user.Type type) throws JSONException {
        if (jsonData.has(KEY_REWARD)) {
            JSONArray rewards = jsonData.optJSONArray(KEY_REWARD);
            for (int i = 0; i < rewards.length(); ++i) {
                Maopao.Like_user user = new Maopao.Like_user(rewards.optJSONObject(i));
                user.setType(type);
                mData.add(user);
            }
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
        ImageView vip;
    }
}
