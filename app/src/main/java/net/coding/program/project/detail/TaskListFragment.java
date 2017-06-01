package net.coding.program.project.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.melnykov.fab.FloatingActionButton;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.common.widget.FlowLabelLayout;
import net.coding.program.event.EventFilterDetail;
import net.coding.program.event.EventRefrushTask;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.network.model.user.Member;
import net.coding.program.task.TaskListUpdate;
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

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.WeakHashMap;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends RefreshBaseFragment implements TaskListUpdate {

    public final String hostTaskDelete = getHostTaskDelete();
    final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //统计，已完成，进行中数量
    final String urlTaskCountProject = Global.HOST_API + "/project/%d/task/user/count";
    final String urlTaskCountMy = Global.HOST_API + "/tasks/projects/count";
    final String URL_TASK_SATUS = Global.HOST_API + "/task/%s/status";
    //筛选
    final String URL_TASK_FILTER = Global.HOST_API + "/tasks/search?";
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
    boolean mNeedUpdate = true;
    ArrayList<TaskObject.SingleTask> mData = new ArrayList<>();
    int mSectionId;
    int mTaskCount[] = new int[2];
    boolean mUpdateAll = true;
    String urlAll = "";
    View.OnClickListener onClickRetry = v -> onRefresh();
    TestBaseAdapter mAdapter;
    String mToday = "";
    String mTomorrow = "";
    WeakHashMap<View, Integer> mOriginalViewHeightPool = new WeakHashMap<>();

    private View listFooter;

    public static String getHostTaskDelete() {
        return Global.HOST_API + "/user/%s/project/%s/task/%s";
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        //setHasOptionsMenu(true);
    }

    @Override
    public void taskListUpdate(boolean must) {
        if (must) {
            mNeedUpdate = true;
        }

        if (mNeedUpdate) {
            mNeedUpdate = false;
            initSetting();
            loadData();
        }
    }

    @Override
    public void onRefresh() {
        initSetting();
        loadData();
    }

    @OptionsItem
    public void action_add() {
        mNeedUpdate = true;
        Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
        TaskObject.SingleTask task = new TaskObject.SingleTask();
        task.project = mProjectObject;
        task.project_id = mProjectObject.getId();
        task.owner = AccountInfo.loadAccount(getActivity());
        task.owner_id = task.owner.id;

        intent.putExtra("mSingleTask", task);
        intent.putExtra("mUserOwner", mMembers.user);

        getParentFragment().startActivityForResult(intent, ListModify.RESULT_EDIT_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mNeedUpdate = true;
        return super.onCreateView(inflater, container, savedInstanceState);
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
                    host += String.format("%s=%s&", mMeAction, MyApp.sUserObject.id);
                }
            }
        } else if (mShowAdd) {
            //项目内 全部任务
            if (!TextUtils.isEmpty(mMeAction) && !mMeAction.equals("owner")) {
                host += String.format("%s=%s&", mMeAction, MyApp.sUserObject.id);
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

        Calendar calendar = Calendar.getInstance();
        mToday = mDateFormat.format(calendar.getTimeInMillis());
        mTomorrow = mDateFormat.format(calendar.getTimeInMillis() + 1000 * 60 * 60 * 24);

        mNeedUpdate = true;
        mAdapter = new TestBaseAdapter();

        fab.attachToListView(listView.getWrappedList());
        fab.setVisibility(View.GONE);
        listFooter = getActivity().getLayoutInflater().inflate(R.layout.divide_bottom_15, listView.getWrappedList(), false);
        listView.setAreHeadersSticky(false);
        listView.addFooterView(listFooter, null, false);
        listView.setAdapter(mAdapter);

        updateFootStyle();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            TaskObject.SingleTask singleTask = (TaskObject.SingleTask) mAdapter.getItem(position);
            mNeedUpdate = true;

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

        taskListUpdate(true);
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

                JSONObject jsonData = respanse.getJSONObject("data");
                JSONArray array = jsonData.getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    TaskObject.SingleTask task = new TaskObject.SingleTask(array.getJSONObject(i));
                    mData.add(task);
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
                mNeedUpdate = false;
                EventBus.getDefault().post(new EventRefrushTask());
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(URL_TASK_SATUS)) {
            if (code == 0) {
                umengEvent(UmengEvent.TASK, "修改任务");
                umengEvent(UmengEvent.E_TASK, "标记完成");

                TaskParam param = (TaskParam) data;
                TaskObject.SingleTask task = param.mTask;
                task.status = param.mStatus;

            } else {
                Toast.makeText(getActivity(), "修改任务失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void deleteTask(final int pos) {
        TaskObject.SingleTask task = mData.get(pos);
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

    static class TaskParam {
        TaskObject.SingleTask mTask;
        int mStatus;

        TaskParam(TaskObject.SingleTask mTask, int mStatus) {
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
            for (TaskObject.SingleTask item : mData) {
                if (item.status == TaskObject.STATUS_PROGRESS) {
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
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final TaskObject.SingleTask data = (TaskObject.SingleTask) getItem(position);
            holder.mTitle.setText("      " + data.content);
            holder.mTitle.setTextColor(data.isDone() ? CodingColor.font4 : CodingColor.font1);

            holder.mRefId.setText(data.getNumber());
            holder.mName.setText(data.creator.name);
            holder.mTime.setText(Global.dayToNow(data.created_at, false));
            holder.mDiscuss.setText(String.valueOf(data.comments));
            iconfromNetwork(holder.mIcon, data.owner.avatar);

            int flowWidth = MyApp.sWidthPix - Global.dpToPx(100 + 15); // item 左边空 100 dp，右边空15dp
            if (!data.deadline.isEmpty()) {
                flowWidth -= Global.dpToPx(55);
            }
            holder.flowLabelLayout.setLabels(data.labels, flowWidth);

            final int pos = position;

            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(data.isDone());

            holder.mTaskDes.setVisibility(data.has_description ? View.VISIBLE : View.INVISIBLE);

            final int priorityIcons[] = new int[]{
                    R.drawable.task_mark_0,
                    R.drawable.task_mark_1,
                    R.drawable.task_mark_2,
                    R.drawable.task_mark_3,
            };

            int priority = data.priority;
            if (priorityIcons.length <= priority) {
                priority = priorityIcons.length - 1;
            }
            holder.mTaskPriority.setBackgroundResource(priorityIcons[priority]);

            if (data.deadline.isEmpty() && data.labels.isEmpty()) {
                holder.mLayoutDeadline.setVisibility(View.GONE);
            } else {
                holder.mLayoutDeadline.setVisibility(View.VISIBLE);
            }

            int[] taskColors = new int[]{
                    0xFFF68435,
                    0xFFA1CF64,
                    0xFFF56061,
                    0xFF59A2FF,
                    0xFFA9B3BE
            };

            if (data.deadline.isEmpty()) {
                holder.mDeadline.setVisibility(View.GONE);
            } else {
                holder.mDeadline.setVisibility(View.VISIBLE);

                if (data.deadline.equals(mToday)) {
                    holder.mDeadline.setText("今天");
                    holder.setDeadlineColor(taskColors[0]);
                } else if (data.deadline.equals(mTomorrow)) {
                    holder.mDeadline.setText("明天");
                    holder.setDeadlineColor(taskColors[1]);
                } else {
                    if (data.deadline.compareTo(mToday) < 0) {
                        holder.setDeadlineColor(taskColors[2]);
                    } else {
                        holder.setDeadlineColor(taskColors[3]);
                    }
                    String num[] = data.deadline.split("-");
                    holder.mDeadline.setText(String.format("%s/%s", num[1], num[2]));
                }

                if (data.isDone()) {
                    holder.setDeadlineColor(taskColors[4]);
                }
            }

            holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    statusTask(pos, data.getId(), isChecked));

            if (position == mData.size() - 1) {
                loadData();
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

        class ViewHolder {
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

            public void setDeadlineColor(int color) {
                mDeadline.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                mDeadline.setTextColor(color);
            }
        }
    }
}
