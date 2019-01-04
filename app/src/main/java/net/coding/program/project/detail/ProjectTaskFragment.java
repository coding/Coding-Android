package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ListModify;
import net.coding.program.common.PinyinComparator;
import net.coding.program.common.event.EventRefreshTask;
import net.coding.program.common.event.EventRequestTaskCount;
import net.coding.program.common.event.EventUpdateTaskCount;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.ProjectTaskCountModel;
import net.coding.program.common.model.ProjectTaskUserCountModel;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.model.TaskProjectCountModel;
import net.coding.program.common.model.UserObject;
import net.coding.program.message.JSONUtils;
import net.coding.program.network.model.user.Member;
import net.coding.program.route.BlankViewDisplay;
import net.coding.program.task.add.TaskAddActivity;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.third.MyPagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

@EFragment(R.layout.fragment_project_task_filter)
@OptionsMenu(R.menu.fragment_project_task)
public class ProjectTaskFragment extends TaskFilterFragment {

    final String HOST_MEMBERS = Global.HOST_API + "/project/%d/members?pageSize=1000";

    private static final String TAG_PROJECT_TASK_COUNT = "TAG_PROJECT_TASK_COUNT";
    private static final String TAG_ALL_COUNT = "TAG_ALL_COUNT";
    private static final String TAG_WATCH_COUNT = "TAG_WATCH_COUNT";
    private static final String TAG_SOME_COUNT = "TAG_SOME_COUNT";
    private static final String TAG_SOME_LABEL = "TAG_SOME_LABEL";

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    MyPagerSlidingTabStrip tabs;
    @ViewById(R.id.pagerProjectTask)
    ViewPager pager;
    @ViewById
    View blankLayout;

    ArrayList<Member> mUsersInfo = new ArrayList<>();
    ArrayList<Member> mMembersAll = new ArrayList<>();
    ArrayList<Member> mMembersAllAll = new ArrayList<>();
    String HOST_TASK_MEMBER = Global.HOST_API + "/project/%d/task/user/count";
    View.OnClickListener onClickRetry = v -> refresh();

    MemberTaskCount mMemberTask = new MemberTaskCount();
    private MyPagerAdapter adapter;
    private DrawerLayout drawer;
    private UserObject account;

    @AfterViews
    protected final void initProjectTaskFragment() {
        showDialogLoading();

        setActionBarShadow(0);

        tabs.setLayoutInflater(mInflater);
        tabs.setVisibility(View.INVISIBLE);

        account = AccountInfo.loadAccount(getActivity());

        HOST_TASK_MEMBER = String.format(HOST_TASK_MEMBER, mProjectObject.getId());
        refresh();

        adapter = new MyPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                loadData(position);
            }
        });

        // 必须添加，否则回收恢复的时候，TaskListFragment 的 actionmenu 会显示几个出来
        setHasOptionsMenu(true);
        initFilterViews();
    }

    @Override
    protected void initFilterViews() {
        super.initFilterViews();
        toolBarTitle = (TextView) getActivity().findViewById(R.id.toolbarProjectTitle);
        if (toolBarTitle != null) {
            toolBarTitle.setOnClickListener(v -> {
                meActionFilter();
                loadDataCount();
            });
            toolBarTitle.setBackgroundResource(0);
            Drawable drawable = getResources().getDrawable(R.drawable.arrow_drop_down_green);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            toolBarTitle.setCompoundDrawables(null, null, drawable, null);
            toolBarTitle.setCompoundDrawablePadding(GlobalCommon.dpToPx(10));
            toolBarTitle.setText("全部任务");
        }

        loadData(0);
    }

    @Override
    protected boolean isProjectInner() {
        return true;
    }

    private void loadData(int index) {

        mTaskProjectCountModel = new TaskProjectCountModel();

        if (index == 0) {
            //全部成员
            //「全部任务」数量 - 进行中，已完成的 「我创建的」数量 = create
            getNetwork(String.format(urlProjectTaskCount, mProjectObject.getId()), urlProjectTaskCount);
            getNetwork(String.format(urlALL_Count, mProjectObject.getId()), urlALL_Count);
            getNetwork(String.format(urlALL_WATCH_Count, mProjectObject.getId(), account.id), urlALL_WATCH_Count);

            loadAllLabels();
        } else {
            Member members = mMembersAll.get(index);

            //某个成员
            getNetwork(String.format(urlSome_Count, mProjectObject.getId(), members.user_id), urlSome_Count);
            getNetwork(String.format(urlSome_Label, mProjectObject.getId(), members.user_id), urlSome_Label);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRequestTaskCount(EventRequestTaskCount event) {
        if (getActivity() == null) return;

        loadDataCount();
    }

    private void loadDataCount() {
        mTaskProjectCountModel = new TaskProjectCountModel();
        int index = pager.getCurrentItem();
        if (index == 0) {
            //全部成员
            //「全部任务」数量 - 进行中，已完成的 「我创建的」数量 = create
            getNetwork(String.format(urlProjectTaskCount, mProjectObject.getId()), TAG_PROJECT_TASK_COUNT);
            getNetwork(String.format(urlALL_Count, mProjectObject.getId()), TAG_ALL_COUNT);
            getNetwork(String.format(urlALL_WATCH_Count, mProjectObject.getId(), account.id), TAG_WATCH_COUNT);

        } else {
            Member members = mMembersAll.get(index);

            //某个成员
            getNetwork(String.format(urlSome_Count, mProjectObject.getId(), members.user_id), TAG_SOME_COUNT);
        }
    }

    private void loadAllLabels() {
//        int cur = tabs.getCurrentPosition();
        int cur = pager.getCurrentItem();
        if (cur != 0) {
            Member members = mMembersAll.get(cur);
            getNetwork(String.format(urlSome_Label, mProjectObject.getId(), members.user_id), urlSome_Label);
        } else {
            if (statusIndex == 0) {
                getNetwork(String.format(urlALL_Label, mProjectObject.owner_user_name, mProjectObject.name), urlALL_Label);
            } else {
                getNetwork(String.format(urlProjectTaskLabels, mProjectObject.getId()) + getRole(), urlProjectTaskLabels);
            }
        }
    }

    private void refresh() {
        getNetwork(HOST_TASK_MEMBER, HOST_TASK_MEMBER);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MEMBERS)) {
            hideDialogLoading();
            if (code == 0) {
                ArrayList<Member> usersInfo = new ArrayList<>();

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); ++i) {
                    Member userInfo = new Member(jsonArray.getJSONObject(i));
                    if (mMemberTask.memberHasTask(userInfo.user_id)) { // 只显示有任务的
                        if (userInfo.user.global_key.equals(GlobalData.sUserObject.global_key)) {
                            usersInfo.add(0, userInfo);
                        } else {
                            usersInfo.add(userInfo);
                        }
                    }

                    mMembersAllAll.add(userInfo);
                }

                mUsersInfo = usersInfo;
                mMembersAll = new ArrayList<>();
                mMembersAll.add(new Member());
                mMembersAll.addAll(mUsersInfo);

                adapter.notifyDataSetChanged();

                final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                        .getDisplayMetrics());
                pager.setPageMargin(pageMargin);

                tabs.setViewPager(pager);
                tabs.setVisibility(View.VISIBLE);
//                actionDivideLine.setVisibility(View.VISIBLE);
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
        } else if (tag.equals(urlProjectTaskCount)) {
            showLoading(false);
            if (code == 0) {
                TaskProjectCountModel projectTaskCountModel = JSONUtils.getData(respanse.getString("data"), TaskProjectCountModel.class);
                mTaskProjectCountModel.creatorDone = projectTaskCountModel.creatorDone;
                mTaskProjectCountModel.creatorProcessing = projectTaskCountModel.creatorProcessing;
                mTaskProjectCountModel.watcherDone = projectTaskCountModel.watcherDone;
                mTaskProjectCountModel.watcherProcessing = projectTaskCountModel.watcherProcessing;
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(urlALL_Count)) {
            showLoading(false);
            if (code == 0) {
                ProjectTaskCountModel projectTaskCountModel = JSONUtils.getData(respanse.getString("data"), ProjectTaskCountModel.class);
                mTaskProjectCountModel.owner = projectTaskCountModel.done + projectTaskCountModel.processing;
                mTaskProjectCountModel.ownerDone = projectTaskCountModel.done;
                mTaskProjectCountModel.ownerProcessing = projectTaskCountModel.processing;
                mTaskProjectCountModel.creator = projectTaskCountModel.create;
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(urlALL_WATCH_Count)) {
            showLoading(false);
            if (code == 0) {
                mTaskProjectCountModel.watcher = JSONUtils.getJSONLong("totalRow", respanse.getString("data"));
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(urlProjectTaskLabels)) {
            showLoading(false);
            if (code == 0) {
                taskLabelModels = JSONUtils.getTaskLabelModelList(respanse.getString("data"));
                Collections.sort(taskLabelModels, new PinyinComparator());
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(urlALL_Label)) {
            showLoading(false);
            if (code == 0) {
                taskLabelModels = JSONUtils.getTaskLabelModelList(respanse.getString("data"));
                Collections.sort(taskLabelModels, new PinyinComparator());
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(urlSome_Count)) {
            showLoading(false);
            if (code == 0) {
                ProjectTaskUserCountModel item = JSONUtils.getData(respanse.getString("data"), ProjectTaskUserCountModel.class);

                mTaskProjectCountModel.owner = item.memberDone + item.memberProcessing;
                mTaskProjectCountModel.ownerDone = item.memberDone;
                mTaskProjectCountModel.ownerProcessing = item.memberProcessing;

                mTaskProjectCountModel.creatorDone = item.creatorDone;
                mTaskProjectCountModel.creator = item.creatorDone + item.creatorProcessing;
                mTaskProjectCountModel.creatorProcessing = item.creatorProcessing;

                mTaskProjectCountModel.watcher = item.watcherDone + item.watcherProcessing;
                mTaskProjectCountModel.watcherDone = item.watcherDone;
                mTaskProjectCountModel.watcherProcessing = item.watcherProcessing;
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(urlSome_Label)) {
            showLoading(false);
            if (code == 0) {
                taskLabelModels = JSONUtils.getTaskLabelModelList(respanse.getString("data"));
                Collections.sort(taskLabelModels, new PinyinComparator());
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_PROJECT_TASK_COUNT)) {
            if (code == 0) {
                TaskProjectCountModel projectTaskCountModel = JSONUtils.getData(respanse.getString("data"), TaskProjectCountModel.class);
                mTaskProjectCountModel.creatorDone = projectTaskCountModel.creatorDone;
                mTaskProjectCountModel.creatorProcessing = projectTaskCountModel.creatorProcessing;
                mTaskProjectCountModel.watcherDone = projectTaskCountModel.watcherDone;
                mTaskProjectCountModel.watcherProcessing = projectTaskCountModel.watcherProcessing;
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_ALL_COUNT)) {
            if (code == 0) {
                ProjectTaskCountModel projectTaskCountModel = JSONUtils.getData(respanse.getString("data"), ProjectTaskCountModel.class);
                mTaskProjectCountModel.owner = projectTaskCountModel.done + projectTaskCountModel.processing;
                mTaskProjectCountModel.ownerDone = projectTaskCountModel.done;
                mTaskProjectCountModel.ownerProcessing = projectTaskCountModel.processing;
                mTaskProjectCountModel.creator = projectTaskCountModel.create;
                postEventUpdateCount();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_WATCH_COUNT)) {
            if (code == 0) {
                mTaskProjectCountModel.watcher = JSONUtils.getJSONLong("totalRow", respanse.getString("data"));
                postEventUpdateCount();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_SOME_COUNT)) {
            if (code == 0) {
                ProjectTaskUserCountModel item = JSONUtils.getData(respanse.getString("data"), ProjectTaskUserCountModel.class);

                mTaskProjectCountModel.owner = item.memberDone + item.memberProcessing;
                mTaskProjectCountModel.ownerDone = item.memberDone;
                mTaskProjectCountModel.ownerProcessing = item.memberProcessing;

                mTaskProjectCountModel.creatorDone = item.creatorDone;
                mTaskProjectCountModel.creator = item.creatorDone + item.creatorProcessing;
                mTaskProjectCountModel.creatorProcessing = item.creatorProcessing;

                mTaskProjectCountModel.watcher = item.watcherDone + item.watcherProcessing;
                mTaskProjectCountModel.watcherDone = item.watcherDone;
                mTaskProjectCountModel.watcherProcessing = item.watcherProcessing;

                postEventUpdateCount();
            } else {
                showErrorMsg(code, respanse);
            }
        }

        //设置DrawerLayout的数据
        setDrawerData();
    }

    private void postEventUpdateCount() {
        EventBus.getDefault().post(new EventUpdateTaskCount());
    }

    @OnActivityResult(ListModify.RESULT_EDIT_LIST)
    void onResultEditList(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            EventBus.getDefault().post(new EventRefreshTask());

            String globarKey = data.getStringExtra(TaskAddActivity.RESULT_GLOBARKEY);

            Member modifyMember = null;
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
//                    actionDivideLine.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @OptionsItem(R.id.action_add)
    void actionAdd() {
        floatButton();
    }

    public final void floatButton() {
        Member member = adapter.getItemData(pager.getCurrentItem());

//        Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
        SingleTask task = new SingleTask();
        task.project = mProjectObject;
        task.project_id = mProjectObject.getId();
        task.owner = AccountInfo.loadAccount(getActivity());
        task.owner_id = task.owner.id;

        TaskAddActivity_.intent(this)
                .mSingleTask(task)
                .mUserOwner(member.user)
                .canPickProject(false)
                .startForResult(ListModify.RESULT_EDIT_LIST);
    }

    @OptionsItem
    protected final void action_filter() {
        actionFilter();
    }

    @Override
    protected void sureFilter() {
        super.sureFilter();
        loadAllLabels();
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

    public class MyPagerAdapter extends FragmentStatePagerAdapter implements MyPagerSlidingTabStrip.IconTabProvider {

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

            bundle.putString("mMeAction", mMeActions[statusIndex]);
            if (mFilterModel != null) {
                bundle.putString("mStatus", mFilterModel.status + "");
                bundle.putString("mLabel", mFilterModel.label);
                bundle.putString("mKeyword", mFilterModel.keyword);
            } else {
                bundle.putString("mStatus", "");
                bundle.putString("mLabel", "");
                bundle.putString("mKeyword", "");
            }

            fragment.setArguments(bundle);

            return fragment;
        }

        public Member getItemData(int postion) {
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
