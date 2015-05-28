package net.coding.program.project.git;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.model.Merge;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;

@EActivity(R.layout.activity_commit_list)
@OptionsMenu(R.menu.menu_commit_list)
public class CommitListActivity extends BackActivity {

    @Extra
    Merge mMerge;

    @AfterViews
    protected final void initCommitListActivity() {
        getSupportActionBar().setTitle(mMerge.getTitle());
    }
}
