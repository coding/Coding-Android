package net.coding.program.project.detail.merge;

import android.content.Intent;

import net.coding.program.R;
import net.coding.program.common.base.MDEditPreviewActivity;
import net.coding.program.common.model.BaseComment;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.project.detail.file.FileDynamicActivity;
import net.coding.program.task.TaskDespPreviewFragment_;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@EActivity(R.layout.activity_comment)
public class CommentActivity extends MDEditPreviewActivity {

    private static final String HOST_SEND_COMMENT = "HOST_SEND_COMMENT";

    @Extra
    CommentParam mParam;

    private TopicData modifyData = new TopicData();

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String atName = mParam.getAtSome();
        if (!atName.isEmpty()) {
            modifyData.content = String.format("@%s ", atName);
        }

        editFragment = CommentEditFragment_.builder().mMergeUrl(mParam.getAtSomeUrl()).build();
        previewFragment = TaskDespPreviewFragment_.builder().build();
        initEditPreviewFragment();

        switchEdit();
    }

    @Override
    public void saveData(TopicData data) {
        modifyData = data;
    }

    @Override
    public TopicData loadData() {
        return modifyData;
    }

    @Override
    public void exit() {
        String contentString = modifyData.content;
        if (EmojiFilter.containsEmptyEmoji(this, contentString)) {
            return;
        }

        RequestData request = mParam.getSendCommentParam(contentString);
        postNetwork(request.url, request.params, HOST_SEND_COMMENT);
        showProgressBar(true, "发送中");
    }

    @Override
    public String getProjectPath() {
        return mParam.getProjectPath();
    }

    @Override
    public boolean isProjectPublic() {
        return mParam.isPublicProject();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_SEND_COMMENT)) {
            showProgressBar(false);
            if (code == 0) {
                addUmengLog();
                JSONObject jsonData = respanse.getJSONObject("data");
                if (!jsonData.optString("noteable_id").isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("data", jsonData.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    BaseComment comment = new BaseComment(jsonData);
                    Intent intent = new Intent();
                    intent.putExtra("data", comment);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void addUmengLog() {
        if (mParam instanceof FileDynamicActivity.FileDynamicParam) {
            // 文件评论
            umengEvent(UmengEvent.E_FILE, "添加文件评论");
        } else if (mParam instanceof MergeFileDetailActivity.LineNoteParam) {
            umengEvent(UmengEvent.E_GIT, "添加Linenote评论");
        } else if (mParam instanceof MergeDetailActivity.MergeCommentParam) {
            umengEvent(UmengEvent.E_GIT, "添加MR/PR评论");
        }
        // commit 评论未加
    }

    public static abstract class CommentParam implements Serializable {
        public abstract RequestData getSendCommentParam(String input);

        public abstract String getAtSome();

        public abstract String getAtSomeUrl();

        public abstract String getProjectPath();

        public abstract boolean isPublicProject();
    }
}
