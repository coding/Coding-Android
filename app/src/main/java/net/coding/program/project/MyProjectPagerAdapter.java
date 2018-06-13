package net.coding.program.project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import net.coding.program.common.model.ProjectObject;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/8/1.
 */
class MyProjectPagerAdapter extends FragmentStatePagerAdapter {

    private ProjectFragment projectFragment;

    public MyProjectPagerAdapter(ProjectFragment projectFragment, FragmentManager fm) {
        super(fm);
        this.projectFragment = projectFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return projectFragment.program_title[position];
    }

    @Override
    public int getCount() {
        return projectFragment.program_title.length + 1;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == projectFragment.program_title.length) {
            return new MenuProjectFragment_();
        }

        ProjectListFragment fragment = new ProjectListFragment_();
        fragment.setPos(position);
        fragment.setTitle(projectFragment.program_title[position]);
        Bundle bundle = new Bundle();

        bundle.putSerializable("mData", getChildData(position));
        bundle.putSerializable("type", projectFragment.type);
        fragment.setArguments(bundle);

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
                if (projectFragment.type == ProjectFragment.Type.Pick) {
                    stuffPrivateProjectChildData(childData);
                } else {
                    childData.addAll(projectFragment.mData);
                }
                break;
        }

        return childData;
    }

    private void stuffChildData(ArrayList<ProjectObject> child, String type) {
        for (int i = 0; i < projectFragment.mData.size(); ++i) {
            ProjectObject item = projectFragment.mData.get(i);
            if (item.current_user_role.equals(type)) {
                child.add(item);
            }
        }
    }

    private void stuffPrivateProjectChildData(ArrayList<ProjectObject> child) {
        for (int i = 0; i < projectFragment.mData.size(); ++i) {
            ProjectObject item = projectFragment.mData.get(i);
            if (!item.isPublic()) {
                child.add(item);
            }
        }
    }
}
