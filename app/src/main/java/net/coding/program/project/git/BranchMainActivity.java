package net.coding.program.project.git;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.project.detail.ProjectGitFragmentMain;
import net.coding.program.project.detail.ProjectGitFragmentMain_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_branch_main)
public class BranchMainActivity extends BackActivity {

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_branch_main);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_branch_main, menu);
//        return true;
//    }
    @Extra
    String mProjectPath;
    @Extra
    String mVersion;


    @AfterViews
    protected final void initBranchMainActivity() {
        String projectString = "/project/";
        int start = mProjectPath.indexOf(projectString) + projectString.length();
        String title = mProjectPath.substring(start);
        getSupportActionBar().setTitle(title);

        ProjectGitFragmentMain fragment = ProjectGitFragmentMain_.builder()
                .mProjectPath(mProjectPath)
                .mVersion(mVersion)
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}
