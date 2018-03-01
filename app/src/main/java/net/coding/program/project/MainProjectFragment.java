package net.coding.program.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import net.coding.program.terminal.TerminalActivity;
import net.coding.program.user.AddFollowActivity_;

import org.androidannotations.api.builder.FragmentBuilder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainProjectFragment extends BaseFragment {

    TextView toolbarTitle;
    Toolbar mainProjectToolbar;
    View hasViews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        hasViews = inflater.inflate(R.layout.fragment_main_project, container, false);
        return hasViews;
    }

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.toolbarTitle = hasViews.findViewById(R.id.toolbarTitle);
        this.mainProjectToolbar = hasViews.findViewById(R.id.mainProjectToolbar);
        View view_terminalClick = hasViews.findViewById(R.id.terminalClick);

        if (view_terminalClick != null) {
            view_terminalClick.setOnClickListener(new View.OnClickListener() {

                                                      @Override
                                                      public void onClick(View view) {
                                                          terminalClick();
                                                      }
                                                  }
            );
        }
        if (this.toolbarTitle != null) {
            this.toolbarTitle.setOnClickListener(new View.OnClickListener() {

                                                     @Override
                                                     public void onClick(View view) {
                                                         toolbarTitle(view);
                                                     }
                                                 }
            );
        }
        initMainProjectFragment();
    }


    void toolbarTitle(View v) {
        EventBus.getDefault().post(new EventFilter(0));
    }

    void terminalClick() {
        Intent i = new Intent(getActivity(), TerminalActivity.class);
        startActivity(i);
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


    void action_create_friend() {
        umengEvent(UmengEvent.LOCAL, "快捷添加好友");
        AddFollowActivity_.intent(this).start();
    }

    final void action_create() {
        umengEvent(UmengEvent.LOCAL, "快捷创建项目");
        ProjectCreateActivity_.intent(this).start();
    }

    final void action_create_task() {
        umengEvent(UmengEvent.LOCAL, "快捷创建任务");
        TaskAddActivity_.intent(this).mUserOwner(GlobalData.sUserObject).start();
    }

    final void action_create_maopao() {
        umengEvent(UmengEvent.LOCAL, "快捷创建冒泡");
        MaopaoAddActivity_.intent(this).start();
    }

    final void action_scan() {
        if (!PermissionUtil.checkCamera(getActivity())) {
            return;
        }

        Intent intent = new Intent(getActivity(), QRScanActivity.class);
        intent.putExtra(QRScanActivity.EXTRA_OPEN_AUTH_LIST, false);
        startActivity(intent);
    }

    final void action_2fa() {
        if (!PermissionUtil.checkCamera(getActivity())) {
            return;
        }

        GlobalCommon.start2FAActivity(getActivity());
    }

    void action_search() {
        SearchProjectActivity_.intent(this).start();
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    public static class FragmentBuilder_
            extends FragmentBuilder<MainProjectFragment.FragmentBuilder_, MainProjectFragment> {

        @Override
        public net.coding.program.project.MainProjectFragment build() {
            MainProjectFragment fragment_ = new MainProjectFragment();
            fragment_.setArguments(args);
            return fragment_;
        }
    }
}
