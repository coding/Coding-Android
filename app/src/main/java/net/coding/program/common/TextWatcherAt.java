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
    String mMergeUrl;

    public TextWatcherAt(Context ctx, StartActivity startActivity, int activityResult) {
        this(ctx, startActivity, activityResult, null, null);
    }

    public TextWatcherAt(Context ctx, StartActivity startActivity, int activityResult, String mergeUrl) {
        this(ctx, startActivity, activityResult, null, mergeUrl);
    }

    public TextWatcherAt(Context mContext, StartActivity mStartActivity, int mResult, ProjectObject mProjectObject) {
        this.mContext = mContext;
        this.mStartActivity = mStartActivity;
        this.mResult = mResult;
        this.mProjectObject = mProjectObject;
    }

    private TextWatcherAt(Context mContext, StartActivity mStartActivity, int mResult, ProjectObject mProjectObject, String mMergeUrl) {
        this.mContext = mContext;
        this.mStartActivity = mStartActivity;
        this.mResult = mResult;
        this.mProjectObject = mProjectObject;
        this.mMergeUrl = mMergeUrl;
    }

    public static void startActivityAt(Context context, StartActivity startActivity, int result) {
        Intent intent;
        intent = new Intent(context, UsersListActivity_.class);
        intent.putExtra("type", UsersListActivity.Friend.Follow);
        intent.putExtra("select", true);
        intent.putExtra("hideFollowButton", true);
        startActivity.startActivityForResult(intent, result);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newEnter = s.toString().substring(start, start + count);
        if (newEnter.equals("@")) {
            if (mMergeUrl != null && !mMergeUrl.isEmpty()) {
                Intent intent;
                intent = new Intent(mContext, MembersSelectActivity_.class);
                intent.putExtra("mMergeUrl", mMergeUrl);
                mStartActivity.startActivityForResult(intent, mResult);

            } else if (mProjectObject == null) {
                startActivityAt(mContext, mStartActivity, mResult);
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

}
