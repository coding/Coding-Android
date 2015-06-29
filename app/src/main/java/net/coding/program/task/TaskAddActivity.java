package net.coding.program.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
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
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.CommentBackup;
import net.coding.program.common.DatePickerFragment;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.enter.ImageCommentLayout;
import net.coding.program.common.photopick.ImageInfo;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.TaskObject;
import net.coding.program.model.UserObject;
import net.coding.program.project.detail.MembersActivity_;
import net.coding.program.project.detail.TaskListFragment;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@EActivity(R.layout.activity_task_add)
public class TaskAddActivity extends BaseActivity implements StartActivity, DatePickerFragment.DateSet {

    public static final String RESULT_GLOBARKEY = "RESULT_GLOBARKEY";
    public static final int RESULT_REQUEST_SELECT_USER = 3;
    public static final int RESULT_REQUEST_FOLLOW = 1002;
    public static final int RESULT_REQUEST_DESCRIPTION = 4;
    public static final int RESULT_REQUEST_DESCRIPTION_CREATE = 5;
    final String HOST_COMMENT_ADD = Global.HOST + "/api/task/%s/comment";
    final int priorityDrawable[] = new int[]{
            R.drawable.ic_task_priority_0,
            R.drawable.ic_task_priority_1,
            R.drawable.ic_task_priority_2,
            R.drawable.ic_task_priority_3
    };
    final String HOST_TASK_ADD = Global.HOST + "/api%s/task";

    final String HOST_FORMAT_TASK_COMMENT = Global.HOST + "/api/activity/task/%s?last_id=9999999";
    final String HOST_TASK_UPDATE = Global.HOST + "/api/task/%s/update";
    final String TAG_TASK_UPDATE = "TAG_TASK_UPDATE";
    final String hostDeleteComment = Global.HOST + "/api/task/%s/comment/%s";
    final String tagTaskDetail = "tagTaskDetail";
    final String HOST_PREVIEW = Global.HOST + "/api/markdown/preview";
    private final MyImageGetter myImageGetter = new MyImageGetter(TaskAddActivity.this);
    private final ClickSmallImage onClickImage = new ClickSmallImage(this);
    @ViewById
    protected View blankLayout;
    @Extra
    TaskObject.SingleTask mSingleTask;
    @Extra
    UserObject mUserOwner;
    @Extra
    TaskJumpParams mJumpParams;
    @ViewById
    ListView listView;
    View mHeadView;
    EditText title;
    ImageView circleIcon;
    TextView name;
    TextView status;
    LinearLayout linearlayout2;
    LinearLayout linearlayout3;
    TextView priority;
    TextView deadline;
    TextView description;
    ViewGroup descriptionLayout;
    TextView descriptionButton;
    TaskObject.TaskDescription descriptionData = new TaskObject.TaskDescription();
    TaskObject.TaskDescription descriptionDataNew = new TaskObject.TaskDescription();
    @StringArrayRes
    String strings_priority[];
    TaskParams mNewParam;
    TaskParams mOldParam;
    ImageCommentLayout mEnterComment;
    String HOST_DESCRIPTER = Global.HOST + "/api/task/%s/description";
    MenuItem mMenuSave;
    String urlComments = "";
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            requestTaskFromNetwork();
        }
    };
    StatusBaseAdapter mStatusAdapter;
    PriorityAdapter mPriorityAdapter;
    ArrayList<DynamicObject.DynamicTask> mData = new ArrayList<>();
    HashMap<String, String> mSendedImages = new HashMap<>();
    String tagUrlCommentPhoto = "";
    // 发评论
    View.OnClickListener mOnClickSendText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendCommentAll();
        }
    };
    boolean mTaskNoFound = false;
    private View.OnClickListener mOnClickComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final TaskObject.TaskComment comment = (TaskObject.TaskComment) v.getTag();
            if (comment.isMy()) {
                showDialog("任务", "删除评论？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = String.format(hostDeleteComment, comment.taskId, comment.id);
                        deleteNetwork(url, hostDeleteComment, comment.id);
                    }
                });
            } else {
                EnterLayout mEnterLayout = mEnterComment.getEnterLayout();
                mEnterLayout.content.setTag(comment);
                String format = "回复 %s";
                mEnterLayout.content.setHint(String.format(format, comment.owner.name));

                mEnterLayout.restoreLoad(comment);

                mEnterLayout.popKeyboard();
            }
        }
    };

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
        public int getItemViewType(int position) {
            DynamicObject.DynamicTask task = mData.get(position);
            if (task.target_type.equals("TaskComment")) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int count = getCount();
            if (getItemViewType(position) == 0) {
                CommentHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.activity_task_comment_much_image_task, parent, false);
                    holder = new CommentHolder(convertView, mOnClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
                    convertView.setTag(R.id.layout, holder);

                } else {
                    holder = (CommentHolder) convertView.getTag(R.id.layout);
                }

                TaskObject.TaskComment data = mData.get(position).getTaskComment();
                holder.setContent(data);

                holder.updateLine(position, count);

                return convertView;
            } else {
                TaskListHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.task_list_item_dynamic, parent, false);
                    holder = new TaskListHolder();
                    holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.mContent = (TextView) convertView.findViewById(R.id.content);
                    holder.timeLineUp = convertView.findViewById(R.id.timeLineUp);
                    holder.timeLineDown = convertView.findViewById(R.id.timeLineDown);
                    convertView.setTag(R.id.layout, holder);
                } else {
                    holder = (TaskListHolder) convertView.getTag(R.id.layout);
                }

                DynamicObject.DynamicTask data = mData.get(position);

                holder.mContent.setText(data.dynamicTitle());

                int iconResId = R.drawable.ic_task_dynamic_update;
                try {
                    String resName = "ic_task_dynamic_" + data.action;
                    Field field = R.drawable.class.getField(resName);
                    iconResId = Integer.parseInt(field.get(null).toString());
                } catch (Exception e) {
                    Global.errorLog(e);
                }
                holder.mIcon.setImageResource(iconResId);

                holder.updateLine(position, count);

                return convertView;
            }
        }
    };
    private View.OnClickListener onClickCreateDescription = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TaskDescriptionActivity_
                    .intent(TaskAddActivity.this)
                    .taskId(0)
                    .projectId(mSingleTask.project.getId())
                    .descriptionData(descriptionDataNew)
                    .startForResult(RESULT_REQUEST_DESCRIPTION_CREATE);
        }
    };

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initControl();

        if (mJumpParams == null) {
            initData();
        } else { // 跳转过来的，要先取得任务数据
            requestTaskFromNetwork();
        }
    }

    private void requestTaskFromNetwork() {
        final String hostTaskDetail = Global.HOST + "/api/user/%s/project/%s/task/%s";
        String url = String.format(hostTaskDetail, mJumpParams.userKey, mJumpParams.projectName, mJumpParams.taskId);
        getNetwork(url, tagTaskDetail);
        showDialogLoading();
    }

    private void initControl() {
        mEnterComment = new ImageCommentLayout(this, mOnClickSendText, getImageLoad());

        // 单独提出来是因为弹出软键盘时，由于head太长，导致 title 会被顶到消失，现在的解决方法是 edit作为一个单独的head加载
        View headEdit = mInflater.inflate(R.layout.activity_task_add_head_edit, null);
        title = (EditText) headEdit.findViewById(R.id.title);
        listView.addHeaderView(headEdit);

        mHeadView = mInflater.inflate(R.layout.activity_task_add_head, null);
        circleIcon = (ImageView) mHeadView.findViewById(R.id.circleIcon);
        name = (TextView) mHeadView.findViewById(R.id.name);
        status = (TextView) mHeadView.findViewById(R.id.status);
        priority = (TextView) mHeadView.findViewById(R.id.priority);
        linearlayout2 = (LinearLayout) mHeadView.findViewById(R.id.linearlayout2);
        linearlayout3 = (LinearLayout) mHeadView.findViewById(R.id.linearlayout3);
        deadline = (TextView) mHeadView.findViewById(R.id.deadline);
        descriptionLayout = (ViewGroup) mHeadView.findViewById(R.id.descriptionLayout);
        description = (TextView) mHeadView.findViewById(R.id.description);
        descriptionButton = (TextView) mHeadView.findViewById(R.id.descriptionButton);
        listView.addHeaderView(mHeadView);
    }

    private void updateSendButton() {
        if (title.getText().toString().isEmpty()
                || isContentUnmodify()) {
            enableSendButton(false);
        } else {
            enableSendButton(true);
        }
    }

    private boolean isContentUnmodify() {
        return ((mNewParam != null && mNewParam.equals(mOldParam)) && !descripChange()) ||
                mTaskNoFound;
    }

    private boolean descripChange() {
        return !mSingleTask.isEmpty() &&
                !descriptionData.markdown.equals(descriptionDataNew.markdown);
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
        if (mSingleTask.isEmpty()) {
            if (mUserOwner.id == 0) {
                mSingleTask.owner = AccountInfo.loadAccount(this);
            } else {
                mSingleTask.owner = mUserOwner;
            }
            mSingleTask.owner_id = mSingleTask.owner.id;
            mSingleTask.priority = 1; // 默认优先级是 1：正常处理
        }

        invalidateOptionsMenu();

        mNewParam = new TaskParams(mSingleTask);
        mOldParam = new TaskParams(mSingleTask);

        mEnterComment.getEnterLayout().content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_FOLLOW, mSingleTask.project));

        setHeadData();
        listView.setAdapter(commentAdpter);
        mStatusAdapter = new StatusBaseAdapter();
        mPriorityAdapter = new PriorityAdapter();

        selectMember();

        if (mSingleTask.isEmpty()) {
            getSupportActionBar().setTitle("新建任务");
            status.setText("未完成");
            linearlayout2.setVisibility(View.GONE);
            mEnterComment.hide();

            findViewById(R.id.line2_comment_off).setVisibility(View.VISIBLE);
            findViewById(R.id.line2_comment_on).setVisibility(View.GONE);

        } else {
            getSupportActionBar().setTitle(mSingleTask.project.name);
            title.setText(mSingleTask.content);

            setStatus();
            setPriority();
            linearlayout2.setVisibility(View.VISIBLE);

            findViewById(R.id.line2_comment_off).setVisibility(View.GONE);
            findViewById(R.id.line2_comment_on).setVisibility(View.VISIBLE);

        }

        setDeadline();

        initDescription();

        // 获取任务的评论
        if (!mSingleTask.isEmpty()) {
            urlComments = String.format(HOST_FORMAT_TASK_COMMENT, mSingleTask.getId());
            updateDynamicFromNetwork();
        }

        if (!mSingleTask.isEmpty()) {
            CommentBackup.BackupParam param = new CommentBackup.BackupParam(CommentBackup.Type.Task, mSingleTask.getId(), 0);
            EnterLayout mEnterLayout = mEnterComment.getEnterLayout();
            mEnterLayout.content.setTag(param);
            mEnterLayout.restoreLoad(param);
        }

        TextView time = (TextView) mHeadView.findViewById(R.id.time);
        TextView createName = (TextView) mHeadView.findViewById(R.id.createrName);
        if (mSingleTask.isEmpty()) {
            createName.setText(mNewParam.owner.name);
            time.setText("现在");
        } else {
            createName.setText(mSingleTask.creator.name);
            time.setText(Global.dayToNow(mSingleTask.created_at));
        }
    }

    private void updateDynamicFromNetwork() {
        getNetwork(urlComments, HOST_FORMAT_TASK_COMMENT);
    }

    private void initDescription() {
//        if (mSingleTask.isEmpty()) {
//            descriptionLayout.setOnClickListener(onClickCreateDescription);
//        } else {
//            if (mSingleTask.has_description) {
//                description.setText("载入备注中...");
//                HOST_DESCRIPTER = String.format(HOST_DESCRIPTER, mSingleTask.getCreateTime());
//                getNetwork(HOST_DESCRIPTER, HOST_DESCRIPTER);
//            } else {
//                descriptionLayout.setOnClickListener(onClickCreateDescription);
//            }
//        }
        if (mSingleTask.isEmpty()) {
            descriptionButtonUpdate(false);
            descriptionButton.setOnClickListener(onClickCreateDescription);
        } else {
            if (mSingleTask.has_description) {
                descriptionButtonUpdate(true);
                HOST_DESCRIPTER = String.format(HOST_DESCRIPTER, mSingleTask.getId());
                getNetwork(HOST_DESCRIPTER, HOST_DESCRIPTER);
            } else {
                descriptionButtonUpdate(false);
                descriptionButton.setOnClickListener(onClickCreateDescription);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mTaskNoFound) {
            MenuInflater menuInflater = getMenuInflater();
            if (mJumpParams == null && mSingleTask.isEmpty()) {
                menuInflater.inflate(R.menu.task_add, menu);
            } else {
                menuInflater.inflate(R.menu.task_add_edit, menu);
                if (mSingleTask != null && !mSingleTask.isEmpty() && !mSingleTask.creator.isMe()) {
                    menu.findItem(R.id.action_delete).setVisible(false);
                }
            }
            mMenuSave = menu.findItem(R.id.action_save);
            updateSendButton();
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void deleteTask() {
        showDialog("任务", "删除任务？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TaskObject.SingleTask task = mSingleTask;
                String url = String.format(TaskListFragment.hostTaskDelete, task.project.owner_user_name, task.project.name, task.getId());
                deleteNetwork(url, TaskListFragment.hostTaskDelete);
                showProgressBar(true, "删除任务中...");
            }
        });
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

        linearlayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();

                Bundle bundle = new Bundle();
                bundle.putString("date", mNewParam.deadline);
                bundle.putBoolean("clear", true);
                newFragment.setArguments(bundle);

                newFragment.setCancelable(true);
                newFragment.show(getSupportFragmentManager(), "datePicker");
                getSupportFragmentManager().executePendingTransactions();
                dialogTitleLineColor(newFragment.getDialog());
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
    }

    private void setStatus() {
        if (mNewParam.status == 1) {
            status.setText("未完成");
        } else {
            status.setText("已完成");
        }
    }

    public void dateSetResult(String date, boolean clear) {
        if (clear) {
            mNewParam.deadline = "";
        } else {
            mNewParam.deadline = date;
        }

        setDeadline();
        updateSendButton();
    }

    private void setPriority() {
        priority.setText(strings_priority[mNewParam.priority]);
    }

    private void setDeadline() {
        String s = mNewParam.deadline;
        if (s.isEmpty()) {
            s = "未指定";
        }

        deadline.setText(s);
    }

    private void setDescription() {
        Global.MessageParse parseData = HtmlContent.parseReplacePhoto(descriptionDataNew.description);
        description.setText(Html.fromHtml(parseData.text, myImageGetter, Global.tagHandler));
    }

    @OptionsItem
    void action_save() {
        String content = title.getText().toString();
        if (EmojiFilter.containsEmoji(this, content)) {
            return;
        }

        if (mSingleTask.isEmpty()) {
            String url = String.format(HOST_TASK_ADD, mSingleTask.project.backend_project_path);
            RequestParams params = new RequestParams();
            params.put("content", content);
            params.put("status", mNewParam.status);
            params.put("priority", mNewParam.priority);
            params.put("owner_id", mNewParam.ownerId);
            params.put("deadline", mNewParam.deadline);
            if (!descriptionDataNew.markdown.isEmpty()) {
                params.put("description", descriptionDataNew.markdown);
            }
            postNetwork(url, params, HOST_TASK_ADD);
            showProgressBar(R.string.create_task_ing);

        } else {
            String url = String.format(HOST_TASK_UPDATE, mSingleTask.getId());
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
            if (mNewParam.ownerId != (mSingleTask.owner_id)) {
                params.put("owner_id", mNewParam.ownerId);
            }
            if (!mNewParam.deadline.equals(mSingleTask.deadline)) {
                params.put("deadline", mNewParam.deadline);
            }

            String oldData = descriptionData.markdown;
            if (oldData != null && !oldData.equals(descriptionDataNew.markdown)) {
                params.put("description", descriptionDataNew.markdown);
            }

            putNetwork(url, params, TAG_TASK_UPDATE);
            showProgressBar(R.string.modify_task_ing);
        }
    }

    @OptionsItem
    void action_copy() {
        final String urlTemplate = Global.HOST + "/u/%s/p/%s/task/%d";
        String url = String.format(urlTemplate, mSingleTask.project.owner_user_name, mSingleTask.project.name, mSingleTask.getId());
        Global.copy(this, url);
        showButtomToast("已复制 " + url);
    }

    @OptionsItem
    void action_delete() {
        deleteTask();
    }

    void selectMember() {
        name.setText(mNewParam.owner.name);
        ImageLoader.getInstance().displayImage(Global.makeSmallUrl(circleIcon, mNewParam.owner.avatar), circleIcon);
    }

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
            showProgressBar(false);
            if (code == 0) {
                closeActivity("新建任务成功");
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_FORMAT_TASK_COMMENT)) {
            if (code == 0) {
                mData.clear();
                JSONArray jsonArray = respanse.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    mData.add(new DynamicObject.DynamicTask(jsonArray.getJSONObject(i)));
                }
                commentAdpter.notifyDataSetChanged();

                updateDynamicFromNetwork();

            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_COMMENT_ADD)) {
            showProgressBar(false);
            if (code == 0) {

                EnterLayout mEnterLayout = mEnterComment.getEnterLayout();
                mEnterLayout.restoreDelete(data);

                mEnterLayout.clearContent();
                mEnterLayout.hideKeyboard();
                mEnterLayout.content.setHint("");
                mEnterLayout.content.setTag(null);
                mEnterComment.clearContent();

                commentAdpter.notifyDataSetChanged();
                updateDynamicFromNetwork();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(hostDeleteComment)) {
            if (code == 0) {
                int commentId = (int) data;
                for (int i = 0; i < mData.size(); ++i) {
                    if (mData.get(i).id == (commentId)) {
                        mData.remove(i);
                        break;
                    }
                }
                commentAdpter.notifyDataSetChanged();
                showButtomToast("删除成功");
                updateDynamicFromNetwork();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_TASK_UPDATE)) {
            showProgressBar(false);
            if (code == 0) {
                closeActivity("修改任务成功");
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TaskListFragment.hostTaskDelete)) {
            showProgressBar(false);
            if (code == 0) {
                closeActivity("删除任务成功");
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(tagTaskDetail)) {
            if (code == 0) {
                mSingleTask = new TaskObject.SingleTask(respanse.getJSONObject("data"));
                initData();

                BlankViewDisplay.setBlank(1, this, true, blankLayout, onClickRetry);
            } else {
                showErrorMsg(code, respanse);
                if (code == 1600) {
                    mEnterComment.hide();
                    mTaskNoFound = true;
                    invalidateOptionsMenu();
                    BlankViewDisplay.setBlank(0, this, false, blankLayout, onClickRetry, "任务不存在");
                } else {
                    BlankViewDisplay.setBlank(0, this, false, blankLayout, onClickRetry);
                }
            }
            hideProgressDialog();

        } else if (tag.equals(HOST_DESCRIPTER)) {
            if (code == 0) {
                descriptionData = new TaskObject.TaskDescription(respanse.getJSONObject("data"));
                descriptionDataNew = new TaskObject.TaskDescription(descriptionData);
                setDescription();

                descriptionButtonUpdate(false);
                descriptionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TaskDescriptionActivity_
                                .intent(TaskAddActivity.this)
                                .descriptionData(descriptionDataNew)
                                .taskId(mSingleTask.getId())
                                .projectId(mSingleTask.project.getId())
                                .startForResult(RESULT_REQUEST_DESCRIPTION);
                    }
                });

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_PREVIEW)) {
            if (code == 0) {
                descriptionDataNew.description = respanse.optString("data", "");
                setDescription();
            } else {
                showButtomToast("发生错误");
            }

            hideProgressDialog();
        } else if (tag.equals(tagUrlCommentPhoto)) {
            if (code == 0) {
                String fileUri;
                if (mSingleTask.project.isPublic()) {
                    fileUri = respanse.optString("data", "");
                } else {
                    AttachmentFileObject fileObject = new AttachmentFileObject(respanse.optJSONObject("data"));
                    fileUri = fileObject.owner_preview;
                }
                String mdPhotoUri = String.format("\n![图片](%s)", fileUri);
                mSendedImages.put((String) data, mdPhotoUri);
                sendCommentAll();
            } else {
                showErrorMsg(code, respanse);
                showProgressBar(false);
            }
        }
    }

    private void descriptionButtonUpdate(boolean loading) {
        if (!loading && descriptionDataNew.markdown.isEmpty()) {
            descriptionButton.setText("添加描述");
            descriptionButton.setTextColor(getResources().getColor(R.color.font_black_6));
        } else {
            descriptionButton.setText("查看描述");
            descriptionButton.setTextColor(getResources().getColor(R.color.font_green));
        }
    }

    @OnActivityResult(ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE)
    final void commentImage(int result, Intent data) {
        if (result == RESULT_OK) {
            mEnterComment.onActivityResult(
                    ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE,
                    data);
        }
    }

    @OnActivityResult(ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE_DETAIL)
    final void commentImageDetail(int result, Intent data) {
        if (result == RESULT_OK) {
            mEnterComment.onActivityResult(
                    ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE_DETAIL,
                    data);
        }
    }

    @OnActivityResult(RESULT_REQUEST_DESCRIPTION)
    void resultDescription(int result, Intent data) {
        if (result == RESULT_OK) {
            updateDescriptionFromResult(data);
            updateSendButton();
        }
    }

    @OnActivityResult(RESULT_REQUEST_DESCRIPTION_CREATE)
    void resultDescriptionCreate(int result, Intent data) {
        if (result == RESULT_OK) {
            updateDescriptionFromResult(data);
            updateSendButton();
        }
    }

    void updateDescriptionFromResult(Intent data) {
        descriptionDataNew.markdown = data.getStringExtra("data");

        RequestParams params = new RequestParams();
        params.put("content", descriptionDataNew.markdown);
        postNetwork(HOST_PREVIEW, params, HOST_PREVIEW);

        descriptionButtonUpdate(false);
    }

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
        } else if (requestCode == RESULT_REQUEST_FOLLOW) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                mEnterComment.getEnterLayout().insertText(name);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (!isContentUnmodify()) {
            showDialog("任务", "确定放弃此次编辑？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        } else {
            finish();
        }
    }

    private void popListSelectDialog(String title, BaseAdapter selectsAdapter, DialogInterface.OnClickListener clickList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskAddActivity.this);
        builder.setTitle(title)
                .setAdapter(selectsAdapter, clickList);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialogTitleLineColor(dialog);
    }

    private void sendComment(String input) {
        if (mSingleTask == null) {
            showButtomToast("发送评论失败");
            return;
        }

        EnterLayout mEnterLayout = mEnterComment.getEnterLayout();
        String s = input;

        if (EmojiFilter.containsEmptyEmoji(this, s)) {
            showProgressBar(false);
            return;
        }

        Object item = mEnterLayout.content.getTag();
        if (item != null && (item instanceof TaskObject.TaskComment)) {
            TaskObject.TaskComment comment = (TaskObject.TaskComment) item;
            s = Global.encodeInput(comment.owner.name, s);
        } else {
            s = Global.encodeInput("", s);
        }

        RequestParams params = new RequestParams();

        params.put("content", s);

        postNetwork(String.format(HOST_COMMENT_ADD, mSingleTask.getId()), params, HOST_COMMENT_ADD, 0, item);

        showProgressBar(R.string.sending_comment);
    }

    private void sendCommentAll() {
        showProgressBar(true);

        ArrayList<ImageInfo> photos = mEnterComment.getPickPhotos();
        for (ImageInfo item : photos) {
            String imagePath = item.path;
            if (!mSendedImages.containsKey(imagePath)) {
                try {
                    String url = mSingleTask.project.getHttpUploadPhoto();
                    RequestParams params = new RequestParams();
                    params.put("dir", 0);
                    Uri uri = Uri.parse(imagePath);
                    File file = new PhotoOperate(this).scal(uri);
                    params.put("file", file);
                    tagUrlCommentPhoto = imagePath; // tag必须不同，否则无法调用下一次
                    postNetwork(url, params, tagUrlCommentPhoto, 0, imagePath);
                    showProgressBar(true);
                } catch (Exception e) {
                    showProgressBar(false);
                }

                return;
            }
        }

        String send = mEnterComment.getEnterLayout().getContent();
        for (ImageInfo item : photos) {
            send += mSendedImages.get(item.path);
        }
        sendComment(send);
    }

    private static class TaskListHolder {
        View timeLineUp;
        View timeLineDown;
        ImageView mIcon;
        TextView mContent;

        public void updateLine(int position, int count) {
            switch (count) {
                case 1:
                    setLine(false, false);
                    break;

                default:
                    if (position == 0) {
                        setLine(false, true);
                    } else if (position == count - 1) {
                        setLine(true, false);
                    } else {
                        setLine(true, true);
                    }
                    break;
            }
        }

        private void setLine(boolean up, boolean down) {
            timeLineUp.setVisibility(up ? View.VISIBLE : View.INVISIBLE);
            timeLineDown.setVisibility(down ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private static class CommentHolder extends ImageCommentHolder {
        View timeLineUp;
        View timeLineDown;

        public CommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
            super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);

            timeLineUp = convertView.findViewById(R.id.timeLineUp);
            timeLineDown = convertView.findViewById(R.id.timeLineDown);
        }

        public void updateLine(int position, int count) {
            switch (count) {
                case 1:
                    setLine(false, false);
                    break;

                default:
                    if (position == 0) {
                        setLine(false, true);
                    } else if (position == count - 1) {
                        setLine(true, false);
                    } else {
                        setLine(true, true);
                    }
                    break;
            }
        }

        private void setLine(boolean up, boolean down) {
            timeLineUp.setVisibility(up ? View.VISIBLE : View.INVISIBLE);
            timeLineDown.setVisibility(down ? View.VISIBLE : View.INVISIBLE);
        }
    }

    static class ViewHolder {
        View mIcon;
        ImageView mCheck;
        TextView mTitle;
    }

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

    private static class TaskParams {
        String content = "";
        int status;
        int ownerId;
        int priority;
        String deadline = "";

        UserObject owner;

        public TaskParams(TaskObject.SingleTask singleTask) {
            content = singleTask.content;
            status = singleTask.status;
            ownerId = singleTask.owner_id;
            priority = singleTask.priority;
            owner = singleTask.owner;
            deadline = singleTask.deadline;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TaskParams)) return false;

            TaskParams that = (TaskParams) o;

            if (ownerId != that.ownerId) return false;
            if (priority != that.priority) return false;
            if (status != that.status) return false;
            if (!content.equals(that.content)) return false;
            if (!deadline.equals(that.deadline)) return false;
            return owner.equals(that.owner);

        }

        @Override
        public int hashCode() {
            int result = content.hashCode();
            result = 31 * result + status;
            result = 31 * result + ownerId;
            result = 31 * result + priority;
            result = 31 * result + deadline.hashCode();
            result = 31 * result + owner.hashCode();
            return result;
        }
    }

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
}
