package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.task.TaskListParentUpdate;
import net.coding.program.task.TaskListUpdate;
import net.coding.program.task.add.TaskAddActivity;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.third.MyPagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project_task)
public class ProjectTaskFragment extends BaseFragment implements TaskListParentUpdate, TaskListFragment.FloatButton {

    final String HOST_MEMBERS = Global.HOST_API + "/project/%d/members?pageSize=1000";
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    MyPagerSlidingTabStrip tabs;
    @ViewById(R.id.pagerProjectTask)
    ViewPager pager;
    @ViewById
    View blankLayout;
    @ViewById
    FloatingActionButton floatButton;
    ArrayList<TaskObject.Members> mUsersInfo = new ArrayList<>();
    ArrayList<TaskObject.Members> mMembersAll = new ArrayList<>();
    ArrayList<TaskObject.Members> mMembersAllAll = new ArrayList<>();
    String HOST_TASK_MEMBER = Global.HOST_API + "/project/%d/task/user/count";
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refresh();
        }
    };
    MemberTaskCount mMemberTask = new MemberTaskCount();
    private MyPagerAdapter adapter;

    @AfterViews
    protected final void init() {
        showDialogLoading();
        tabs.setLayoutInflater(mInflater);

        HOST_TASK_MEMBER = String.format(HOST_TASK_MEMBER, mProjectObject.getId());
        refresh();

        adapter = new MyPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        // 必须添加，否则回收恢复的时候，TaskListFragment 的 actionmenu 会显示几个出来
        setHasOptionsMenu(true);
    }

    private void refresh() {
        getNetwork(HOST_TASK_MEMBER, HOST_TASK_MEMBER);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MEMBERS)) {
            hideProgressDialog();
            if (code == 0) {
                ArrayList<TaskObject.Members> usersInfo = new ArrayList<>();

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); ++i) {
                    TaskObject.Members userInfo = new TaskObject.Members(jsonArray.getJSONObject(i));
                    if (mMemberTask.memberHasTask(userInfo.user_id)) { // 只显示有任务的
                        if (userInfo.user.global_key.equals(MyApp.sUserObject.global_key)) {
                            usersInfo.add(0, userInfo);
                        } else {
                            usersInfo.add(userInfo);
                        }
                    }

                    mMembersAllAll.add(userInfo);
                }

                mUsersInfo = usersInfo;
                mMembersAll = new ArrayList<>();
                mMembersAll.add(new TaskObject.Members());
                mMembersAll.addAll(mUsersInfo);

                adapter.notifyDataSetChanged();

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

                String getMembers = String.format(HOST_MEMBERS, mProjectObject.getId());
                getNetwork(getMembers, HOST_MEMBERS);

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mMembersAllAll.size(), this, false, blankLayout, onClickRetry);
            }
        }
    }

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
        List<WeakReference<Fragment>> fragmentArray = adapter.getFragments();
        for (WeakReference<Fragment> ref : fragmentArray) {
            Fragment item = ref.get();
            if (item instanceof TaskListUpdate) {
                ((TaskListUpdate) item).taskListUpdate();
            }
        }
    }

    @Click
    public final void floatButton() {
        TaskObject.Members member = adapter.getItemData(pager.getCurrentItem());

        Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
        TaskObject.SingleTask task = new TaskObject.SingleTask();
        task.project = mProjectObject;
        task.project_id = mProjectObject.getId();
        task.owner = AccountInfo.loadAccount(getActivity());
        task.owner_id = task.owner.id;

        intent.putExtra("mSingleTask", task);
        intent.putExtra("mUserOwner", member.user);

        startActivityForResult(intent, ListModify.RESULT_EDIT_LIST);
    }

    @Override
    public void showFloatButton(boolean show) {
        if (show) {
            floatButton.show();
        } else {
            floatButton.hide();
        }
    }

    private static class MemberTaskCount {

        private ArrayList<Count> mData = new ArrayList<>();

        public void addItems(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); ++i) {
                Count count = new Count(jsonArray.getJSONObject(i));
                mData.add(count);
            }
        }

        public boolean memberHasTask(int id) {
            for (Count item : mData) {
                if (item.user == id) {
                    return true;
                }
            }

            return false;
        }

        static class Count {
            public int done;
            public int processing;
            public int user;

            public Count(JSONObject json) {
                done = json.optInt("done");
                processing = json.optInt("processing");
                user = json.optInt("user");
            }
        }
    }

    public class MyPagerAdapter extends SaveFragmentPagerAdapter implements MyPagerSlidingTabStrip.IconTabProvider {

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

            saveFragment(fragment);

            return fragment;
        }

        public TaskObject.Members getItemData(int postion) {
            return mMembersAll.get(postion);
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
}
