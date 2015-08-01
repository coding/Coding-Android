package net.coding.program.project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.model.ProjectObject;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/8/1.
 */
class ProjectPagerAdapter extends SaveFragmentPagerAdapter {

    private ProjectFragment projectFragment;

    private ArrayList<ProjectObject> mData;

    public ProjectPagerAdapter(ProjectFragment projectFragment, FragmentManager fm, ArrayList<ProjectObject> data) {
        super(fm);
        this.projectFragment = projectFragment;
        this.mData = data;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return projectFragment.program_title[position];
    }

    @Override
    public int getCount() {
        return projectFragment.program_title.length;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ProjectListFragment fragment = (ProjectListFragment) super.instantiateItem(container, position);
        fragment.setData(getChildData(position), projectFragment.requestOk);

        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("", "all p " + position);
        ProjectListFragment fragment = new ProjectListFragment_();
        Bundle bundle = new Bundle();

        bundle.putSerializable("mData", getChildData(position));
        fragment.setArguments(bundle);

        saveFragment(fragment);

        return fragment;
    }

    private ArrayList<ProjectObject> getChildData(int position) {
        ArrayList<ProjectObject> childData = new ArrayList<>();

        switch (position) {
            case 1:
                stuffChildData(childData, "member");
                break;
            case 2:
                stuffChildData(childData, "owner");
                break;
            default:
                childData.addAll(mData);
                break;
        }

        return childData;
    }

    void stuffChildData(ArrayList<ProjectObject> child, String type) {
        for (int i = 0; i < mData.size(); ++i) {
            ProjectObject item = mData.get(i);
            if (item.current_user_role.equals(type)) {
                child.add(item);
            }
        }
    }
}
