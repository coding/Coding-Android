package net.coding.program.common;

import android.content.Context;
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
                UsersListActivity_
                        .intent(mContext)
                        .type(UsersListActivity.Friend.Follow)
                        .select(true)
                        .startForResult(mResult);
            } else {
                MembersSelectActivity_
                        .intent(mContext)
                        .mProjectObject(mProjectObject)
                        .startForResult(mResult);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}
