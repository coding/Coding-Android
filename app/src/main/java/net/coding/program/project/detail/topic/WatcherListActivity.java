package net.coding.program.project.detail.topic;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.network.model.user.Member;
import net.coding.program.project.detail.MembersActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenchao on 16/7/26.
 * 讨论关注者列表界面
 */

@EActivity(R.layout.activity_members)
public class WatcherListActivity extends MembersActivity {

    @Extra
    ArrayList<UserObject> watchers = new ArrayList<>();

    @Extra
    int topicId;

    @AfterViews
    void initWatcherListActivity() {
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            Member members = mMembersArray.get(position);
            for (int i = 0; i < watchers.size(); ++i) {
                UserObject item = watchers.get(i);
                if (item.id == members.user.id) {
                    watchers.remove(item);
                    removeWatchUser(item);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }

            addWatchUser(members.user);
            watchers.add(members.user);
            adapter.notifyDataSetChanged();
        }));
    }

    private void removeWatchUser(UserObject user) {
        String url = String.format(Global.HOST_API + "/topic/%s/user/%s/watch", topicId, user.global_key);
        MyAsyncHttpClient.delete(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                watchers.add(user);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addWatchUser(UserObject user) {
        String url = String.format(Global.HOST_API + "/topic/%s/user/%s/watch", topicId, user.global_key);
        MyAsyncHttpClient.post(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                watchers.remove(user);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void updateChecked(ViewHolder holder, Member data) {
        for (UserObject item : watchers) {
            if (data.user.id == item.id) {
                holder.watchCheck.setVisibility(View.VISIBLE);
                return;
            }
        }

        holder.watchCheck.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("resultData", watchers);
        setResult(Activity.RESULT_OK, intent);

        super.onBackPressed();
    }
}
