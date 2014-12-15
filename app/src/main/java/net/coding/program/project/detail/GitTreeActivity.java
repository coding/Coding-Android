package net.coding.program.project.detail;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.model.GitFileInfoObject;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_project_git_tree)
public class GitTreeActivity extends FragmentActivity {

    @Extra
    ProjectObject mProjectObject;

    @Extra
    GitFileInfoObject mGitFileInfoObject;

    @AfterViews
    void init() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        String userId = getIntent().getStringExtra("id");

        ProjectGitFragment fragment = ProjectGitFragment_.builder().mGitFileInfoObject(mGitFileInfoObject).mProjectObject(mProjectObject).build();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, mGitFileInfoObject.name);
        ft.commit();
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
