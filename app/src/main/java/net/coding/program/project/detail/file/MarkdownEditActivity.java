package net.coding.program.project.detail.file;

import android.content.Intent;
import android.view.View;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditPreviewActivity;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.task.TaskDescrip;
import net.coding.program.task.TaskDespEditFragment_;
import net.coding.program.task.TaskDespPreviewFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_markdown_edit)
public class MarkdownEditActivity extends MDEditPreviewActivity implements TaskDescrip {

    private static final String TAG_SAVE_CONTENT = "TAG_SAVE_CONTENT";
    private static final String TAG_HTTP_FILE_VIEW = "TAG_HTTP_FILE_VIEW";

    @Extra
    FileDynamicActivity.ProjectFileParam mParam;

    @ViewById(R.id.blankLayout)
    View blankLayout;

    SingleTask.TaskDescription descriptionData = new SingleTask.TaskDescription();

    String HOST_DESCRIPTION = Global.HOST_API + "/task/%s/description";

    private TopicData modifyData = new TopicData();

    @AfterViews
    protected final void initTaskDescriptionActivity() {
        BlankViewHelp.setBlankLoading(blankLayout, true);
        editFragment = TaskDespEditFragment_.builder().build();
        previewFragment = TaskDespPreviewFragment_.builder().build();
        initEditPreviewFragment();

        FileSaveHelp mFileSaveHelp = new FileSaveHelp(this);
        File file = mParam.getLocalFile(mFileSaveHelp.getFileDownloadPath());
        if (file != null && file.exists()) {
            initData(file);
            BlankViewHelp.setBlankLoading(blankLayout, false);
        } else {
            String urlDownload = Global.HOST_API + "/project/%d/files/%s/download";
            String url = String.format(urlDownload, mParam.getProjectId(), mParam.getFileId());
            MyAsyncHttpClient.get(this, url, new FileAsyncHttpResponseHandler(this) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    initData(file);
                    reloadData();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    BlankViewHelp.setBlankLoading(blankLayout, false);
                }
            });
        }
    }

    private void initData(File file) {
        descriptionData.markdown = Global.readTextFile(file);
        modifyData.content = descriptionData.markdown;
        if (modifyData.content.isEmpty() || mParam.openByEditor) {
            switchEdit();
        } else {
            switchPreview();
        }
    }


    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_DESCRIPTION)) {
            hideProgressDialog();
            if (code == 0) {
                showButtomToast("修改成功");
                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_SAVE_CONTENT)) {
            hideProgressDialog();
            if (code == 0) {
                showProgressBar(true, "正在保存");
                setResult(RESULT_OK);
                String url = mParam.getHtttpFileView();
                getNetwork(url, TAG_HTTP_FILE_VIEW);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_HTTP_FILE_VIEW)) {
            showProgressBar(false);
            if (code == 0) {
                JSONObject json = respanse.optJSONObject("data").optJSONObject("file");
                AttachmentFileObject fileObject = new AttachmentFileObject(json);

                FileSaveHelp help = new FileSaveHelp(this);
                mParam.setFileObject(fileObject);
                File localFile = mParam.getLocalFile(help.getFileDownloadPath());
                TxtEditActivity.writeFile(localFile, modifyData.content);
                fileObject.isDownload = true;

                EventBus.getDefault().post(new EventFileModify());

                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void closeAndSave(String s) {
        Intent intent = new Intent();
        intent.putExtra("data", s);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public String createLocateHtml(String s) {
        try {
            final String bubble = Global.readTextFile(getAssets().open("topic-android.html"));
            return bubble.replace("${webview_content}", s);
        } catch (Exception e) {
            Global.errorLog(e);
            return "";
        }
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
        RequestData request = mParam.getHttpEditFile(modifyData.content);
        postNetwork(request, TAG_SAVE_CONTENT);
        showProgressBar(true, "正在保存");
    }

    @Override
    public String getProjectPath() {
        return mParam.getProjectPath();
    }

    // 有文件的项目必定是私有项目
    @Override
    public boolean isProjectPublic() {
        return false;
    }
}
