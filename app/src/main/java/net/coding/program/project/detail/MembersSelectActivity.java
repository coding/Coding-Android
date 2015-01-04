package net.coding.program.project.detail;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

@EActivity(R.layout.activity_members_select)
public class MembersSelectActivity extends BaseFragmentActivity {

    @Extra
    ProjectObject mProjectObject;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new MembersListFragment_
                        .FragmentBuilder_()
                        .mProjectObject(mProjectObject)
                        .mSelect(true)
                        .build())
                .commit();
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }
}
