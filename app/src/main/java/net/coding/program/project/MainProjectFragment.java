package net.coding.program.project;

import android.support.v4.app.Fragment;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_main_project)
public class MainProjectFragment extends BaseFragment {


    @AfterViews
    void initMainProjectFragment() {
        setToolbar("我的项目");

        Fragment fragment = new ProjectFragment_();
        getChildFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
