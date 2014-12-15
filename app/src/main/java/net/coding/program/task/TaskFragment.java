package net.coding.program.task;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.ListModify;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.project.detail.TaskListFragment;
import net.coding.program.project.detail.TaskListFragment_;
import net.coding.program.third.MyPagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


@EFragment(R.layout.fragment_task)
public class TaskFragment extends BaseFragment implements TaskListParentUpdate {

    @ViewById
    MyPagerSlidingTabStrip tabs;

    @ViewById(R.id.pagerTaskFragment)
    ViewPager pager;

    private PageTaskFragment adapter;

    final String host = Global.HOST + "/api/projects?pageSize=100&type=all";
    final String urlTaskCount = Global.HOST + "/api/tasks/projects/count";

    ArrayList<ProjectObject> mData = new ArrayList<ProjectObject>();
    ArrayList<ProjectObject> mAllData = new ArrayList<ProjectObject>();

    @AfterViews
    void init() {
        showDialogLoading();
        tabs.setLayoutInflater(mInflater);
        getNetwork(host, host);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(host)) {
            hideProgressDialog();
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    ProjectObject projectObject = new ProjectObject(jsonArray.getJSONObject(i));
                    mAllData.add(projectObject);
                }
                getNetwork(urlTaskCount, urlTaskCount);
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(urlTaskCount)) {
            if (code == 0) {
                mData = new ArrayList<ProjectObject>();
                mData.add(new ProjectObject());

                JSONArray jsonArray = respanse.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    TaskCount taskCount = new TaskCount(jsonArray.getJSONObject(i));
                    for (int j = 0; j < mAllData.size(); ++j) {
                        ProjectObject project = mAllData.get(j);
                        if (taskCount.project.equals(project.id)) {
                            mData.add(project);
                        }
                    }
                }

                adapter = new PageTaskFragment(getChildFragmentManager());
                pager.setAdapter(adapter);

                int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                        .getDisplayMetrics());
                pager.setPageMargin(pageMargin);

                tabs.setViewPager(pager);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ListModify.RESULT_EDIT_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                taskListParentUpdate();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void taskListParentUpdate() {
        List<Fragment> array = getChildFragmentManager().getFragments();
        for (Fragment fragment : array) {
            if (fragment instanceof TaskListUpdate) {
                ((TaskListUpdate) fragment).taskListUpdate();
            }
        }
    }

    private class PageTaskFragment extends android.support.v4.app.FragmentPagerAdapter implements MyPagerSlidingTabStrip.IconTabProvider {

        public PageTaskFragment(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position).name;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TaskListFragment fragment = (TaskListFragment) super.instantiateItem(container, position);
            fragment.setParent(TaskFragment.this);

            Log.d("", "init p " + position);

            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public String getPageIconUrl(int position) {
            return mData.get(position).icon;
        }

        @Override
        public Fragment getItem(int position) {
            TaskListFragment_ fragment = new TaskListFragment_();
            Bundle bundle = new Bundle();
            bundle.putSerializable("mMembers", new TaskObject.Members(AccountInfo.loadAccount(getActivity())));
            bundle.putSerializable("mProjectObject", mData.get(position));
            bundle.putBoolean("mShowAdd", false);

            fragment.setArguments(bundle);

            return fragment;
        }
    }

    public static class TaskCount {
        public String project = "";
        public int processing;
        public int done;

        public TaskCount(JSONObject json) throws JSONException {
            project = json.getString("project");
            processing = json.getInt("processing");
            done = json.getInt("done");
        }
    }
}
