package net.coding.program.project.git;

import android.support.annotation.Nullable;

import net.coding.program.R;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.CodingToolbarBackActivity;
import net.coding.program.project.detail.ProjectGitFragmentMain;
import net.coding.program.project.detail.ProjectGitFragmentMain_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_branch_main)
public class BranchMainActivity extends CodingToolbarBackActivity {

    @Extra
    String mProjectPath;
    @Extra
    String mVersion;

    @AfterViews
    protected final void initBranchMainActivity() {
        String projectString = "/project/";
        int start = mProjectPath.indexOf(projectString) + projectString.length();
        String title = mProjectPath.substring(start);
        setActionBarTitle(title);

        ProjectGitFragmentMain fragment = ProjectGitFragmentMain_.builder()
                .mProjectPath(mProjectPath)
                .mVersion(mVersion)
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Nullable
    @Override
    protected ProjectObject getProject() {
        return null;
    }

    @Override
    protected String getProjectPath() {
        return mProjectPath;
    }
}
