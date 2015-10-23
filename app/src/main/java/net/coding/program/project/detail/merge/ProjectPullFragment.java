package net.coding.program.project.detail.merge;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.ProjectObject;

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
    private ProjectMergeFragment.MergePagerAdapter mAdapter;

    @AfterViews
    protected final void initProjectMergeFragment() {
        String title = ProjectObject.getTitle(mProjectObject.isPublic());
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle(title);
        mAdapter = new ProjectMergeFragment.MergePagerAdapter(getChildFragmentManager(), mProjectObject);
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

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int radios[] = new int[]{
                        R.id.checkOpen,
                        R.id.checkClose
                };
                checkGroup.check(radios[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
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

}