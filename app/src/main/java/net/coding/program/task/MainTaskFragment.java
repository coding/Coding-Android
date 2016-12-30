package net.coding.program.task;


import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_main_task)
public class MainTaskFragment extends TaskFragment {

    @AfterViews
    void initMainTaskFragment() {
        setToolbar("我的任务");
    }

}
