package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.loopj.android.http.RequestParams;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.BaseComment;
import net.coding.program.model.Commit;
import net.coding.program.model.Merge;
import net.coding.program.project.detail.TopicAddActivity;
import net.coding.program.project.detail.TopicEditFragment;
import net.coding.program.task.TaskDespPreviewFragment_;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@EActivity(R.layout.activity_comment)
public class CommentActivity extends BackActivity implements TopicEditFragment.SaveData {

    private static final String HOST_SEND_COMMENT = "HOST_SEND_COMMENT";
    @Extra
    Merge mMerge;
    @Extra
    CommitCommentParam mCommitParam;

    CommentEditFragment editFragment;
    Fragment previewFragment;
    private TopicAddActivity.TopicData modifyData = new TopicAddActivity.TopicData();

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mMerge != null) {
            editFragment = CommentEditFragment_.builder().mMergeUrl(mMerge.getMergeAtMemberUrl()).build();
        } else {
            editFragment = CommentEditFragment_.builder().mMergeUrl(mCommitParam.atUrl).build();
        }
        previewFragment = TaskDespPreviewFragment_.builder().build();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (!editFragment.isEmpty()) {
            showDialog("发表评论", "确定放弃已写的评论？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        } else {
            finish();
        }
    }

    @Override
    public void saveData(TopicAddActivity.TopicData data) {
        modifyData = data;
    }

    @Override
    public TopicAddActivity.TopicData loadData() {
        return modifyData;
    }

    @Override
    public void switchPreview() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, previewFragment).commit();
    }

    @Override
    public void switchEdit() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
    }

    @Override
    public void exit() {
        String contentString = modifyData.content;
        if (EmojiFilter.containsEmptyEmoji(this, contentString, "内容不能为空", "内容不能包含表情")) {
            return;
        }

        if (mMerge != null) {
            Merge.PostRequest postRequest = mMerge.getHttpSendComment();
            postRequest.setContent(contentString);
            postNetwork(postRequest.url, postRequest.params, HOST_SEND_COMMENT);
            showProgressBar(true, "发送中");
        } else {
            String url = Commit.getHttpSendComment(mCommitParam.projectPath);
            RequestParams params = new RequestParams();
            params.put("commitId", mCommitParam.mCommitId);
            params.put("noteable_type", "Commit");
            params.put("content", contentString);
            params.put("position", 0);
            params.put("line", 0);
            postNetwork(url, params, HOST_SEND_COMMENT);
            showProgressBar(true, "发送中");
        }
    }

    @Override
    public String getProjectPath() {
        if (mMerge != null) {
            return mMerge.getProjectPath();
        } else {
            return mCommitParam.projectPath;
        }
    }

    @Override
    public boolean isProjectPublic() {
        if (mMerge != null) {
            return mMerge.isPull();
        } else {
            return mCommitParam.isPull();

        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_SEND_COMMENT)) {
            showProgressBar(false);
            if (code == 0) {
                BaseComment comment = new BaseComment(respanse.getJSONObject("data"));
                Intent intent = new Intent();
                intent.putExtra("data", comment);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    public static class CommitCommentParam implements Serializable {
        String projectPath;
        String mCommitId;
        String atUrl;

        public CommitCommentParam(String projectPath, String mCommitId) {
            this.projectPath = projectPath;
            this.mCommitId = mCommitId;
            atUrl = Global.HOST_API + projectPath + "/relationships/context?context_type=pull_request_comment&item_id=" + mCommitId;
        }

        public boolean isPull() { // 这个参数没有用处
            return true;
        }
    }
}
