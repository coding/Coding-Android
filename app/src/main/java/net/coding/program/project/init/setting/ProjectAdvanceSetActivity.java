package net.coding.program.project.init.setting;

import net.coding.program.R;

import org.androidannotations.annotations.EActivity;

/**
 * Created by jack wang on 2015/3/31.
 * 删除项目
 */
@EActivity(R.layout.init_activity_project_advance_set)
public class ProjectAdvanceSetActivity extends ProjectAdvanceSetBaseActivity {


     void actionDelete2FA(String code) {
        showProgressBar(true);
        deleteNetwork(mProjectObject.getHttpDeleteProject2fa(code), TAG_DELETE_PROJECT_2FA);
    }
}
