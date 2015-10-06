package net.coding.program.project.detail.file;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.PostRequest;
import net.coding.program.model.TaskObject;
import net.coding.program.project.detail.AttachmentsActivity;
import net.coding.program.project.detail.TopicAddActivity;
import net.coding.program.project.detail.TopicEditFragment;
import net.coding.program.task.TaskDescrip;
import net.coding.program.task.TaskDespEditFragment;
import net.coding.program.task.TaskDespEditFragment_;
import net.coding.program.task.TaskDespPreviewFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EActivity(R.layout.activity_markdown_edit)
//@OptionsMenu(R.menu.menu_markdown_edit)
public class MarkdownEditActivity extends BackActivity implements TaskDescrip, TopicEditFragment.SaveData {

    private static final String TAG_SAVE_CONTENT = "TAG_SAVE_CONTENT";
    private static final String TAG_HTTP_FILE_VIEW = "TAG_HTTP_FILE_VIEW";

    @Extra
    FileDynamicActivity.ProjectFileParam mParam;

    TaskObject.TaskDescription descriptionData = new TaskObject.TaskDescription();

    String HOST_DESCRIPTION = Global.HOST_API + "/task/%s/description";

    TaskDespEditFragment editFragment;
    Fragment previewFragment;
    private TopicAddActivity.TopicData modifyData = new TopicAddActivity.TopicData();

    @AfterViews
    protected final void initTaskDescriptionActivity() {
        editFragment = TaskDespEditFragment_.builder().build();
        previewFragment = TaskDespPreviewFragment_.builder().build();

        FileSaveHelp mFileSaveHelp = new FileSaveHelp(this);
        File file = mParam.getLocalFile(mFileSaveHelp.getFileDownloadPath());
        descriptionData.markdown = TxtEditActivity.readPhoneNumber(file);

        String markdown = descriptionData.markdown;
        if (markdown.isEmpty()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
        } else {
            modifyData.content = markdown;
            getSupportFragmentManager().beginTransaction().replace(R.id.container, previewFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (editFragment.isContentModify()) {
            showDialog(mParam.getFileObject().getName(), "确定放弃此次编辑？", new DialogInterface.OnClickListener() {
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

                Intent intent = new Intent();
                intent.putExtra(AttachmentFileObject.RESULT, fileObject);
                intent.putExtra(AttachmentsActivity.FileActions.ACTION_NAME, AttachmentsActivity.FileActions.ACTION_EDIT);
                setResult(RESULT_OK, intent);
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
            final String bubble = Global.readTextFile(getAssets().open("topic-android"));
            return bubble.replace("${webview_content}", s);
        } catch (Exception e) {
            Global.errorLog(e);
            return "";
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
        PostRequest request = mParam.getHttpEditFile(modifyData.content);
        postNetwork(request, TAG_SAVE_CONTENT);
        showProgressBar(true, "正在保存到服务器");
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
