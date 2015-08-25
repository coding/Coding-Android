package net.coding.program.project.detail.merge;


import android.app.Activity;
import android.content.Intent;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;

@EFragment(R.layout.fragment_comment_edit)
public class CommentEditFragment extends TopicEditFragment implements StartActivity {

    final int RESULT_REQUEST_AT = 1;

    @FragmentArg
    String mMergeUrl;
    private TextWatcherAt watcher;

    public boolean isEmpty() {
        return Global.isEmptyContainSpace(title);
    }

    // TODO 会调用多次，因为在观察者模式里面注册了多个，以后再看吧
    @AfterViews
    protected final void initCommentEditFragment() {
        if (watcher == null) {
            watcher = new TextWatcherAt(getActivity(), this, RESULT_REQUEST_AT, mMergeUrl);
        }
        edit.removeTextChangedListener(watcher);
        edit.addTextChangedListener(watcher);
    }

    @OnActivityResult(RESULT_REQUEST_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            EnterLayout.insertText(edit, name);
        }
    }
}
