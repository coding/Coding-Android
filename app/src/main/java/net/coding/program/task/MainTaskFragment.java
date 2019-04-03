package net.coding.program.task;


import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.event.EventFilter;
import net.coding.program.common.event.EventPosition;

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
    AppBarLayout appbarLayout;

    @ViewById
    View actionBarCompShadow;

    @ViewById
    TextView mainTaskToolbarTitle;

    @AfterViews
    void initMainTaskFragment() {
        setToolbar("我的任务", R.id.mainTaskToolbar);
    }

    @Click
    void mainTaskToolbarTitle(View v) {
        EventBus.getDefault().post(new EventFilter(1));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventPosition eventPosition) {
        mainTaskToolbarTitle.setText(eventPosition.title);
    }

    public void hideActionBarShadow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (actionBarCompShadow != null) {
                actionBarCompShadow.setVisibility(View.GONE);
            }
        } else {
            if (appbarLayout != null) {
                ViewCompat.setElevation(appbarLayout, 0);
            }
        }
    }
}
