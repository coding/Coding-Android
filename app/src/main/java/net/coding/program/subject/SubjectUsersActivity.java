package net.coding.program.subject;

import android.support.v4.app.Fragment;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by david on 15-7-20.
 * 话题墙
 */
@EActivity(R.layout.activity_subject_detail)
public class SubjectUsersActivity extends BackActivity {

    @Extra
    int topicId;


    @AfterViews
    protected final void initSubjectUserActivity() {

        setTitle("全部参与者");
        if (topicId > 0) {
            showSubjectDetailFragment();
        }
    }


    private void showSubjectDetailFragment() {
        Fragment fragment = SubjectUserFragment_.builder()
                .topicId(topicId)
                .build();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }
}
