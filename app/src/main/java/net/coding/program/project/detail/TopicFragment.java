package net.coding.program.project.detail;


import android.content.Intent;
import android.widget.FrameLayout;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_topic)
public class TopicFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    FrameLayout container;

    private TopicListFragment fragment;

    @AfterViews
    void init() {
        fragment = TopicListFragment_.builder().mProjectObject(mProjectObject).build();
        getChildFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
