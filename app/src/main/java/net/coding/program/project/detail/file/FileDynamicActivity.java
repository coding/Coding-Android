package net.coding.program.project.detail.file;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.FileUtil;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.BaseComment;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.PostRequest;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.merge.CommentActivity;
import net.coding.program.project.detail.merge.CommentActivity_;
import net.coding.program.task.add.CommentHolder;
import net.coding.program.task.add.TaskListHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_file_dynamic)
//@OptionsMenu(R.menu.menu_file_dynamic)
public class FileDynamicActivity extends BackActivity {

    public static final int RESULT_COMMENT = 1;

    public static final String TAG_HTTP_FILE_DYNAMIC = "TAG_HTTP_FILE_DYNAMIC";
    private static final String TAG_HTTP_COMMENT_DELETE = "TAG_HTTP_COMMENT_DELETE";

    DynamicFileAdapter adapter;
    ArrayList<Object> mData = new ArrayList<>();
    @ViewById
    ListView listView;
    @Extra
    ProjectFileParam mProjectFileParam;

    @AfterViews
    protected void initFileDynamicActivity() {
        adapter = new DynamicFileAdapter(this, 0, mData);
        listView.setAdapter(adapter);

        getNetwork(mProjectFileParam.getHttpDynamic(), TAG_HTTP_FILE_DYNAMIC);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HTTP_FILE_DYNAMIC)) {
            if (code == 0) {
                JSONArray json = respanse.getJSONArray("data");
                for (int i = 0; i < json.length(); ++i) {
                    JSONObject jsonItem = json.getJSONObject(i);
                    String targetType = jsonItem.optString("target_type");

                    Object dynamic = null;
                    if (targetType.equals("ProjectFile")) {
                        dynamic = new DynamicObject.DynamicProjectFile(jsonItem);
                    } else if (targetType.equals("ProjectFileComment")) {
                        dynamic = new BaseComment(new DynamicObject.DynamicProjectFileComment(jsonItem));
                    }

                    if (dynamic != null) {
                        mData.add(dynamic);
                    }
                }
                adapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals("TAG_HTTP_COMMENT_DELETE")) {
            if (code == 0) {
                mData.remove(data);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Click
    protected void itemAddComment() {
        FileDynamicParam param = new FileDynamicParam(mProjectFileParam.getProject(),
                Integer.valueOf(mProjectFileParam.mFileObject.file_id), "");
        CommentActivity_.intent(this).mParam(param).startForResult(RESULT_COMMENT);
    }

    @OnActivityResult(RESULT_COMMENT)
    void onResultComment(int result, Intent data) {
        if (result == RESULT_OK && data != null) {
            BaseComment comment = (BaseComment) data.getSerializableExtra("data");
            mData.add(comment);
            adapter.notifyDataSetChanged();
        }
    }

    static class FileDynamicParam implements CommentActivity.CommentParam, Serializable {

        int fileId;
        String atSomeOne;
        ProjectObject mProjectObject;

        public FileDynamicParam(ProjectObject projectObject, int fileId, String atSomeOne) {
            this.mProjectObject = projectObject;
            this.fileId = fileId;
            this.atSomeOne = atSomeOne;
        }

        @Override
        public PostRequest getSendCommentParam(String input) {
            String url = String.format(Global.HOST_API +
                    mProjectObject.getProjectPath() +
                    "/files/%d/comment", fileId);
            PostRequest request = new PostRequest(url, new RequestParams());
            request.setContent(input);
            return request;
        }

        @Override
        public String getAtSome() {
            return atSomeOne;
        }

        @Override
        public String getAtSomeUrl() {
            return String.format(Global.HOST_API +
                    "/user/relationships/context?context_type=project_file_comment&item_id=%d", mProjectObject.getId());
        }

        @Override
        public String getProjectPath() {
            return mProjectObject.getProjectPath();
        }

        @Override
        public boolean isPublicProject() {
            return false;
        }
    }

    public static class ProjectFileParam implements Serializable {

        private AttachmentFileObject mFileObject;
        //        private int mProjectid;
        private ProjectObject mProject;

        public ProjectFileParam(AttachmentFileObject fileObject, ProjectObject project) {
            mFileObject = fileObject;
            mProject = project;
        }

        public String getProjectPath() {
            return mProject.getProjectPath();
        }

        public int getProjectId() {
            return mProject.getId();
        }

        public int getFileId() {
            return Integer.valueOf(mFileObject.file_id);
        }

        public String getHttpDynamic() {
            String url = Global.HOST_API + mProject.getProjectPath() + "/file/%s/activities?last_id=9999999";
            return String.format(url, mFileObject.file_id);
        }

        public String getHttpDeleteComment(int commmentId) {
            String url = Global.HOST_API + mProject.getProjectPath() + "/files/%s/comment/%d";
            return String.format(url, mFileObject.file_id, commmentId);
        }

        public PostRequest getHttpEditFile(String content) {
            final String template = Global.HOST_API + getProjectPath() + "/files/%s/edit";
            String url = String.format(template, mFileObject.file_id);
            RequestParams params = new RequestParams();
            params.put("name", mFileObject.getName());
            params.put("content", content);
            return new PostRequest(url, params);
        }

        public String getHtttpFileView() {
            String url = Global.HOST_API + mProject.getProjectPath() + "/files/%s/view";
            return String.format(url, mFileObject.file_id);
        }

        public ProjectObject getProject() {
            return mProject;
        }

        public AttachmentFileObject getFileObject() {
            return mFileObject;
        }

        public void setFileObject(AttachmentFileObject fileObject) {
            mFileObject = fileObject;
        }

        public File getLocalFile(String path) {
            if (mFileObject == null || mProject == null) {
                return null;
            }

            return FileUtil.getDestinationInExternalPublicDir(path,
                    mFileObject.getSaveName(mProject.getId()));
        }
    }

    class DynamicFileAdapter extends ArrayAdapter<Object> {

        final int TYPE_DYNAMIC = 0;
        final int TYPE_COMMENT = 1;
        private final MyImageGetter myImageGetter = new MyImageGetter(getContext());
        private final ClickSmallImage onClickImage = new ClickSmallImage(FileDynamicActivity.this);
        private View.OnClickListener mOnClickComment = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Object tagData = v.getTag(R.layout.activity_task_comment_much_image_task);
                int itemId = 0;
                String ownerName = "";

                String globalKey = "";
                if (tagData instanceof BaseComment) {
                    BaseComment comment = (BaseComment) tagData;
                    itemId = comment.id;
                    globalKey = comment.owner.global_key;
                    ownerName = comment.owner.name;
                } else if (tagData instanceof DynamicObject.DynamicProjectFileComment) {
                    DynamicObject.DynamicProjectFileComment commentDynamic = (DynamicObject.DynamicProjectFileComment) tagData;
                    DynamicObject.ProjectFileComment comment = commentDynamic.getProjectFileComment();
                    itemId = comment.getId();
                    globalKey = comment.getOwnerGlobalKey();
                    ownerName = comment.getOwnerName();
                }

                final int itemIdFinal = itemId;
                if (globalKey.equals(MyApp.sUserObject.global_key)) {
                    showDialog("评论", "删除评论？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteNetwork(mProjectFileParam.getHttpDeleteComment(itemIdFinal), TAG_HTTP_COMMENT_DELETE, tagData);
                        }
                    });
                } else {
                    FileDynamicParam param = new FileDynamicParam(mProjectFileParam.getProject(),
                            Integer.valueOf(mProjectFileParam.mFileObject.file_id), ownerName);
                    CommentActivity_.intent(FileDynamicActivity.this).mParam(param).startForResult(RESULT_COMMENT);
                }
            }
        };

        public DynamicFileAdapter(Context context, int resource, List<Object> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getItemViewType(int position) {
            Object data = getItem(position);
            if (data instanceof BaseComment) {
                return TYPE_COMMENT;
            } else {
                DynamicObject.DynamicBaseObject item = (DynamicObject.DynamicBaseObject) data;
                if (item.target_type.equals("ProjectFileComment")) {
                    return TYPE_COMMENT;
                } else {
                    return TYPE_DYNAMIC;
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int count = getCount();
            if (getItemViewType(position) == TYPE_COMMENT) {
                CommentHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.activity_task_comment_much_image_task, parent, false);
                    holder = new CommentHolder(convertView, mOnClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
                    convertView.setTag(R.id.layout, holder);
                } else {
                    holder = (CommentHolder) convertView.getTag(R.id.layout);
                }

                Object data = getItem(position);
                holder.setContent(data);
                holder.updateLine(position, count);
                convertView.setTag(R.layout.activity_task_comment_much_image_task, data);

                return convertView;

            } else {
                TaskListHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.task_list_item_dynamic, parent, false);
                    holder = new TaskListHolder(convertView);
                } else {
                    holder = (TaskListHolder) convertView.getTag(TaskListHolder.getTagId());
                }

                DynamicObject.DynamicProjectFile data = (DynamicObject.DynamicProjectFile) getItem(position);
                convertView.setTag(R.layout.activity_task_comment_much_image_task, data);

                String content;
                int resId;
                switch (data.action) {
//                    case "create":
//                        content = "创建了文件";
//                        resId = R.drawable.project_file_dynamic_delete;
//                        break;
                    case "update":
                        content = "更新了文件";
                        resId = R.drawable.project_file_dynamic_edit;
                        break;
                    case "upload_file":
                        content = "上传了新版本";
                        resId = R.drawable.project_file_dynamic_upload;
                        break;
                    case "delete_history":
                        content = "删除了版本";
                        resId = R.drawable.project_file_dynamic_delete;
                        break;
                    case "move_file":
                        content = "移动了文件";
                        resId = R.drawable.project_file_dynamic_move;
                        break;

                    default:
                        content = data.action_msg + "文件";
                        resId = R.drawable.project_file_dynamic_edit;
                        break;
                }

                content = data.user.name + " " + content;
                holder.mContent.setText(content);
                holder.mIcon.setImageResource(resId);

                holder.updateLine(position, count);

                return convertView;
            }
        }
    }
}
