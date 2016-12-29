package net.coding.program;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.roughike.bottombar.OnTabSelectListener;

import net.coding.program.common.ui.GlobalUnit;
import net.coding.program.message.UsersListFragment_;
import net.coding.program.project.ProjectFragment_;
import net.coding.program.setting.MainSettingFragment_;
import net.coding.program.task.TaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.util.List;

@EActivity(R.layout.activity_main_parent)
public class EnterpriseMainActivity extends MainActivity {

    @AfterViews
    void initEnterpriseMainActivity() {
    }

    @Override
    protected OnTabSelectListener getBottomBarListener() {
        return tabId -> {
            int[] tabs = new int[]{
                    R.id.tabProject,
                    R.id.tabTask,
                    R.id.tabMessage,
                    R.id.tabMy
            };

            for (int i = 0; i < tabs.length; ++i) {
                if (tabs[i] == tabId) {
                    onNavigationDrawerItemSelected(i);
                }
            }
        };
    }

    // todo 切换保存状态
    public void onNavigationDrawerItemSelected(int position) {
        mSelectPos = position;
        Fragment fragment = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            actionBarCompShadow.setVisibility(View.VISIBLE);
        } else {
            ViewCompat.setElevation(appbar, GlobalUnit.ACTIONBAR_SHADOW);
        }

        taskOper(position);
        updateNotifyFromService();
        switch (position) {
            case 0://防止重复加载数据
//                fragment = new ProjectFragment_();
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                boolean containFragment = false;
                if (fragments != null) {
                    for (Fragment item : fragments) {
                        if (item instanceof ProjectFragment_) {
                            containFragment = true;
                            break;
                        }
                    }
                }

                if (!containFragment) {
                    fragment = new ProjectFragment_();
                }
                toolbarProjectTitle.setText("全部项目");
                break;
            case 1:
//                bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(20);
                fragment = new TaskFragment_();
                break;
            case 2:
//                bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(300);
                fragment = new UsersListFragment_();
                break;

            case 3:
//                bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(0);
                fragment = new MainSettingFragment_();
                break;
        }

        if (position == 0 || position == 1) {
            if (position == 0) {
                toolbarProjectTitle.setText("我的项目");
            }
            if (position == 1) {
                toolbarProjectTitle.setText("我的任务");
            }
            toolbarProjectTitle.setTag(position);
            visibleTitle(toolbarProjectTitle);
        } else {
            toolbarTitle.setVisibility(View.VISIBLE);
            visibleTitle(toolbarTitle);
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }
}
