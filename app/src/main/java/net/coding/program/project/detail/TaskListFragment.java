package net.coding.program.project.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.melnykov.fab.FloatingActionButton;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ListModify;
import net.coding.program.common.event.EventFilterDetail;
import net.coding.program.common.event.EventRefreshTask;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.common.widget.FlowLabelLayout;
import net.coding.program.network.model.user.Member;
import net.coding.program.route.BlankViewDisplay;
import net.coding.program.task.add.TaskAddActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends RefreshBaseFragment {

    public final String hostTaskDelete = getHostTaskDelete();
    //统计，已完成，进行中数量
    final String urlTaskCountProject = Global.HOST_API + "/project/%d/task/user/count";
    final String urlTaskCountMy = Global.HOST_API + "/tasks/projects/count";
    final String URL_TASK_SATUS = Global.HOST_API + "/task/%s/status";
    //筛选
    final String URL_TASK_FILTER = Global.HOST_API + "/tasks/search?";
    final String URL_TASK_FILTER_BLANK_KEYWORD = Global.HOST_API + "/tasks/list?";
    @FragmentArg
    boolean mShowAdd = false;
    // 4.关键字筛选
    // https://coding.net/api/tasks/search?keyword=Bug
    @FragmentArg
    String mMeAction;

    //筛选 有4种类型，
    // https://coding.net/api/tasks/search?creator=52353&label=bug&status=2&keyword=Bug
    //-------------------
    // 1.我的任务，我关注的，我创建的
    // https://coding.net/api/tasks/search?owner=52353
    // https://coding.net/api/tasks/search?watcher=52353
    // https://coding.net/api/tasks/search?creator=52353

    // 2.进行中，已完成
    // https://coding.net/api/tasks/search?status=1
    // https://coding.net/api/tasks/search?status=2

    // 3.标签筛选 标签内容
    // https://coding.net/api/tasks/search?label=Bug
    @FragmentArg
    String mStatus;
    @FragmentArg
    String mLabel;
    @FragmentArg
    String mKeyword;
    @FragmentArg
    Member mMembers;
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    View blankLayout;
    @ViewById
    FloatingActionButton fab;
    @ViewById
    StickyListHeadersListView listView;
    @StringArrayRes
    String[] task_titles;

    ArrayList<SingleTask> mData = new ArrayList<>();
    int mSectionId;
    int mTaskCount[] = new int[2];
    boolean mUpdateAll = true;
    String urlAll = "";
    View.OnClickListener onClickRetry = v -> onRefresh();
    TestBaseAdapter mAdapter;

    private View listFooter;

    private final EventRefreshTask sendEvent = new EventRefreshTask();

    public static String getHostTaskDelete() {
        return Global.HOST_API + "/user/%s/project/%s/task/%s";
    }

    @Override
    public void onRefresh() {
        initSetting();
        loadData();
    }

    @OptionsItem
    public void action_add() {
        Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
        SingleTask task = new SingleTask();
        task.project = mProjectObject;
        task.project_id = mProjectObject.getId();
        task.owner = AccountInfo.loadAccount(getActivity());
        task.owner_id = task.owner.id;

        intent.putExtra("mSingleTask", task);
        intent.putExtra("mUserOwner", mMembers.user);

        getParentFragment().startActivityForResult(intent, ListModify.RESULT_EDIT_LIST);
    }

    //检查是否有筛选条件
    String checkHostFilter() {
        String host = "";
        int userId = mMembers.user_id;

        //项目内 不是全部任务
        if (mShowAdd && userId != 0) {
            if (!TextUtils.isEmpty(mMeAction)) {
                host += String.format("owner=%s&", userId);
            }
            //关注，创建可以返回数据
            if (!TextUtils.isEmpty(mMeAction) && !mMeAction.equals("owner")) {
                if (!TextUtils.isEmpty(mMeAction)) {
                    host += String.format("%s=%s&", mMeAction, GlobalData.sUserObject.id);
                }
            }
        } else if (mShowAdd) {
            //项目内 全部任务
            if (!TextUtils.isEmpty(mMeAction) && !mMeAction.equals("owner")) {
                host += String.format("%s=%s&", mMeAction, GlobalData.sUserObject.id);
            }
        } else {
            //项目外
            if (!TextUtils.isEmpty(mMeAction) && userId != 0) {
                host += String.format("%s=%s&", mMeAction, userId);
            }
        }

        if (!TextUtils.isEmpty(mStatus) && !mStatus.equals("0")) {
            host += String.format("status=%s&", mStatus);
        }
        if (!TextUtils.isEmpty(mLabel)) {
            host += String.format("label=%s&", Global.encodeUtf8(mLabel));
        }
        if (!TextUtils.isEmpty(mKeyword)) {
            host += String.format("keyword=%s&", Global.encodeUtf8(mKeyword));
        }
        if (mProjectObject != null && !mProjectObject.isEmpty()) {
            host += String.format("project_id=%s&", mProjectObject.getId());
        }
        //去掉最后一个 &
        if (!TextUtils.isEmpty(host)) {
            return host.substring(0, host.length() - 1);
        }

        return host;
    }

    String createHost(String userId, String type) {
        //检查是否有筛选条件
        String searchUrl = checkHostFilter();

        if (!searchUrl.contains("keyword=")) {
            return URL_TASK_FILTER_BLANK_KEYWORD + searchUrl;
        }

        if (!TextUtils.isEmpty(searchUrl)) {
            return URL_TASK_FILTER + searchUrl;
        }

        String BASE_HOST = Global.HOST_API + "%s/tasks%s?";
        String userType;
        if (mProjectObject.isEmpty()) {
            userType = type;

        } else {
            if (userId.isEmpty()) {
                userType = type;
            } else {
                userType = "/user/" + userId + type;
            }
        }

        return String.format(BASE_HOST, mProjectObject.getBackendProjectPath(), userType);
    }

    @AfterViews
    protected void initTaskListFragment() {
        initRefreshLayout();

        SingleTask.initDate();

        mAdapter = new TestBaseAdapter();

        fab.attachToListView(listView.getWrappedList());
        fab.setVisibility(View.GONE);
        listFooter = getActivity().getLayoutInflater().inflate(R.layout.divide_bottom_15, listView.getWrappedList(), false);
        listView.setAreHeadersSticky(false);
        listView.addFooterView(listFooter, null, false);
        listView.setAdapter(mAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listView.setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }

        updateFootStyle();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SingleTask singleTask = (SingleTask) mAdapter.getItem(position);

            TaskAddActivity_.intent(getParentFragment())
                    .mSingleTask(singleTask)
                    .canPickProject(false)
                    .startForResult(ListModify.RESULT_EDIT_LIST);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String content = mData.get(position).content;
            showDialog("删除任务", content, (dialog, which) -> deleteTask(position));
            return true;
        });

        initUrlAndLoadData();
    }

    private void initUrlAndLoadData() {
        urlAll = createHost(mMembers.user.global_key, "/all");

        taskListUpdate(null);
        taskFragmentLoading(true);
    }

    private void updateFootStyle() {
        if (mData.isEmpty()) {
            listFooter.setVisibility(View.INVISIBLE);
        } else {
            listFooter.setVisibility(View.VISIBLE);
        }
    }

    @Click
    void fab() {
        action_add();
    }

    private void loadData() {
        getNextPageNetwork(urlAll, urlAll);

        if (mUpdateAll) {
            if (mProjectObject.isEmpty()) {
                getNetwork(urlTaskCountMy, urlTaskCountMy);
            } else {
                String url = String.format(urlTaskCountProject, mProjectObject.getId());
                getNetwork(url, urlTaskCountProject);
            }
        }
    }


    @Override
    protected void initSetting() {
        super.initSetting();

        mTaskCount[0] = 0;
        mTaskCount[1] = 0;

        mSectionId = 0;
        mUpdateAll = true;
    }

    public void taskFragmentLoading(boolean isLoading) {
        BlankViewHelp.setBlankLoading(blankLayout, isLoading);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlAll)) {
            setRefreshing(false);
            taskFragmentLoading(false);

            if (code == 0) {
                if (mUpdateAll) {
                    mData.clear();
                    mUpdateAll = false;
                }

                JSONObject jsonData = respanse.optJSONObject("data");
                if (jsonData != null) {
                    JSONArray array = jsonData.optJSONArray("list");
                    if (array != null) {
                        for (int i = 0; i < array.length(); ++i) {
                            SingleTask task = new SingleTask(array.getJSONObject(i));
                            mData.add(task);
                        }
                    }
                }

                mAdapter.notifyDataSetChanged();

                AccountInfo.saveTasks(getActivity(), mData, mProjectObject.getId(), mMembers.id);
                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

            } else {
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
            }

            mUpdateAll = false;
        } else if (tag.equals(urlTaskCountMy)) {
            if (code == 0) {
                JSONArray array = respanse.getJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    if (item.optInt("project") == (mProjectObject.getId())) {
                        mTaskCount[0] = item.getInt("processing");
                        mTaskCount[1] = item.getInt("done");
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

        } else if (tag.equals(urlTaskCountProject)) {
            if (code == 0) {
                JSONArray array = respanse.getJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    if (Integer.valueOf(item.getString("user")) == mMembers.id) {
                        mTaskCount[0] = item.getInt("processing");
                        mTaskCount[1] = item.getInt("done");
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

        } else if (tag.equals(hostTaskDelete)) {
            if (code == 0) {
                umengEvent(UmengEvent.TASK, "删除任务");
                mData.remove(pos);
                mAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(sendEvent);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(URL_TASK_SATUS)) {
            if (code == 0) {
                umengEvent(UmengEvent.TASK, "修改任务");
                umengEvent(UmengEvent.E_TASK, "标记完成");

                TaskParam param = (TaskParam) data;
                SingleTask task = param.mTask;
                task.status = param.mStatus;

            } else {
                Toast.makeText(getActivity(), "修改任务失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void deleteTask(final int pos) {
        SingleTask task = mData.get(pos);
        String url = String.format(hostTaskDelete, task.project.owner_user_name, task.project.name, task.getId());
        deleteNetwork(url, hostTaskDelete, pos, null);
    }

    void statusTask(final int pos, final int id, final boolean complete) {
        RequestParams params = new RequestParams();
        int completeStatus = complete ? 2 : 1;
        params.put("status", completeStatus); // 任务完成2，任务正在进行1
        putNetwork(String.format(URL_TASK_SATUS, id), params, URL_TASK_SATUS, new TaskParam(mData.get(pos), completeStatus));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    //筛选后刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventFilterDetail eventFilter) {
        mMeAction = eventFilter.meAction;
        mStatus = eventFilter.status;
        mLabel = eventFilter.label;
        mKeyword = eventFilter.keyword;

        //重新加载所有
        mUpdateAll = true;
        initUrlAndLoadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void taskListUpdate(EventRefreshTask refrushTask) {
        if (sendEvent == refrushTask) return;

        initSetting();
        loadData();
    }

    static class TaskParam {
        SingleTask mTask;
        int mStatus;

        TaskParam(SingleTask mTask, int mStatus) {
            this.mTask = mTask;
            this.mStatus = mStatus;
        }
    }

    public class TestBaseAdapter extends BaseAdapter implements
            StickyListHeadersAdapter {

        public TestBaseAdapter() {
        }

        @Override
        public void notifyDataSetChanged() {
            mSectionId = 0;
            for (SingleTask item : mData) {
                if (item.status == SingleTask.STATUS_PROGRESS) {
                    ++mSectionId;
                } else {
                    break;
                }
            }

            updateFootStyle();

            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_task_list_item, parent, false);
                holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                holder.mTitle = (TextView) convertView.findViewById(R.id.title);
                holder.mDeadline = (TextView) convertView.findViewById(R.id.deadline);
                holder.mDeadline.setBackgroundResource(R.drawable.task_list_item_deadline_background2);
                holder.mName = (TextView) convertView.findViewById(R.id.name);
                holder.mTime = (TextView) convertView.findViewById(R.id.time);
                holder.mDiscuss = (TextView) convertView.findViewById(R.id.discuss);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.mTaskPriority = convertView.findViewById(R.id.taskPriority);
                holder.mTaskDes = convertView.findViewById(R.id.taskDes);
                holder.mLayoutDeadline = convertView.findViewById(R.id.layoutDeadline);
                holder.mRefId = (TextView) convertView.findViewById(R.id.referenceId);
                holder.flowLabelLayout = (FlowLabelLayout) convertView.findViewById(R.id.flowLayout);
                holder.bottomLine = convertView.findViewById(R.id.bottomLine);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final SingleTask data = (SingleTask) getItem(position);
            holder.mTitle.setText("      " + data.content);
            holder.mTitle.setTextColor(data.isDone() ? CodingColor.font4 : CodingColor.font1);

            holder.mRefId.setText(data.getNumber());
            holder.mName.setText(data.creator.name);
            holder.mTime.setText(Global.dayToNow(data.created_at, false));
            holder.mDiscuss.setText(String.valueOf(data.comments));
            iconfromNetwork(holder.mIcon, data.owner.avatar);

            int flowWidth = GlobalData.sWidthPix - GlobalCommon.dpToPx(100 + 15); // item 左边空 100 dp，右边空15dp
            if (!data.deadline.isEmpty()) {
                flowWidth -= GlobalCommon.dpToPx(55);
            }
            holder.flowLabelLayout.setLabels(data.labels, flowWidth);

            final int pos = position;

            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(data.isDone());

            holder.mTaskDes.setVisibility(data.has_description ? View.VISIBLE : View.INVISIBLE);

            holder.mTaskPriority.setBackgroundResource(data.getPriorityIcon());

            if (data.deadline.isEmpty() && data.labels.isEmpty()) {
                holder.mLayoutDeadline.setVisibility(View.GONE);
            } else {
                holder.mLayoutDeadline.setVisibility(View.VISIBLE);
            }


            SingleTask.setDeadline(holder.mDeadline, data);

            holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    statusTask(pos, data.getId(), isChecked));

            if (position == mData.size() - 1) {
                holder.bottomLine.setVisibility(View.INVISIBLE);
                loadData();
            } else {
                holder.bottomLine.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.divide_top_15, parent, false);
            }

            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return 0;
        }

        private class ViewHolder {
            CheckBox mCheckBox;
            ImageView mIcon;
            TextView mTitle;

            TextView mDeadline;
            TextView mName;
            TextView mTime;
            TextView mDiscuss;
            View mTaskPriority;
            View mTaskDes;
            View mLayoutDeadline;
            FlowLabelLayout flowLabelLayout;
            TextView mRefId;
            View bottomLine;
       }
    }
}
