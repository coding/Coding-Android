package net.coding.program.project.git.local;

import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.event.EventDownloadProgress;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_git_main)
public class GitMainActivity extends BackActivity {

    @Extra
    ProjectObject project;

    @ViewById
    TextView progressText;

    @AfterViews
    void initGitMainActivity() {
        cloneButton();
    }

    @Click
    void cloneButton() {
        CloneCodeService.Param param = new CloneCodeService.Param(project, GlobalData.sUserObject.global_key, "123456");
        CloneCodeService.startActionGit(this, param);
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventDownloadProgress event) {
        progressText.setText(event.progress);
    }

}
