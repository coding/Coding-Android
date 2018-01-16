package net.coding.program.project.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.event.EventExitCode;
import net.coding.program.common.model.GitFileInfoObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.CodingToolbarBackActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_project_git_tree)
public class GitTreeActivity extends CodingToolbarBackActivity {

    @Extra
    String mProjectPath;
//    ProjectObject mProjectObject;

    @Extra
    GitFileInfoObject mGitFileInfoObject;

    @Extra
    String mVersion = ProjectGitFragment.MASTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (savedInstanceState != null) {
            mVersion = savedInstanceState.getString("mVersion", ProjectGitFragment.MASTER);
        } else {
            String userId = getIntent().getStringExtra("id");

            ProjectGitFragment fragment = ProjectGitFragment_.builder().mGitFileInfoObject(mGitFileInfoObject).mProjectPath(mProjectPath).mVersion(mVersion).build();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, fragment, mGitFileInfoObject.name);
            ft.commit();
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mVersion", mVersion);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVersion = savedInstanceState.getString("mVersion", ProjectGitFragment.MASTER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventExitCode(EventExitCode notify) {
        finish();
    }
}
