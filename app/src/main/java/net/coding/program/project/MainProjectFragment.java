package net.coding.program.project;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.event.EventFilter;
import net.coding.program.common.event.EventPosition;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.login.auth.QRScanActivity;
import net.coding.program.maopao.MaopaoAddActivity_;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.search.SearchProjectActivity_;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.user.AddFollowActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EFragment(R.layout.fragment_main_project)
public class MainProjectFragment extends BaseFragment {

    @ViewById
    TextView toolbarTitle;

    @ViewById
    Toolbar mainProjectToolbar;

    @AfterViews
    void initMainProjectFragment() {
        toolbarTitle.setText("我的项目");
        mainProjectToolbar.inflateMenu(R.menu.menu_fragment_project);
        mainProjectToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_create_friend:
                    action_create_friend();
                    break;
                case R.id.action_create:
                    action_create();
                    break;
                case R.id.action_create_task:
                    action_create_task();
                    break;
                case R.id.action_create_maopao:
                    action_create_maopao();
                    break;
                case R.id.action_scan:
                    action_scan();
                    break;
                case R.id.action_2fa:
                    action_2fa();
                    break;
                case R.id.action_search:
                    action_search();
                    break;
            }
            return true;
        });

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


    @OptionsItem
    void action_create_friend() {
        umengEvent(UmengEvent.LOCAL, "快捷添加好友");
        AddFollowActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_create() {
        umengEvent(UmengEvent.LOCAL, "快捷创建项目");
        ProjectCreateActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_create_task() {
        umengEvent(UmengEvent.LOCAL, "快捷创建任务");
        TaskAddActivity_.intent(this).mUserOwner(GlobalData.sUserObject).start();
    }

    @OptionsItem
    final void action_create_maopao() {
        umengEvent(UmengEvent.LOCAL, "快捷创建冒泡");
        MaopaoAddActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_scan() {
        if (!PermissionUtil.checkCamera(getActivity())) {
            return;
        }

        Intent intent = new Intent(getActivity(), QRScanActivity.class);
        intent.putExtra(QRScanActivity.EXTRA_OPEN_AUTH_LIST, false);
        startActivity(intent);
    }

    @OptionsItem
    final void action_2fa() {
        if (!PermissionUtil.checkCamera(getActivity())) {
            return;
        }

        GlobalCommon.start2FAActivity(getActivity());
    }

    @OptionsItem
    void action_search() {
        SearchProjectActivity_.intent(this).start();
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }


}
