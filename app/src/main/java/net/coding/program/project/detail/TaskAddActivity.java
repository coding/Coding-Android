package net.coding.program.project.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.BaseActivity;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.comment.ImageCommentHolder;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.TaskObject;
import net.coding.program.model.UserObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_task_add)
public class TaskAddActivity extends BaseActivity implements StartActivity {

    @Extra
    TaskObject.SingleTask mSingleTask;

    @Extra
    UserObject mUserOwner;

    @Extra
    TaskJumpParams mJumpParams;

    public static class TaskJumpParams implements Serializable {
        public String userKey;
        public String projectName;
        public String taskId;

        public TaskJumpParams(String user, String project, String task) {
            userKey = user;
            projectName = project;
            taskId = task;
        }
    }

    @ViewById
    ListView listView;

    View mHeadView;
    EditText title;
    ImageView circleIcon;
    TextView name;
    TextView status;
    LinearLayout linearlayout2;
    TextView priority;

    @StringArrayRes
    String strings_priority[];
    private TextView commentCount;

    static class TaskParams {
        String content = "";
        int status;
        String ownerId = "";
        int priority;
        UserObject owner;

        public TaskParams(TaskObject.SingleTask singleTask) {
            content = singleTask.content;
            status = singleTask.status;
            ownerId = singleTask.owner_id;
            priority = singleTask.priority;
            owner = singleTask.owner;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskParams that = (TaskParams) o;

            if (priority != that.priority) return false;
            if (status != that.status) return false;
            if (content != null ? !content.equals(that.content) : that.content != null)
                return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            if (ownerId != null ? !ownerId.equals(that.ownerId) : that.ownerId != null)
                return false;

            return true;
        }
    }

    TaskParams mNewParam;
    TaskParams mOldParam;

    EnterLayout mEnterLayout;

    final String HOST_COMMENT_ADD = Global.HOST + "/api/task/%s/comment";

    // 发评论
    View.OnClickListener mOnClickSendText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = mEnterLayout.getContent();

            if (EmojiFilter.containsEmoji(s)) {
                showMiddleToast("暂不支持发表情");
                return;
            }

            Object item = mEnterLayout.content.getTag();
            if (item != null && (item instanceof TaskObject.TaskComment)) {
                TaskObject.TaskComment comment = (TaskObject.TaskComment) item;
                String at = String.format("@%s ", comment.owner.global_key);
                s = at + s;
            }

            RequestParams params = new RequestParams();
            params.put("content", s);
            params.put("extra", "");

            postNetwork(String.format(HOST_COMMENT_ADD, mSingleTask.id), params, HOST_COMMENT_ADD);
        }
    };

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        initControl();

        if (mJumpParams == null) {
            initData();
        } else { // 跳转过来的，要先取得任务数据
            final String hostTaskDetail = Global.HOST + "/api/user/%s/project/%s/task/%s";
            String url = String.format(hostTaskDetail, mJumpParams.userKey, mJumpParams.projectName, mJumpParams.taskId);
            getNetwork(url, tagTaskDetail);
            showDialogLoading();
        }
    }

    private void initControl() {
        mEnterLayout = new EnterLayout(this, mOnClickSendText, EnterLayout.Type.TextOnly);

        mHeadView = mInflater.inflate(R.layout.activity_task_add_head, null);
        title = (EditText) mHeadView.findViewById(R.id.title);
        circleIcon = (ImageView) mHeadView.findViewById(R.id.circleIcon);
        name = (TextView) mHeadView.findViewById(R.id.name);
        status = (TextView) mHeadView.findViewById(R.id.status);
        priority = (TextView) mHeadView.findViewById(R.id.priority);
        linearlayout2 = (LinearLayout) mHeadView.findViewById(R.id.linearlayout2);
        commentCount = (TextView) mHeadView.findViewById(R.id.commentCount);
        listView.addHeaderView(mHeadView);
    }

    private void updateSendButton() {
        if (title.getText().toString().isEmpty()
                || mNewParam.equals(mOldParam)) {
            enableSendButton(false);
        } else {
            enableSendButton(true);
        }
    }

    private void enableSendButton(boolean enable) {
        if (mMenuSave == null) {
            return;
        }
        if (enable) {
            mMenuSave.setIcon(R.drawable.ic_menu_ok);
            mMenuSave.setEnabled(true);
        } else {
            mMenuSave.setIcon(R.drawable.ic_menu_ok_unable);
            mMenuSave.setEnabled(false);
        }
    }

    void initData() {
        if (mSingleTask.id.isEmpty()) {
            if (mUserOwner.id.isEmpty()) {
                mSingleTask.owner = AccountInfo.loadAccount(this);
            } else {
                mSingleTask.owner = mUserOwner;
            }
            mSingleTask.owner_id = mSingleTask.owner.id;
            mSingleTask.priority = 1; // 默认优先级是 1：正常处理
        }
        mNewParam = new TaskParams(mSingleTask);
        mOldParam = new TaskParams(mSingleTask);

        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_FOLLOW, mSingleTask.project));

        setHeadData();
        addFoot();
        listView.setAdapter(commentAdpter);
        mStatusAdapter = new StatusBaseAdapter();
        mPriorityAdapter = new PriorityAdapter();

        selectMember();

        if (mSingleTask.id.isEmpty()) {
            status.setText("未完成");
            linearlayout2.setVisibility(View.GONE);
            mEnterLayout.hide();

            findViewById(R.id.layoutListHeadBottom).setVisibility(View.GONE);

        } else {
            title.setText(mSingleTask.content);
            getActionBar().setTitle("编辑任务");

            setStatus();
            setPriority();
            linearlayout2.setVisibility(View.VISIBLE);
        }

        urlComments = String.format(HOST_FORMAT_TASK_COMMENT, mSingleTask.id);
        getNextPageNetwork(urlComments, HOST_FORMAT_TASK_COMMENT);
    }

    MenuItem mMenuSave;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.task_add, menu);

        mMenuSave = menu.findItem(R.id.action_save);
        updateSendButton();

        return super.onCreateOptionsMenu(menu);
    }

    private void setHeadData() {
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNewParam.content = s.toString();
                updateSendButton();
            }
        });
        title.setText("");

        TextView time = (TextView) mHeadView.findViewById(R.id.time);
        TextView createName = (TextView) mHeadView.findViewById(R.id.createrName);

        View delete = mHeadView.findViewById(R.id.delete);
        if (mSingleTask.id.isEmpty()) {
            delete.setVisibility(View.GONE);
        } else {
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog("删除任务", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TaskObject.SingleTask task = mSingleTask;
                            String url = String.format(TaskListFragment.hostTaskDelete, task.project.owner_user_name, task.project.name, task.id);
                            deleteNetwork(url, TaskListFragment.hostTaskDelete);
                        }
                    });
                }
            });
        }

        mHeadView.findViewById(R.id.linearlayout1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MembersActivity_
                        .intent(TaskAddActivity.this)
                        .mProjectObjectId(mSingleTask.project_id)
                        .startForResult(RESULT_REQUEST_SELECT_USER);
            }
        });

        linearlayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popListSelectDialog(
                        "阶段",
                        mStatusAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    mNewParam.status = 1; // "未完成"
                                } else {
                                    mNewParam.status = 2;
                                }

                                setStatus();
                                updateSendButton();
                            }
                        });
            }
        });

        ViewGroup layout3 = (ViewGroup) mHeadView.findViewById(R.id.layout3);
        layout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popListSelectDialog("优先级",
                        mPriorityAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNewParam.priority = priorityDrawable.length - 1 - which;
                                setPriority();
                                updateSendButton();
                            }
                        });
            }
        });


        if (mSingleTask.id.isEmpty()) {
            createName.setText(mNewParam.owner.name);
            time.setText(String.format("现在"));
        } else {
            createName.setText(mSingleTask.creator.name);
            time.setText(String.format(Global.dayToNow(mSingleTask.created_at)));
        }

    }

    View listFoot;

    private void addFoot() {
        listFoot = mInflater.inflate(R.layout.task_comment_empty, null);
        listFoot.setVisibility(View.INVISIBLE);
        listView.addFooterView(listFoot);
    }

    final int priorityDrawable[] = new int[]{
            R.drawable.ic_task_priority_0,
            R.drawable.ic_task_priority_1,
            R.drawable.ic_task_priority_2,
            R.drawable.ic_task_priority_3
    };

    private void setStatus() {
        if (mNewParam.status == 1) {
            status.setText("未完成");
        } else {
            status.setText("已完成");
        }
    }

    private void setPriority() {
        priority.setText(strings_priority[mNewParam.priority]);
    }


    final String HOST_FORMAT_TASK_CONTENT = Global.HOST + "/api/user/%s/project/%s/task/%s";
    final String HOST_TASK_ADD = Global.HOST + "/api%s/task";
    final String HOST_TASK_DEL = Global.HOST + "/api/task/%s";
    final String HOST_FORMAT_TASK_COMMENT = Global.HOST + "/api/task/%s/comments?";

    final String HOST_TASK_UPDATE = Global.HOST + "/api/task/%s/update";
    final String TAG_TASK_UPDATE = "TAG_TASK_UPDATE";


    String urlComments = "";

    // 旧版的任务怎么跳转
    // https://coding.net/u/ease/p/Coding-iOS/tasks/user/8206503/all
    // https://coding.net/u/8206503/p/TestIt2/task/9206

    @OptionsItem
    void action_save() {
        String content = title.getText().toString();
        if (EmojiFilter.containsEmoji(content)) {
            showMiddleToast("暂不支持发表情");
            return;
        }

        if (mSingleTask.id.isEmpty()) {
            String url = String.format(HOST_TASK_ADD, mSingleTask.project.backend_project_path);
            RequestParams params = new RequestParams();
            params.put("content", content);
            params.put("status", mNewParam.status);
            params.put("priority", mNewParam.priority);
            params.put("owner_id", mNewParam.ownerId);
            postNetwork(url, params, HOST_TASK_ADD);

        } else {
            String url = String.format(HOST_TASK_UPDATE, mSingleTask.id);

//            PUT /api/task/{id}/update status priority owner_id content
            RequestParams params = new RequestParams();
            if (!content.equals(mSingleTask.content)) {
                params.put("content", content);
            }
            if (mNewParam.status != mSingleTask.status) {
                params.put("status", mNewParam.status);
            }
            if (mNewParam.priority != mSingleTask.priority) {
                params.put("priority", mNewParam.priority);
            }
            if (!mNewParam.ownerId.equals(mSingleTask.owner_id)) {
                params.put("owner_id", mNewParam.ownerId);
            }

            putNetwork(url, params, TAG_TASK_UPDATE);
        }
    }

    void selectMember() {
        name.setText(mNewParam.owner.name);
        ImageLoader.getInstance().displayImage(Global.makeSmallUrl(circleIcon, mNewParam.owner.avatar), circleIcon);
    }

    private void updateCommentCount() {
        int count = mData.size();
        String foramt = "%d 条评论";
        commentCount.setText(String.format(foramt, count));
        if (count == 0) {
            listFoot.setVisibility(View.VISIBLE);
            listFoot.setPadding(0, 0, 0, 0);
        } else {
            listFoot.setVisibility(View.INVISIBLE);
            listFoot.setPadding(0, -listFoot.getHeight(), 0, 0);
        }
    }

    final String hostDeleteComment = Global.HOST + "/api/task/%s/comment/%s";
    final String tagTaskDetail = "tagTaskDetail";

    public static final String RESULT_GLOBARKEY = "RESULT_GLOBARKEY";

    private void closeActivity(String msg) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_GLOBARKEY, mNewParam.owner.global_key);
        setResult(Activity.RESULT_OK, intent);
        showButtomToast(msg);
        finish();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_TASK_ADD)) {
            if (code == 0) {
                closeActivity("新建任务成功");
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_FORMAT_TASK_COMMENT)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    mData.add(new TaskObject.TaskComment(jsonArray.getJSONObject(i)));
                }
                commentAdpter.notifyDataSetChanged();

                updateCommentCount();

            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_COMMENT_ADD)) {
            if (code == 0) {
                TaskObject.TaskComment item = new TaskObject.TaskComment(respanse.getJSONObject("data"));
                mData.add(0, item);
                mEnterLayout.clearContent();
                mEnterLayout.hideKeyboard();
                mEnterLayout.content.setHint("");
                mEnterLayout.content.setTag(null);

                commentAdpter.notifyDataSetChanged();

                updateCommentCount();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(hostDeleteComment)) {
            if (code == 0) {
                String commentId = (String) data;
                for (int i = 0; i < mData.size(); ++i) {
                    if (mData.get(i).id.equals(commentId)) {
                        mData.remove(i);
                        break;
                    }
                }

                commentAdpter.notifyDataSetChanged();

                updateCommentCount();

                showButtomToast("删除成功");
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_TASK_UPDATE)) {
            if (code == 0) {
                closeActivity("修改任务成功");
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TaskListFragment.hostTaskDelete)) {
            if (code == 0) {
                closeActivity("删除任务成功");
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(tagTaskDetail)) {
            if (code == 0) {
                mSingleTask = new TaskObject.SingleTask(respanse.getJSONObject("data"));
                initData();
            } else {
                showErrorMsg(code, respanse);
            }
            hideProgressDialog();
        }
    }

    private PhotoOperate photoOperate = new PhotoOperate(this);

    public static final int RESULT_REQUEST_SELECT_USER = 3;
    public static final int RESULT_REQUEST_FOLLOW = 1002;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_SELECT_USER) {
            if (resultCode == Activity.RESULT_OK) {
                TaskObject.Members member = (TaskObject.Members) data.getSerializableExtra("members");
                mNewParam.ownerId = member.user.id;
                mNewParam.owner = member.user;
                selectMember();
                updateSendButton();
            }
        } else if (requestCode == RESULT_REQUEST_PHOTO) {
            // 这个版本暂时不支持发图片
            if (resultCode == Activity.RESULT_OK) {
            }

        } else if (requestCode == RESULT_REQUEST_FOLLOW) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                mEnterLayout.insertText(name);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    StatusBaseAdapter mStatusAdapter;

    public class StatusBaseAdapter extends BaseAdapter {

        String[] mData;

        public StatusBaseAdapter() {
            mData = getResources().getStringArray(R.array.task_status);
        }

        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public Object getItem(int position) {
            return mData[position];
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
                convertView = mInflater.inflate(R.layout.activity_task_status_list_item, parent, false);
                holder.mTitle = (TextView) convertView.findViewById(R.id.title);
                holder.mCheck = (ImageView) convertView.findViewById(R.id.check);
                holder.mIcon = convertView.findViewById(R.id.icon);
                holder.mIcon.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTitle.setText(mData[position]);
            if (position == getSelectPos())
                holder.mCheck.setVisibility(View.VISIBLE);
            else
                holder.mCheck.setVisibility(View.GONE);
            return convertView;
        }

        private int getSelectPos() {
            if (mNewParam.status == 1) {
                return 0;
            } else {
                return 1;
            }
        }

    }

    private void popListSelectDialog(String title, BaseAdapter selectsAdapter, DialogInterface.OnClickListener clickList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskAddActivity.this);
        builder.setTitle(title)
                .setAdapter(selectsAdapter, clickList);
        //builder.create().show();
        AlertDialog dialog = builder.create();
        dialog.show();
        dialogTitleLineColor(dialog);
    }

    class ViewHolder {
        View mIcon;
        ImageView mCheck;
        TextView mTitle;
    }

    PriorityAdapter mPriorityAdapter;

    class PriorityAdapter extends BaseAdapter {

        final int priorityDrawableInverse[] = new int[]{
                R.drawable.ic_task_priority_3,
                R.drawable.ic_task_priority_2,
                R.drawable.ic_task_priority_1,
                R.drawable.ic_task_priority_0
        };

        String[] mData;

        public PriorityAdapter() {
            mData = getResources().getStringArray(R.array.strings_priority_inverse);
        }

        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public Object getItem(int position) {
            return mData[position];
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
                convertView = mInflater.inflate(R.layout.activity_task_status_list_item, parent, false);
                holder.mTitle = (TextView) convertView.findViewById(R.id.title);
                holder.mCheck = (ImageView) convertView.findViewById(R.id.check);
                holder.mIcon = convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTitle.setText(mData[position]);
            holder.mIcon.setBackgroundResource(priorityDrawableInverse[position]);
            if (position == getSelectPos())
                holder.mCheck.setVisibility(View.VISIBLE);
            else
                holder.mCheck.setVisibility(View.GONE);
            return convertView;
        }

        private int getSelectPos() {
            return priorityDrawableInverse.length - 1 - mNewParam.priority;
        }
    }


    ArrayList<TaskObject.TaskComment> mData = new ArrayList<TaskObject.TaskComment>();

    BaseAdapter commentAdpter = new BaseAdapter() {
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
            ImageCommentHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_maopao_detail_item, parent, false);
                holder = new ImageCommentHolder(convertView, mOnClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
                convertView.setTag(R.id.layout, holder);

            } else {
                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
            }

            TaskObject.TaskComment data = mData.get(position);
            holder.setTaskCommentContent(data);

            return convertView;
        }
    };

    private final MyImageGetter myImageGetter = new MyImageGetter(TaskAddActivity.this);

    private final ClickSmallImage onClickImage = new ClickSmallImage(this);

    private View.OnClickListener mOnClickComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final TaskObject.TaskComment comment = (TaskObject.TaskComment) v.getTag();
            if (comment.isMy()) {
                showDialog("删除评论", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = String.format(hostDeleteComment, comment.taskId, comment.id);
                        deleteNetwork(url, hostDeleteComment, comment.id);
                    }
                });
            } else {
                mEnterLayout.content.setTag(comment);
                String format = "回复 %s";
                mEnterLayout.content.setHint(String.format(format, comment.owner.name));
                mEnterLayout.popKeyboard();
            }
        }
    };

}
