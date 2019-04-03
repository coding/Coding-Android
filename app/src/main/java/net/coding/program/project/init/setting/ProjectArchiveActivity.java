package net.coding.program.project.init.setting;

import net.coding.program.R;

import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.init_activity_project_archive)
public class ProjectArchiveActivity extends ProjectAdvanceSetBaseActivity {

    void actionDelete2FA(String code) {
        showProgressBar(true);
        postNetwork(mProjectObject.getHttpArchiveProject2fa(code), TAG_ARCHIVE_PROJECT);
    }
}
