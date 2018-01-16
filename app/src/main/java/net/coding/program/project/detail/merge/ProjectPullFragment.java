package net.coding.program.project.detail.merge;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import net.coding.program.R;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_project_merge)
public class ProjectPullFragment extends BaseFragment {

    @ViewById
    protected ViewPager viewPager;
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    RadioGroup checkGroup;
    private PullPagerAdapter mAdapter;

    @AfterViews
    protected final void initProjectMergeFragment() {
        mAdapter = new PullPagerAdapter(getChildFragmentManager(), mProjectObject);
        viewPager.setAdapter(mAdapter);

        ((RadioGroup) getView().findViewById(R.id.checkGroup))
                .setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.checkClose) {
                        mAdapter.setState(1);
                    } else {
                        mAdapter.setState(0);
                    }
                    mAdapter.notifyDataSetChanged();
                });
    }

    private static class PullPagerAdapter extends FragmentStatePagerAdapter {

        private ProjectObject mProjectObject;
        private int mStatus = 0;

        public PullPagerAdapter(FragmentManager fm, ProjectObject projectObject) {
            super(fm);
            mProjectObject = projectObject;
        }

        public void setState(int status) {
            if (mStatus == status) {
                return;
            }

            mStatus = status;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            return MergeListFragment_
                    .builder()
                    .mProjectObject(mProjectObject)
                    .mType(mStatus)
                    .build();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

}
