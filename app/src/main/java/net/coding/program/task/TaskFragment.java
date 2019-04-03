package net.coding.program.task;

import android.app.Activity;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ListModify;
import net.coding.program.common.PinyinComparator;
import net.coding.program.common.event.EventFilter;
import net.coding.program.common.event.EventRefreshTask;
import net.coding.program.common.event.EventRequestTaskCount;
import net.coding.program.common.event.EventUpdateTaskCount;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.TaskCountModel;
import net.coding.program.common.model.TaskProjectCountModel;
import net.coding.program.message.JSONUtils;
import net.coding.program.network.model.user.Member;
import net.coding.program.project.detail.TaskFilterFragment;
import net.coding.program.project.detail.TaskListFragment_;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.third.MyPagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
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


@EFragment(R.layout.fragment_task)
@OptionsMenu(R.menu.fragment_task)
public class TaskFragment extends TaskFilterFragment {

    final String host = Global.HOST_API + "/projects?pageSize=100&type=all";
    final String urlTaskCount = Global.HOST_API + "/tasks/projects/count";

    static final String TAG_TASK_COUNT_OWNER = "TAG_TASK_COUNT_OWNER";
    static final String TAG_TASK_COUNT_WATCHER = "TAG_TASK_COUNT_WATCHER";
    static final String TAG_TASK_COUNT_CREATOR = "TAG_TASK_COUNT_CREATOR";
    static final String TAG_TASK_COUNT_OTHER = "TAG_TASK_COUNT_OTHER";
    static final String TAG_TASK_COUNT_ALL = "TAG_TASK_COUNT_ALL";

    @ViewById
    protected MyPagerSlidingTabStrip tabs;

    @ViewById(R.id.pagerTaskFragment)
    protected ViewPager pager;

    ArrayList<ProjectObject> mData = new ArrayList<>();
    ArrayList<ProjectObject> mAllData = new ArrayList<>();

    int pageMargin;
    private PageTaskFragment adapter;

    @AfterViews
    protected void initTaskFragment() {
        pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());

        tabs.setLayoutInflater(mInflater);

        getNetwork(host, host);

        adapter = new PageTaskFragment(getChildFragmentManager());
        pager.setPageMargin(pageMargin);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                load(position);
            }
        });

        initFilterViews();
        showLoading(true);
    }

    @Override
    protected void initFilterViews() {
        super.initFilterViews();
        setHasOptionsMenu(true);
        load(0);
    }

    private void load(int index) {
        mTaskProjectCountModel = new TaskProjectCountModel();

        if (index == 0) {
            //全部项目
            getNetwork(urlTaskCountAll, urlTaskCountAll);
        } else {
            ProjectObject mProjectObject = mData.get(index);
            int userid = GlobalData.sUserObject.id;
            //某个项目
            getNetwork(String.format(urlTaskSomeCount_owner, mProjectObject.getId(), userid), urlTaskSomeCount_owner);
            getNetwork(String.format(urlTaskSomeCount_watcher, mProjectObject.getId(), userid), urlTaskSomeCount_watcher);
            getNetwork(String.format(urlTaskSomeCount_creator, mProjectObject.getId(), userid), urlTaskSomeCount_creator);
            getNetwork(String.format(urlTaskSomeOther, mProjectObject.getId()), urlTaskSomeOther);
        }
        loadLabels();
    }

    private void loadTaskCount() {
        int pos = pager.getCurrentItem();
        if (pos == 0) {
            getNetwork(urlTaskCountAll, TAG_TASK_COUNT_ALL);
        } else {
            ProjectObject mProjectObject = mData.get(pos);
            int userid = GlobalData.sUserObject.id;
            //某个项目
            getNetwork(String.format(urlTaskSomeCount_owner, mProjectObject.getId(), userid), TAG_TASK_COUNT_OWNER);
            getNetwork(String.format(urlTaskSomeCount_watcher, mProjectObject.getId(), userid), TAG_TASK_COUNT_WATCHER);
            getNetwork(String.format(urlTaskSomeCount_creator, mProjectObject.getId(), userid), TAG_TASK_COUNT_CREATOR);
            getNetwork(String.format(urlTaskSomeOther, mProjectObject.getId()), TAG_TASK_COUNT_OTHER);
        }
    }

    private void loadLabels() {
//        int cur = tabs.getCurrentPosition();
        int cur = pager.getCurrentItem();
        if (cur != 0) {
            ProjectObject mProjectObject = mData.get(cur);
            getNetwork(String.format(urlProjectTaskLabels, mProjectObject.getId()) + getRole(), urlProjectTaskLabels);
        } else {
            getNetwork(urlTaskLabel + getRole(), urlTaskLabel);
        }
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(host)) {
            if (code == 0) {
                JSONArray jsonArray = response.getJSONObject("data").getJSONArray("list");

                jsonToAllData(jsonArray);

                getNetwork(urlTaskCount, urlTaskCount);
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(urlTaskCount)) {
            showLoading(false);
            if (code == 0) {
                ArrayList<ProjectObject> oldProjects = new ArrayList<>();
                oldProjects.addAll(mData);

                JSONArray jsonArray = response.getJSONArray("data");
                jsonToData(jsonArray);

                tabs.setVisibility(View.VISIBLE);
//                actionDivideLine.setVisibility(View.VISIBLE);
                tabs.setViewPager(pager);

                boolean needUpdate;
                if (oldProjects.size() != mData.size()) {
                    needUpdate = true;
                } else {
                    needUpdate = false;
                    for (ProjectObject i : oldProjects) {
                        boolean find = false;
                        for (ProjectObject j : mData) {
                            if (j.id == i.id) {
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            needUpdate = true;
                            break;
                        }
                    }
                }
                if (needUpdate) {
                    adapter.notifyDataSetChanged();
                }
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(urlTaskCountAll)) {
            showLoading(false);
            if (code == 0) {
                TaskCountModel mTaskCountModel = JSONUtils.getData(response.getString("data"), TaskCountModel.class);
                mTaskProjectCountModel.owner = mTaskCountModel.processing + mTaskCountModel.done;
                mTaskProjectCountModel.ownerDone = mTaskCountModel.done;
                mTaskProjectCountModel.ownerProcessing = mTaskCountModel.processing;

                mTaskProjectCountModel.watcher = mTaskCountModel.watchAll;
                mTaskProjectCountModel.watcherDone = mTaskCountModel.watchAll - mTaskCountModel.watchAllProcessing;
                mTaskProjectCountModel.watcherProcessing = mTaskCountModel.watchAllProcessing;

                mTaskProjectCountModel.creator = mTaskCountModel.create;
                mTaskProjectCountModel.creatorDone = mTaskCountModel.create - mTaskCountModel.createProcessing;
                mTaskProjectCountModel.creatorProcessing = mTaskCountModel.createProcessing;


            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(urlTaskLabel) || tag.equals(urlProjectTaskLabels)) {
            showLoading(false);
            if (code == 0) {
                taskLabelModels = JSONUtils.getTaskLabelModelList(response.getString("data"));
                Collections.sort(taskLabelModels, new PinyinComparator());
            } else {
                showErrorMsg(code, response);
            }

        } else if (tag.equals(urlTaskSomeCount_owner)) {
            showLoading(false);
            if (code == 0) {
                mTaskProjectCountModel.owner = JSONUtils.getJSONLong("totalRow", response.getString("data"));
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(urlTaskSomeCount_watcher)) {
            showLoading(false);
            if (code == 0) {
                mTaskProjectCountModel.watcher = JSONUtils.getJSONLong("totalRow", response.getString("data"));
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(urlTaskSomeCount_creator)) {
            showLoading(false);
            if (code == 0) {
                mTaskProjectCountModel.creator = JSONUtils.getJSONLong("totalRow", response.getString("data"));
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(urlTaskSomeOther)) {
            showLoading(false);
            if (code == 0) {
                TaskProjectCountModel item = JSONUtils.getData(response.getString("data"), TaskProjectCountModel.class);
                mTaskProjectCountModel.ownerDone = item.ownerDone;
                mTaskProjectCountModel.ownerProcessing = item.ownerProcessing;
                mTaskProjectCountModel.creatorDone = item.creatorDone;
                mTaskProjectCountModel.creatorProcessing = item.creatorProcessing;
                mTaskProjectCountModel.watcherDone = item.watcherDone;
                mTaskProjectCountModel.watcherProcessing = item.watcherProcessing;
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(TAG_TASK_COUNT_OWNER)) {
            if (code == 0) {
                mTaskProjectCountModel.owner = JSONUtils.getJSONLong("totalRow", response.getString("data"));
                postUpdateEvent();
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(TAG_TASK_COUNT_WATCHER)) {
            if (code == 0) {
                mTaskProjectCountModel.watcher = JSONUtils.getJSONLong("totalRow", response.getString("data"));
                postUpdateEvent();
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(TAG_TASK_COUNT_CREATOR)) {
            if (code == 0) {
                mTaskProjectCountModel.creator = JSONUtils.getJSONLong("totalRow", response.getString("data"));
                postUpdateEvent();
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(TAG_TASK_COUNT_OTHER)) {
            if (code == 0) {
                TaskProjectCountModel item = JSONUtils.getData(response.getString("data"), TaskProjectCountModel.class);
                mTaskProjectCountModel.ownerDone = item.ownerDone;
                mTaskProjectCountModel.ownerProcessing = item.ownerProcessing;
                mTaskProjectCountModel.creatorDone = item.creatorDone;
                mTaskProjectCountModel.creatorProcessing = item.creatorProcessing;
                mTaskProjectCountModel.watcherDone = item.watcherDone;
                mTaskProjectCountModel.watcherProcessing = item.watcherProcessing;
                postUpdateEvent();
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(TAG_TASK_COUNT_ALL)) {
            if (code == 0) {
                TaskCountModel mTaskCountModel = JSONUtils.getData(response.getString("data"), TaskCountModel.class);
                mTaskProjectCountModel.owner = mTaskCountModel.processing + mTaskCountModel.done;
                mTaskProjectCountModel.ownerDone = mTaskCountModel.done;
                mTaskProjectCountModel.ownerProcessing = mTaskCountModel.processing;

                mTaskProjectCountModel.watcher = mTaskCountModel.watchAll;
                mTaskProjectCountModel.watcherDone = mTaskCountModel.watchAll - mTaskCountModel.watchAllProcessing;
                mTaskProjectCountModel.watcherProcessing = mTaskCountModel.watchAllProcessing;

                mTaskProjectCountModel.creator = mTaskCountModel.create;
                mTaskProjectCountModel.creatorDone = mTaskCountModel.create - mTaskCountModel.createProcessing;
                mTaskProjectCountModel.creatorProcessing = mTaskCountModel.createProcessing;

                postUpdateEvent();
            } else {
                showErrorMsg(code, response);
            }
        }

        //设置DrawerLayout的数据
        setDrawerData();
    }

    private void postUpdateEvent() {
        EventBus.getDefault().post(new EventUpdateTaskCount());
    }

    private void jsonToAllData(JSONArray jsonArray) throws JSONException {
        mAllData.clear();
        for (int i = 0; i < jsonArray.length(); ++i) {
            ProjectObject projectObject = new ProjectObject(jsonArray.getJSONObject(i));
            mAllData.add(projectObject);
        }
    }

    private void jsonToData(JSONArray jsonArray) throws JSONException {
        mData.clear();
        mData.add(new ProjectObject());

        for (int i = 0; i < jsonArray.length(); ++i) {
            TaskCount taskCount = new TaskCount(jsonArray.getJSONObject(i));
            for (int j = 0; j < mAllData.size(); ++j) {
                ProjectObject project = mAllData.get(j);
                if (taskCount.project == project.getId()) {
                    mData.add(project);
                }
            }
        }
    }

    @OnActivityResult(ListModify.RESULT_EDIT_LIST)
    void onResultEditList(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            EventBus.getDefault().post(new EventRefreshTask());
        }
    }

    @OptionsItem
    protected final void action_setting_add() {
        if (mData != null && mData.size() > 0) {
            ProjectObject projectObject = mData.get(pager.getCurrentItem());
            TaskAddActivity_.intent(this)
                    .mUserOwner(GlobalData.sUserObject)
                    .mProjectObject(projectObject)
                    .startForResult(ListModify.RESULT_EDIT_LIST);
        }
    }

    // 用于处理推送
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventFilter eventFilter) {
        //确定是我的任务筛选
        if (eventFilter.index == 1) {
            meActionFilter();
            loadTaskCount();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRequestTaskCount(EventRequestTaskCount event) {
        if (getActivity() == null) return;

        loadTaskCount();
    }

    // 用于处理推送
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainRefresh(EventRefreshTask event) {
        getNetwork(host, host);
    }

    @OptionsItem
    protected final void action_filter() {
        actionFilter();
    }

    @Override
    protected void sureFilter() {
        super.sureFilter();
        //筛选了状态，相应的筛选标签也变化
        //getNetwork(urlTaskLabel + getRole(), urlTaskLabel);
        loadLabels();
    }

    public static class TaskCount {
        public int project;
        public int processing;
        public int done;

        public TaskCount(JSONObject json) throws JSONException {
            project = json.optInt("project");
            processing = json.optInt("processing");
            done = json.optInt("done");
        }
    }

    private class PageTaskFragment extends FragmentStatePagerAdapter implements MyPagerSlidingTabStrip.IconTabProvider {

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
            bundle.putSerializable("mMembers", new Member(AccountInfo.loadAccount(getActivity())));
            bundle.putSerializable("mProjectObject", mData.get(position));
            bundle.putBoolean("mShowAdd", false);

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
    }
}
