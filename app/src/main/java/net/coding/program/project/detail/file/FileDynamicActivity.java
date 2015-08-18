package net.coding.program.project.detail.file;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.PostRequest;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_file_dynamic)
//@OptionsMenu(R.menu.menu_file_dynamic)
public class FileDynamicActivity extends BackActivity {

    public static final int RESULT_COMMENT = 1;

    public static final String TAG_HTTP_FILE_DYNAMIC = "TAG_HTTP_FILE_DYNAMIC";
    DynamicFileAdapter adapter;
    ArrayList<DynamicObject.DynamicBaseObject> mData = new ArrayList<>();
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

                    DynamicObject.DynamicBaseObject dynamic = null;
                    if (targetType.equals("ProjectFile")) {
                        dynamic = new DynamicObject.DynamicProjectFile(jsonItem);
                    } else if (targetType.equals("ProjectFileComment")) {
                        dynamic = new DynamicObject.DynamicProjectFileComment(jsonItem);
                    }

                    if (dynamic != null) {
                        mData.add(dynamic);
                    }
                }
                adapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Click
    protected void itemAddComment() {
        FileDynamicParam param = new FileDynamicParam(mProjectFileParam.mProjectid,
                Integer.valueOf(mProjectFileParam.mFileObject.file_id));
        CommentActivity_.intent(this).mParam(param).startForResult(RESULT_COMMENT);
    }

    @OnActivityResult(RESULT_COMMENT)
    void onResultComment() {

    }

    static class FileDynamicParam implements CommentActivity.CommentParam, Serializable {

        int projectId;
        int fileId;


        public FileDynamicParam(int projectId, int fileId) {
            this.projectId = projectId;
            this.fileId = fileId;
        }

        @Override
        public PostRequest getSendCommentParam(String input) {
            String url = String.format(Global.HOST_API +
                    "/project/%d/files/%d/comment", projectId, fileId);
            PostRequest request = new PostRequest(url, new RequestParams());
            request.setContent(input);
            return request;
        }

        @Override
        public String getAtSome() {
            return "";
        }

        @Override
        public String getAtSomeUrl() {
            return String.format(Global.HOST_API +
                    "/user/relationships/context?context_type=project_file_comment&item_id=%d", projectId);
        }

        @Override
        public String getProjectPath() {
            return String.format("/project/%d", projectId);
        }

        @Override
        public boolean isPublicProject() {
            return false;
        }
    }

    public static class ProjectFileParam implements Serializable {

        private AttachmentFileObject mFileObject;
        private int mProjectid;

        public ProjectFileParam(AttachmentFileObject fileObject, int projectId) {
            mFileObject = fileObject;
            mProjectid = projectId;
        }

        public String getHttpDynamic() {
            String url = Global.HOST_API + "/project/%d/file/%s/activities?last_id=9999999";
            return String.format(url, mProjectid, mFileObject.file_id);
        }
    }

    class DynamicFileAdapter extends ArrayAdapter<DynamicObject.DynamicBaseObject> {

        final int TYPE_DYNAMIC = 0;
        final int TYPE_COMMENT = 1;
        private final MyImageGetter myImageGetter = new MyImageGetter(getContext());
        private final ClickSmallImage onClickImage = new ClickSmallImage(FileDynamicActivity.this);
        private View.OnClickListener mOnClickComment = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final TaskObject.TaskComment comment = (TaskObject.TaskComment) v.getTag();
//                if (comment.isMy()) {
//                    showDialog("任务", "删除评论？", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            String url = String.format(hostDeleteComment, comment.taskId, comment.id);
//                            deleteNetwork(url, hostDeleteComment, comment.id);
//                        }
//                    });
//                } else {
//                    EnterLayout mEnterLayout = mEnterComment.getEnterLayout();
//                    mEnterLayout.content.setTag(comment);
//                    String format = "回复 %s";
//                    mEnterLayout.content.setHint(String.format(format, comment.owner.name));
//
//                    mEnterLayout.restoreLoad(comment);
//
//                    mEnterLayout.popKeyboard();
//                }
                showButtomToast("item");
            }
        };


        public DynamicFileAdapter(Context context, int resource, List<DynamicObject.DynamicBaseObject> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getItemViewType(int position) {
            DynamicObject.DynamicBaseObject item = getItem(position);
            if (item.target_type.equals("ProjectFileComment")) {
                return TYPE_COMMENT;
            } else {
                return TYPE_DYNAMIC;
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

//                TaskObject.TaskComment data = getItem(position);
//                holder.setContent(data);


                DynamicObject.DynamicProjectFileComment data = (DynamicObject.DynamicProjectFileComment) getItem(position);
                holder.setContent(data);

                holder.updateLine(position, count);

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

                holder.mContent.setText(data.content(null));

//                int iconResId = R.drawable.ic_task_dynamic_update;
//                try {
//                    String resName = "ic_task_dynamic_" + kotlin.data.action;
//                    Field field = R.drawable.class.getField(resName);
//                    iconResId = Integer.parseInt(field.get(null).toString());
//                } catch (Exception e) {
//                    Global.errorLog(e);
//                }
//                holder.mIcon.setImageResource(iconResId);

                holder.updateLine(position, count);

                return convertView;
            }
        }
    }
}
