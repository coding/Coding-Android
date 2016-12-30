package net.coding.program.project;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.event.EventFilter;
import net.coding.program.event.EventPosition;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EFragment(R.layout.fragment_main_project)
public class MainProjectFragment extends BaseFragment {

    @ViewById
    TextView toolbarTitle;

    @AfterViews
    void initMainProjectFragment() {
        setToolbar("我的项目");

        Fragment fragment = new ProjectFragment_();
        getChildFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @Click
    void toolbarTitle(View v) {
        EventBus.getDefault().post(new EventFilter(0));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventPosition eventPosition) {
        toolbarTitle.setText(eventPosition.title);
    }
}
