package net.coding.program.project.detail;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.melnykov.fab.FloatingActionButton;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.FlowLabelLayout;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.task.TaskListUpdate;
import net.coding.program.task.add.TaskAddActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.WeakHashMap;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends RefreshBaseFragment implements TaskListUpdate {

    public static final String hostTaskDelete = Global.HOST_API + "/user/%s/project/%s/task/%s";
    final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final String urlTaskCountProject = Global.HOST_API + "/project/%d/task/user/count";
    final String urlTaskCountMy = Global.HOST_API + "/tasks/projects/count";
    final String URL_TASK_SATUS = Global.HOST_API + "/task/%s/status";
    @FragmentArg
    boolean mShowAdd = false;
    @FragmentArg
    TaskObject.Members mMembers;
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    View blankLayout;
    @ViewById
    FloatingActionButton fab;
    boolean mNeedUpdate = true;
    ArrayList<TaskObject.SingleTask> mData = new ArrayList<>();
    int mSectionId;
    @StringArrayRes
    String[] task_titles;
    @ViewById
    StickyListHeadersListView listView;
    int mTaskCount[] = new int[2];
    boolean mUpdateAll = true;
    String urlAll = "";
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
    TestBaseAdapter mAdapter;
    String mToday = "";
    String mTomorrow = "";
    WeakHashMap<View, Integer> mOriginalViewHeightPool = new WeakHashMap<>();
    private net.coding.program.task.TaskListParentUpdate mParent;

    public void setParent(net.coding.program.task.TaskListParentUpdate parent) {
        mParent = parent;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void taskListUpdate() {
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

    String createHost(String userId, String type) {
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

        return String.format(BASE_HOST, mProjectObject.backend_project_path, userType);
    }

    @AfterViews
    protected void init() {
        initRefreshLayout();

//        mData = AccountInfo.loadTasks(getActivity(), mProjectObject.getId(), mMembers.id);

        Calendar calendar = Calendar.getInstance();
        mToday = mDateFormat.format(calendar.getTimeInMillis());
        mTomorrow = mDateFormat.format(calendar.getTimeInMillis() + 1000 * 60 * 60 * 24);

        mNeedUpdate = true;
        mAdapter = new TestBaseAdapter();

        fab.attachToListView(listView.getWrappedList());
        fab.setVisibility(View.GONE);
//        listView.setAnimExecutor(new AnimationExecutor());
        View footer = getActivity().getLayoutInflater().inflate(R.layout.divide_15_top, null);
        listView.addFooterView(footer, null, false);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskObject.SingleTask singleTask = (TaskObject.SingleTask) mAdapter.getItem(position);
//                if (singleTask.status == 1) {
                mNeedUpdate = true;
                Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
                intent.putExtra("mSingleTask", singleTask);
                getParentFragment().startActivityForResult(intent, ListModify.RESULT_EDIT_LIST);
//                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                String content = mData.get(position).content;
                showDialog("删除任务", content, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTask(position);
                    }
                });

                return true;
            }
        });

//        if (getParentFragment() instanceof FloatButton) {
//            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//                private int mLastScrollY;
//                private int mPreviousFirstVisibleItem;
//                private int mScrollThreshold;
//
//                @Override
//                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                    if(totalItemCount == 0) return;
//                    if (isSameRow(firstVisibleItem)) {
//                        int newScrollY = getTopItemScrollY(view);
//                        boolean isSignificantDelta = Math.abs(mLastScrollY - newScrollY) > mScrollThreshold;
//                        if (isSignificantDelta) {
//                            if (mLastScrollY > newScrollY) {
//                                onScrollUp();
//                            } else {
//                                onScrollDown();
//                            }
//                        }
//                        mLastScrollY = newScrollY;
//                    } else {
//                        if (firstVisibleItem > mPreviousFirstVisibleItem) {
//                            onScrollUp();
//                        } else {
//                            onScrollDown();
//                        }
//
//                        mLastScrollY = getTopItemScrollY(view);
//                        mPreviousFirstVisibleItem = firstVisibleItem;
//                    }
//                }
//
//                private boolean isSameRow(int firstVisibleItem) {
//                    return firstVisibleItem == mPreviousFirstVisibleItem;
//                }
//
//                private int getTopItemScrollY(AbsListView mListView) {
//                    if (mListView == null || mListView.getChildAt(0) == null) return 0;
//                    View topChild = mListView.getChildAt(0);
//                    return topChild.getTop();
//                }
//
////                private ScrollDirectionListener mScrollDirectionListener;
////                private AbsListView.OnScrollListener mOnScrollListener;
////
////                private void setScrollDirectionListener(ScrollDirectionListener scrollDirectionListener) {
////                    mScrollDirectionListener = scrollDirectionListener;
////                }
////
////                public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
////                    mOnScrollListener = onScrollListener;
////                }
//
//
//                @Override
//                public void onScrollStateChanged(AbsListView view, int scrollState) {
//                }
//
//                private void onScrollDown() {
//                    ((FloatButton) getParentFragment()).showFloatButton(true);
//                }
//
//                private void onScrollUp() {
//                    ((FloatButton) getParentFragment()).showFloatButton(false);
//                }
//            });
//        }

        urlAll = createHost(mMembers.user.global_key, "/all");

        loadData();
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

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlAll)) {
            setRefreshing(false);

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
                if (mParent != null) {
                    mNeedUpdate = false;
                    mParent.taskListParentUpdate();
                }

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(URL_TASK_SATUS)) {
            if (code == 0) {
                umengEvent(UmengEvent.TASK, "修改任务");

                TaskParam param = (TaskParam) data;
                TaskObject.SingleTask task = param.mTask;
                task.status = param.mStatus;

                if (mParent != null) {
                    mNeedUpdate = false;
                    mParent.taskListParentUpdate();
                }

            } else {
                Toast.makeText(getActivity(), "修改任务失败", Toast.LENGTH_SHORT).show();
            }

            mAdapter.notifyDataSetChanged();
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

    public interface FloatButton {
        void showFloatButton(boolean show);
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
            StickyListHeadersAdapter, SectionIndexer {

        public TestBaseAdapter() {
        }

        @Override
        public void notifyDataSetChanged() {
            mSectionId = 0;
            for (TaskObject.SingleTask item  : mData) {
                if (item.status == TaskObject.STATUS_PRECESS) {
                    ++mSectionId;
                } else {
                    break;
                }
            }

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
                holder.mDeadline.setBackgroundResource(R.drawable.task_list_item_deadline_background);
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

            holder.mRefId.setText(data.getNumber());
            holder.mName.setText(data.creator.name);
            holder.mTime.setText(Global.dayToNow(data.created_at));
            holder.mDiscuss.setText(String.valueOf(data.comments));
            iconfromNetwork(holder.mIcon, data.owner.avatar);


            int flowWidth = MyApp.sWidthPix - Global.dpToPx(100 + 12); // item 左边空 100 dp，右边空12dp
            if (!data.deadline.isEmpty()) {
                flowWidth -= Global.dpToPx(55);
            }
            holder.flowLabelLayout.setLabels(data.labels, flowWidth);

            final int pos = position;

            holder.mCheckBox.setOnCheckedChangeListener(null);
            if (data.status == 1) {
                holder.mCheckBox.setChecked(false);
            } else {
                holder.mCheckBox.setChecked(true);
            }

            holder.mTaskDes.setVisibility(data.has_description ? View.VISIBLE : View.INVISIBLE);

            final int priorityIcons[] = new int[]{
                    R.drawable.task_mark_0,
                    R.drawable.task_mark_1,
                    R.drawable.task_mark_2,
                    R.drawable.task_mark_3,
            };

            holder.mTaskPriority.setBackgroundResource(priorityIcons[data.priority]);

            if (data.deadline.isEmpty() && data.labels.isEmpty()) {
                holder.mLayoutDeadline.setVisibility(View.GONE);
            } else {
                holder.mLayoutDeadline.setVisibility(View.VISIBLE);
            }

            int[] taskColors = new int[]{
                    0xfff49f31,
                    0xff97ba66,
                    0xfff24b4b,
                    0xffb2c6d0,
                    0xffc7c8c7
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

            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    statusTask(pos, data.getId(), isChecked);
                }
            });

            if (position == mData.size() - 1) {
                loadData();
            }

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_project_dynamic_list_head, parent, false);
                holder.mHead = (TextView) convertView.findViewById(R.id.head);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            int type = getSectionForPosition(position);
            String title = task_titles[type];
            holder.mHead.setText(title);

            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return getSectionForPosition(position);
        }

        @Override
        public int getPositionForSection(int section) {
            return section;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position < mSectionId) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public Object[] getSections() {
            return task_titles;
        }

        class HeaderViewHolder {
            TextView mHead;
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

    //animation executor
    class AnimationExecutor implements ExpandableStickyListHeadersListView.IAnimationExecutor {

        @Override
        public void executeAnim(final View target, final int animType) {
            if (ExpandableStickyListHeadersListView.ANIMATION_EXPAND == animType && target.getVisibility() == View.VISIBLE) {
                return;
            }
            if (ExpandableStickyListHeadersListView.ANIMATION_COLLAPSE == animType && target.getVisibility() != View.VISIBLE) {
                return;
            }
            if (mOriginalViewHeightPool.get(target) == null) {
                mOriginalViewHeightPool.put(target, target.getHeight());
            }
            final int viewHeight = mOriginalViewHeightPool.get(target);
            float animStartY = animType == ExpandableStickyListHeadersListView.ANIMATION_EXPAND ? 0f : viewHeight;
            float animEndY = animType == ExpandableStickyListHeadersListView.ANIMATION_EXPAND ? viewHeight : 0f;
            final ViewGroup.LayoutParams lp = target.getLayoutParams();
            ValueAnimator animator = ValueAnimator.ofFloat(animStartY, animEndY);
            animator.setDuration(200);
            target.setVisibility(View.VISIBLE);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (animType == ExpandableStickyListHeadersListView.ANIMATION_EXPAND) {
                        target.setVisibility(View.VISIBLE);
                    } else {
                        target.setVisibility(View.GONE);
                    }
                    target.getLayoutParams().height = viewHeight;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    lp.height = ((Float) valueAnimator.getAnimatedValue()).intValue();
                    target.setLayoutParams(lp);
                    target.requestLayout();
                }
            });
            animator.start();
        }
    }
}
