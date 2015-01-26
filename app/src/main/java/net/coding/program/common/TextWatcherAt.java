package net.coding.program.common;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.MembersSelectActivity_;
import net.coding.program.user.UsersListActivity;
import net.coding.program.user.UsersListActivity_;

/**
 * Created by chaochen on 14-10-29.
 */
public class TextWatcherAt implements TextWatcher {

    Context mContext;
    StartActivity mStartActivity;
    int mResult;

    ProjectObject mProjectObject;

    public TextWatcherAt(Context ctx, StartActivity startActivity, int activityResult) {
        this(ctx, startActivity, activityResult, null);
    }

    public TextWatcherAt(Context mContext, StartActivity mStartActivity, int mResult, ProjectObject mProjectObject) {
        this.mContext = mContext;
        this.mStartActivity = mStartActivity;
        this.mResult = mResult;
        this.mProjectObject = mProjectObject;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newEnter = s.toString().substring(start, start + count);
        if (newEnter.equals("@")) {
            if (mProjectObject == null) {
                startUserFollowList(mContext, mStartActivity, mResult);
            } else {
                Intent intent;
                intent = new Intent(mContext, MembersSelectActivity_.class);
                intent.putExtra("mProjectObject", mProjectObject);
                mStartActivity.startActivityForResult(intent, mResult);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public static void startUserFollowList(Context mContext, StartActivity mStartActivity, int mResult) {
        Intent intent;
        intent = new Intent(mContext, UsersListActivity_.class);
        intent.putExtra("type", UsersListActivity.Friend.Follow);
        intent.putExtra("select", true);
        mStartActivity.startActivityForResult(intent, mResult);
    }
}
