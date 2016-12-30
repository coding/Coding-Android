package net.coding.program.task;


import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.event.EventFilter;
import net.coding.program.event.EventPosition;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EFragment(R.layout.fragment_main_task)
public class MainTaskFragment extends TaskFragment {

    @ViewById
    TextView toolbarTitle;

    @AfterViews
    void initMainTaskFragment() {
        setToolbar("我的任务");
    }

    @Click
    void toolbarTitle(View v) {
        EventBus.getDefault().post(new EventFilter(1));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventPosition eventPosition) {
        toolbarTitle.setText(eventPosition.title);
    }
}
