package net.coding.program.project.detail;

import android.os.Bundle;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

@EActivity(R.layout.activity_user_dynamic)
public class UserDynamicActivity extends BaseFragmentActivity {

    @Extra
    TaskObject.Members mMember;

    @Extra
    ProjectObject mProjectObject;

    @AfterViews
    protected void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(mMember.user.name);

        ProjectDynamicFragment_ fragment = new ProjectDynamicFragment_();

        Bundle bundle = new Bundle();
        bundle.putString("mUser_id", mMember.user_id);
        bundle.putSerializable("mProjectObject", mProjectObject);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }
}
