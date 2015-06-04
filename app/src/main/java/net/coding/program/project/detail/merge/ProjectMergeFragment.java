package net.coding.program.project.detail.merge;


import android.app.Activity;
import android.content.Intent;
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

    @ViewById
    protected ViewPager viewPager;
    @FragmentArg
    ProjectObject mProjectObject;
    private MergePagerAdapter mAdapter;

    @AfterViews
    protected final void initProjectMergeFragment() {
        mAdapter = new MergePagerAdapter(getChildFragmentManager(), mProjectObject);
        viewPager.setAdapter(mAdapter);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MergeListFragment.RESULT_CHANGE) {
            if (resultCode == Activity.RESULT_OK) {
                mAdapter.notifyDataSetChanged();
            }
        }
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
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
