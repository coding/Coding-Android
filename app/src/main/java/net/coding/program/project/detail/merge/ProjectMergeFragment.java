package net.coding.program.project.detail.merge;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_project_merge)
public class ProjectMergeFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    protected ViewPager viewPager;

    @AfterViews
    protected final void initProjectMergeFragment() {
        MergePagerAdapter adapter = new MergePagerAdapter(getChildFragmentManager(), mProjectObject);
        viewPager.setAdapter(adapter);

        ((RadioGroup) getView().findViewById(R.id.checkGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.checkClose) {
                    viewPager.setCurrentItem(1);
                } else {
                    viewPager.setCurrentItem(0);
                }
            }
        });
    }

    private static class MergePagerAdapter extends FragmentPagerAdapter {

        private ProjectObject mProjectObject;

        public MergePagerAdapter(FragmentManager fm, ProjectObject projectObject) {
            super(fm);
            mProjectObject = projectObject;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = MergeListFragment_.builder().mProjectObject(mProjectObject).mType(position).build();
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
