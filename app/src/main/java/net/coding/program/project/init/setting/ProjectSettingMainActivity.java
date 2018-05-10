package net.coding.program.project.init.setting;

import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.project.EventProjectModify;
import net.coding.program.project.detail.ProjectActivity_;
import net.coding.program.project.detail.ProjectFunction;
import net.coding.program.project.init.setting.v2.EnterpriseProjectSetActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.activity_project_setting_main)
public class ProjectSettingMainActivity extends BackActivity {

    @Extra
    ProjectObject projectObject;

    @ViewById
    TextView memberList;

    @AfterViews
    void initProjectSettingMainActivity() {
        memberList.setText("成员管理");
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProjectModify(EventProjectModify event) {
        finish();
    }

    @Click
    void maopaoList() {
        CodingCompat.instance().launchProjectMaopao(this, projectObject);
    }

    @Click
    void memberList() {
        ProjectActivity_.intent(this)
                .mProjectObject(projectObject)
                .mJumpType(ProjectFunction.member)
                .start();
    }

    @Click
    void projectSetting() {
        if (GlobalData.isEnterprise()) {
            EnterpriseProjectSetActivity_.intent(this).project(projectObject).start();
        } else {
            ProjectSetActivity_.intent(this).projectObject(projectObject).start();
        }
    }

}
