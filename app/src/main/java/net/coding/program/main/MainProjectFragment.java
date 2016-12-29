package net.coding.program.main;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import net.coding.program.R;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.project.ProjectFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_main_project)
public class MainProjectFragment extends BaseFragment {

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initMainProjectFragment() {
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);

        Fragment fragment = new ProjectFragment_();
        getChildFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
