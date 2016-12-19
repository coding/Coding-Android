package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.util.ViewUtils;
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
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project_task)
@OptionsMenu(R.menu.fragment_project_task)
public class ProjectTaskFragment extends TaskFilterFragment implements TaskListParentUpdate, TaskListFragment.FloatButton {

    final String HOST_MEMBERS = Global.HOST_API + "/project/%d/members?pageSize=1000";

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    MyPagerSlidingTabStrip tabs;
    @ViewById(R.id.pagerProjectTask)
    ViewPager pager;
    @ViewById
    View blankLayout, actionDivideLine;
    @ViewById
    FloatingActionButton floatButton;

    ArrayList<TaskObject.Members> mUsersInfo = new ArrayList<>();
    ArrayList<TaskObject.Members> mMembersAll = new ArrayList<>();
    ArrayList<TaskObject.Members> mMembersAllAll = new ArrayList<>();
    String HOST_TASK_MEMBER = Global.HOST_API + "/project/%d/task/user/count";
    View.OnClickListener onClickRetry = v -> refresh();

    MemberTaskCount mMemberTask = new MemberTaskCount();
    private MyPagerAdapter adapter;
    private DrawerLayout drawer;

    @AfterViews
    protected final void initProjectTaskFragment() {
        showDialogLoading();

        setActionBarShadow(0);

        tabs.setLayoutInflater(mInflater);
        tabs.setVisibility(View.INVISIBLE);

        HOST_TASK_MEMBER = String.format(HOST_TASK_MEMBER, mProjectObject.getId());
        refresh();

        adapter = new MyPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        // 必须添加，否则回收恢复的时候，TaskListFragment 的 actionmenu 会显示几个出来
        setHasOptionsMenu(true);

        TextView viewById = (TextView) ViewUtils.findActionBarTitle(getActivity().getWindow().getDecorView());
        if (viewById != null) {
            viewById.setBackgroundResource(R.drawable.maopao_spinner);
            viewById.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Do something
                    Toast.makeText(getActivity(), "hello world", Toast.LENGTH_SHORT).show();
                }
            });
            viewById.setText("我的任务");
        }

        initFilterViews();
    }


    private void refresh() {
        getNetwork(HOST_TASK_MEMBER, HOST_TASK_MEMBER);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        postLabelJson(tag, code, respanse);
        if (tag.equals(HOST_MEMBERS)) {
            hideDialogLoading();
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
                tabs.setVisibility(View.VISIBLE);
                actionDivideLine.setVisibility(View.VISIBLE);
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
                hideDialogLoading();
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mMembersAllAll.size(), this, false, blankLayout, onClickRetry);
            }
        }

    }

    @OnActivityResult(ListModify.RESULT_EDIT_LIST)
    void onResultEditList(int resultCode, Intent data) {
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
                    tabs.setVisibility(View.VISIBLE);
                    actionDivideLine.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void taskListParentUpdate() {
        List<WeakReference<Fragment>> fragmentArray = adapter.getFragments();
        for (WeakReference<Fragment> ref : fragmentArray) {
            Fragment item = ref.get();
            if (item instanceof TaskListUpdate) {
                ((TaskListUpdate) item).taskListUpdate(true);
            }
        }
    }

    @Click
    public final void floatButton() {
        TaskObject.Members member = adapter.getItemData(pager.getCurrentItem());

//        Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
        TaskObject.SingleTask task = new TaskObject.SingleTask();
        task.project = mProjectObject;
        task.project_id = mProjectObject.getId();
        task.owner = AccountInfo.loadAccount(getActivity());
        task.owner_id = task.owner.id;

        TaskAddActivity_.intent(this)
                .mSingleTask(task)
                .mUserOwner(member.user)
                .startForResult(ListModify.RESULT_EDIT_LIST);
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
//            bundle.putString("mMeAction", "");
//            bundle.putString("mStatus", "");
//            bundle.putString("mLabel", "");
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

    @OptionsItem
    protected final void action_filter() {
        actionFilter();
        //drawer.openDrawer(GravityCompat.END);
    }

}
