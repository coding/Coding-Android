package net.coding.program.project.detail.merge;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditPreviewActivity;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.task.TaskDescrip;
import net.coding.program.task.TaskDespEditFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONObject;

import java.io.Serializable;

@EActivity(R.layout.activity_readme_edit)
public class ReadmeEditActivity extends MDEditPreviewActivity implements TaskDescrip {

    @Extra
    ProjectObject mProjectObject;

    @Extra
    PostParam mPostParam;

    private TopicData modifyData = new TopicData();

    @AfterViews
    protected final void initTaskDescriptionActivity() {
        editFragment = TaskDespEditFragment_.builder().build();
        String url = mProjectObject.getHttpReadmePreview(mPostParam.version, mPostParam.name);
        previewFragment = ReadmePerviewFragment_.builder().url(url).build();

//        "https://coding.net/api" + "/user/gggg/project/gggg_ghhjj" + "/git/blob-preview/master%252FREADME.md"
//        if (markdown.isEmpty()) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
//        } else {
        modifyData.content = mPostParam.data;

        initEditPreviewFragment();
        switchEdit();
//        }
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
        RequestParams params = new RequestParams();
        params.put("content", modifyData.content);
        params.put("message", "update README.md");
        params.put("lastCommitSha", mPostParam.lastCommitId);
        String url = mProjectObject.getHttpReadme(mPostParam.version, mPostParam.name);
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(ReadmeEditActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                closeAndSave("");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });
        showProgressBar(true);
    }

    @Override
    public String getProjectPath() {
        return mProjectObject.getProjectPath();
    }

    @Override
    public boolean isProjectPublic() {
        return mProjectObject.isPublic();
    }

    @Override
    public void closeAndSave(String s) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public String createLocateHtml(String s) {
        try {
            final String bubble = Global.readTextFile(getAssets().open("markdown.html"));
            return bubble.replace("${webview_content}", s);
        } catch (Exception e) {
            Global.errorLog(e);
            return "";
        }
    }

    public static class PostParam implements Serializable {
        String lastCommitId;
        String name;
        String data;
        String version;

        public PostParam(JSONObject jsonData, String version) {
            JSONObject headCommit = jsonData.optJSONObject("headCommit");
            lastCommitId = headCommit.optString("commitId", "");

            JSONObject json = jsonData.optJSONObject("readme");
            name = json.optString("name", "");
            data = json.optString("data", "");
            this.version = version;
        }
    }
}
