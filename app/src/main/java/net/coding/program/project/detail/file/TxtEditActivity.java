package net.coding.program.project.detail.file;

import android.content.Intent;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.RequestData;
import net.coding.program.project.detail.AttachmentsActivity;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@EActivity(R.layout.activity_txt_edit)
@OptionsMenu(R.menu.menu_txt_edit)
public class TxtEditActivity extends BackActivity {

    private static final String TAG_SAVE_CONTENT = "TAG_SAVE_CONTENT";
    private static final String TAG_HTTP_FILE_VIEW = "TAG_HTTP_FILE_VIEW";

    @Extra
    FileDynamicActivity.ProjectFileParam mParam;

    @ViewById
    EditText editText;

    private FileSaveHelp mFileSaveHelp;

    public static void writeFile(File srcFile, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(srcFile);
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @AfterViews
    final void initTxtEditActivity() {
        getSupportActionBar().setTitle(mParam.getFileObject().getName());
        mFileSaveHelp = new FileSaveHelp(this);

        File file = mParam.getLocalFile(mFileSaveHelp.getFileDownloadPath());
        if (file != null && file.exists()) {
            String content = "";
            try {
                FileInputStream is = new FileInputStream(file);
                content = AttachmentsHtmlDetailActivity.readTextFile(is);
            } catch (Exception e) {
                Global.errorLog(e);
            }
            editText.setText(content);
        } else {
            showButtomToast("文件未保存到本地");
            finish();
        }
    }

    @OptionsItem
    void action_save() {
        RequestData request = mParam.getHttpEditFile(editText.getText().toString());
        postNetwork(request, TAG_SAVE_CONTENT);
        showProgressBar(true, "正在保存");
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_SAVE_CONTENT)) {
            showProgressBar(false);
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
                writeFile(localFile, editText.getText().toString());
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
}
