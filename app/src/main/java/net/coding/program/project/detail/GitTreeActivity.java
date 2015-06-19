package net.coding.program.project.detail;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.umeng.UmengActivity;
import net.coding.program.model.GitFileInfoObject;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_project_git_tree)
public class GitTreeActivity extends UmengActivity {

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
}
