package net.coding.program.project.detail.merge;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import net.coding.program.R;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_project_merge2)
public class ProjectMergeFragment extends BaseFragment {

    @ViewById
    protected ViewPager viewPager;
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    RadioGroup checkGroup;
    @ViewById
    WechatTab tabs;

    private MergePagerAdapter mAdapter;

    @AfterViews
    protected final void initProjectMergeFragment() {
        String title = ProjectObject.getTitle(mProjectObject.isPublic());
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle(title);
        mAdapter = new MergePagerAdapter(getChildFragmentManager(), mProjectObject);
        viewPager.setAdapter(mAdapter);

        ((RadioGroup) getView().findViewById(R.id.checkGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.checkClose) {
                    mAdapter.setState(1);
                } else {
                    mAdapter.setState(0);
                }

                mAdapter.notifyDataSetChanged();
            }
        });

        tabs.setViewPager(viewPager);
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
        private int mStatus = 0;

        ProjectObject.MergeExamine[] mine = new ProjectObject.MergeExamine[] {
                ProjectObject.MergeExamine.review,
                ProjectObject.MergeExamine.mine,
                ProjectObject.MergeExamine.other
        };

        String[] titles = new String[] {
                "我评审的",
                "我发布的",
                "其他的"
        };

        public MergePagerAdapter(FragmentManager fm, ProjectObject projectObject) {
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
                    .mMineType(mine[position])
                    .build();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mine.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
