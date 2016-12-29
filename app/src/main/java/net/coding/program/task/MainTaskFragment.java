package net.coding.program.task;


import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ui.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_main_task)
public class MainTaskFragment extends TaskFragment {

    @ViewById
    TextView toolbarTitle;

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initMainTaskFragment() {
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.setActionBarTitle("");
        toolbarTitle.setText("我的任务");

    }

}
