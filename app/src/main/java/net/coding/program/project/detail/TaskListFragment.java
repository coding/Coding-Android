package net.coding.program.project.detail;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ListModify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.task.TaskListUpdate;

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

import java.util.ArrayList;
import java.util.WeakHashMap;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends RefreshBaseFragment implements TaskListUpdate {

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

    private net.coding.program.task.TaskListParentUpdate mParent;

    public void setParent(net.coding.program.task.TaskListParentUpdate parent) {
        mParent = parent;
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
    void action_add() {
        mNeedUpdate = true;
        Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
        TaskObject.SingleTask task = new TaskObject.SingleTask();
        task.project = mProjectObject;
        task.project_id = mProjectObject.id;
        task.owner = AccountInfo.loadAccount(getActivity());
        task.owner_id = task.owner.id;

        intent.putExtra("mSingleTask", task);
        intent.putExtra("mUserOwner", mMembers.user);

        getParentFragment().startActivityForResult(intent, ListModify.RESULT_EDIT_LIST);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mShowAdd && !mProjectObject.id.isEmpty()) {
            inflater.inflate(R.menu.project_task, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mNeedUpdate = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    boolean mNeedUpdate = true;

    ArrayList<TaskObject.SingleTask> mData = new ArrayList<TaskObject.SingleTask>();
    int mSectionId;

    @StringArrayRes
    String[] task_titles;

    String createHost(String userId, String type) {
        String BASE_HOST = Global.HOST + "/api%s/tasks%s?";
        String userType;
        if (mProjectObject.id.isEmpty()) {
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

    @ViewById
    ExpandableStickyListHeadersListView listView;

    int mTaskCount[] = new int[2];

    @AfterViews
    protected void init() {
        super.init();

        mNeedUpdate = true;
        mAdapter = new TestBaseAdapter();

        fab.attachToListView(listView.getWrappedList());
        fab.setVisibility(View.GONE);
        listView.setAnimExecutor(new AnimationExecutor());
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskObject.SingleTask singleTask = (TaskObject.SingleTask) mAdapter.getItem(position);
                if (singleTask.status == 1) {
                    mNeedUpdate = true;
                    Intent intent = new Intent(getActivity(), TaskAddActivity_.class);
                    intent.putExtra("mSingleTask", singleTask);
                    getParentFragment().startActivityForResult(intent, ListModify.RESULT_EDIT_LIST);
                }
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

        urlAll = String.format(createHost(mMembers.user.global_key, "/all"));

        setHasOptionsMenu(true);

        loadData();
    }

    @Click
    void fab() {
        action_add();
    }

    private void loadData() {
        getNextPageNetwork(urlAll, urlAll);

        if (mUpdateAll) {
            if (mProjectObject.id.isEmpty()) {
                getNetwork(urlTaskCountMy, urlTaskCountMy);
            } else {
                String url = String.format(urlTaskCountProject, mProjectObject.id);
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

    boolean mUpdateAll = true;

    String urlAll = "";

    final String urlTaskCountProject = Global.HOST + "/api/project/%s/task/user/count";
    final String urlTaskCountMy = Global.HOST + "/api/tasks/projects/count";
    public static final String hostTaskDelete = Global.HOST + "/api/user/%s/project/%s/task/%s";
    final String URL_TASK_SATUS = Global.HOST + "/api/task/%s/status";

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

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
                    if (task.status == 1) {
                        ++mSectionId;
                    }
                }

                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

            } else {
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
            }

            mUpdateAll = false;
            mAdapter.notifyDataSetChanged();
        } else if (tag.equals(urlTaskCountMy)) {
            if (code == 0) {
                JSONArray array = respanse.getJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    if (item.getString("project").equals(mProjectObject.id)) {
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
                    if (item.getString("user").equals(mMembers.id)) {
                        mTaskCount[0] = item.getInt("processing");
                        mTaskCount[1] = item.getInt("done");
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

        } else if (tag.equals(hostTaskDelete)) {
            if (code == 0) {
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
        String url = String.format(hostTaskDelete, task.project.owner_user_name, task.project.name, task.id);
        deleteNetwork(url, hostTaskDelete, pos, null);
    }

    static class TaskParam {
        TaskObject.SingleTask mTask;
        int mStatus;

        TaskParam(TaskObject.SingleTask mTask, int mStatus) {
            this.mTask = mTask;
            this.mStatus = mStatus;
        }
    }

    void statusTask(final int pos, final String id, final boolean complete) {
        RequestParams params = new RequestParams();
        int completeStatus = complete ? 2 : 1;
        params.put("status", completeStatus); // 任务完成2，任务正在进行1
        putNetwork(String.format(URL_TASK_SATUS, id), params, URL_TASK_SATUS, new TaskParam(mData.get(pos), completeStatus));
    }

    TestBaseAdapter mAdapter;

    public class TestBaseAdapter extends BaseAdapter implements
            StickyListHeadersAdapter, SectionIndexer {

        public TestBaseAdapter() {
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
                holder.mName = (TextView) convertView.findViewById(R.id.name);
                holder.mTime = (TextView) convertView.findViewById(R.id.time);
                holder.mDiscuss = (TextView) convertView.findViewById(R.id.discuss);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final TaskObject.SingleTask data = (TaskObject.SingleTask) getItem(position);
            holder.mTitle.setText(data.content);

            holder.mName.setText(data.creator.name);
            holder.mTime.setText(Global.dayToNow(data.created_at));
            holder.mDiscuss.setText(String.valueOf(data.comments));
            iconfromNetwork(holder.mIcon, data.owner.avatar);

            final int pos = position;

            holder.mCheckBox.setOnCheckedChangeListener(null);
            if (data.status == 1) {
                holder.mCheckBox.setChecked(false);
            } else {
                holder.mCheckBox.setChecked(true);
            }

            final int checkboxButton[] = new int[]{
                    R.drawable.checkbox_task_0,
                    R.drawable.checkbox_task_1,
                    R.drawable.checkbox_task_2,
                    R.drawable.checkbox_task_3,
            };

            holder.mCheckBox.setButtonDrawable(checkboxButton[data.priority]);


            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    statusTask(pos, data.id, isChecked);
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
//            String title = String.format(task_titles[type], mTaskCount[type]);
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

            TextView mName;
            TextView mTime;
            TextView mDiscuss;
        }
    }

    WeakHashMap<View, Integer> mOriginalViewHeightPool = new WeakHashMap<View, Integer>();

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
