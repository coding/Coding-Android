package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ListModify;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.task.TaskListParentUpdate;
import net.coding.program.task.TaskListUpdate;
import net.coding.program.third.MyPagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project_task)
public class ProjectTaskFragment extends BaseFragment implements TaskListParentUpdate {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    MyPagerSlidingTabStrip tabs;

    @ViewById(R.id.pagerProjectTask)
    ViewPager pager;

    @ViewById
    View blankLayout;

    private MyPagerAdapter adapter;

    @AfterViews
    void init() {
        showDialogLoading();
        tabs.setLayoutInflater(mInflater);

        HOST_TASK_MEMBER = String.format(HOST_TASK_MEMBER, mProjectObject.id);
        refresh();

    }

    private void refresh() {
        getNetwork(HOST_TASK_MEMBER, HOST_TASK_MEMBER);
    }

    ArrayList<TaskObject.UserTaskCount> mUsers = new ArrayList<TaskObject.UserTaskCount>();
    ArrayList<TaskObject.Members> mUsersInfo = new ArrayList<TaskObject.Members>();
    ArrayList<TaskObject.Members> mMembersAll = new ArrayList<TaskObject.Members>();
    ArrayList<TaskObject.Members> mMembersAllAll = new ArrayList<TaskObject.Members>();

    final String HOST_MEMBERS = Global.HOST + "/api/project/%s/members?pageSize=1000";
    String HOST_TASK_MEMBER = Global.HOST + "/api/project/%s/task/user/count";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MEMBERS)) {
            hideProgressDialog();
            if (code == 0) {
                ArrayList<TaskObject.Members> usersInfo = new ArrayList<TaskObject.Members>();

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); ++i) {
                    TaskObject.Members userInfo = new TaskObject.Members(jsonArray.getJSONObject(i));
                    if (mMemberTask.memberHasTask(userInfo.user_id)) { // 只显示有任务的
                        usersInfo.add(userInfo);
                    }

                    mMembersAllAll.add(userInfo);
                }

                mUsersInfo = usersInfo;
                mMembersAll = new ArrayList<TaskObject.Members>();
                mMembersAll.add(new TaskObject.Members());
                mMembersAll.addAll(mUsersInfo);

                adapter = new MyPagerAdapter(getChildFragmentManager());
                pager.setAdapter(adapter);

                final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                        .getDisplayMetrics());
                pager.setPageMargin(pageMargin);

                tabs.setViewPager(pager);
            } else {
                showErrorMsg(code, respanse);

                BlankViewDisplay.setBlank(mMembersAllAll.size(), this, false, blankLayout, onClickRetry);
            }

        } else if (tag.equals(HOST_TASK_MEMBER)) {
            if (code == 0) {
                mMemberTask.addItems(respanse.getJSONArray("data"));

                String getMembers = String.format(HOST_MEMBERS, mProjectObject.id);
                getNetwork(getMembers, HOST_MEMBERS);

            } else {
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mMembersAllAll.size(), this, false, blankLayout, onClickRetry);
            }
        }
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refresh();
        }
    };

//    public static final int RESUST_TASK_PAGER = 2010;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ListModify.RESULT_EDIT_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                taskListParentUpdate();
                String globarKey = data.getStringExtra(TaskAddActivity.RESULT_GLOBARKEY);

                TaskObject.Members modifyMember = null;
                for (int i = 0; i < mMembersAllAll.size(); ++i) {
                    if (mMembersAllAll.get(i).user.global_key.equals(globarKey)) {
                        modifyMember = mMembersAllAll.get(i);
                        break;
                    }
                }

                if (modifyMember != null) {
                    if (!mMembersAll.contains(modifyMember)) {
                        mMembersAll.add(modifyMember);
                        adapter.notifyDataSetChanged();
                        tabs.setViewPager(pager);
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void taskListParentUpdate() {
        List<Fragment> fragmentArray = getChildFragmentManager().getFragments();
        for (Fragment item : fragmentArray) {
            if (item instanceof TaskListUpdate) {
                ((TaskListUpdate) item).taskListUpdate();
            }
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter implements MyPagerSlidingTabStrip.IconTabProvider {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public int getCount() {
            return mMembersAll.size();
        }

        @Override
        public Fragment getItem(int position) {
            TaskListFragment_ fragment = new TaskListFragment_();
            Bundle bundle = new Bundle();
            bundle.putSerializable("mMembers", mMembersAll.get(position));
            bundle.putSerializable("mProjectObject", mProjectObject);
            bundle.putSerializable("mMembersArray", mUsersInfo);
            bundle.putSerializable("mMemberPos", position);
            bundle.putBoolean("mShowAdd", true);
            fragment.setParent(ProjectTaskFragment.this);

            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public String getPageIconUrl(int position) {
            return mMembersAll.get(position).user.avatar;
        }
    }

    MemberTaskCount mMemberTask = new MemberTaskCount();

    private static class MemberTaskCount {

        private ArrayList<Count> mData = new ArrayList<Count>();

        public void addItems(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); ++i) {
                Count count = new Count(jsonArray.getJSONObject(i));
                mData.add(count);
            }
        }

        public boolean memberHasTask(String id) {
            for (Count item : mData) {
                if (item.user.equals(id)) {
                    return true;
                }
            }

            return false;
        }

        static class Count {
            public int done;
            public int processing;
            public String user;

            public Count(JSONObject json) throws JSONException {
                done = json.getInt("done");
                processing = json.getInt("processing");
                user = json.getString("user");
            }
        }
    }

}
