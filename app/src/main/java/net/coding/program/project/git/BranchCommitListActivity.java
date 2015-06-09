package net.coding.program.project.git;

import net.coding.program.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;

@EActivity(R.layout.fragment_project_dynamic)
@OptionsMenu(R.menu.menu_branch_commit_list)
public class BranchCommitListActivity extends BackActivity {

    private static final String HOST_COMMITS = "HOST_COMMITS";

    @Extra
    String mCommitsUrl;

}
