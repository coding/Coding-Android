package net.coding.program.task.add;

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
 * 任务关注者列表界面
 */
@EActivity(R.layout.activity_members)
public class WatcherListActivity extends MembersActivity {

    @Extra
    int mTaskId = 0; // 任务的 id 号，0 表示新建任务

    @Extra
    ArrayList<UserObject> mWatchUsers = new ArrayList<>(); // 任务的关注者

    @AfterViews
    void initWatcherListActivity() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Member members = mMembersArray.get(position);
            for (int i = 0; i < mWatchUsers.size(); ++i) {
                UserObject item = mWatchUsers.get(i);
                if (members.user.id == item.id) {
                    UserObject deleteUser = mWatchUsers.remove(i);
                    removeWatchUser(deleteUser);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }

            addWatchUser(members.user);
            mWatchUsers.add(members.user);
            adapter.notifyDataSetChanged();
        });
    }

    private void removeWatchUser(UserObject user) {
        if (mTaskId == 0) {
            return;
        }

        String url = String.format(Global.HOST_API + "/task/%d/user/%s/watch", mTaskId, user.global_key);
        MyAsyncHttpClient.delete(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                mWatchUsers.add(user);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addWatchUser(UserObject user) {
        if (mTaskId == 0) {
            return;
        }

        String url = String.format(Global.HOST_API + "/task/%d/user/%s/watch", mTaskId, user.global_key);
        MyAsyncHttpClient.post(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                mWatchUsers.remove(user);
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void updateChecked(ViewHolder holder, Member data) {
        for (UserObject item : mWatchUsers) {
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
        intent.putExtra("resultData", mWatchUsers);
        setResult(Activity.RESULT_OK, intent);

        super.onBackPressed();
    }

}
