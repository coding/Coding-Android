package net.coding.program.project.detail.file;

import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_txt_edit)
@OptionsMenu(R.menu.menu_txt_edit)
public class TxtEditActivity extends BackActivity {

    private static final String TAG_SAVE_CONTENT = "TAG_SAVE_CONTENT";
    private static final String TAG_HTTP_FILE_VIEW = "TAG_HTTP_FILE_VIEW";

    @Extra
    FileDynamicActivity.ProjectFileParam mParam;

    @ViewById
    EditText editText;

    @ViewById(R.id.blankLayout)
    View blankLayout;

    private FileSaveHelp mFileSaveHelp;

    String oldContent = "";

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

        BlankViewHelp.setBlankLoading(blankLayout, true);
        File file = mParam.getLocalFile(mFileSaveHelp.getFileDownloadPath());
        if (file != null && file.exists()) {
            initFile(file);
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
                    initFile(file);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    BlankViewHelp.setBlankLoading(blankLayout, false);
                }
            });

        }
    }

    private void initFile(File file) {
        String content = "";
        try {
            FileInputStream is = new FileInputStream(file);
            content = AttachmentsHtmlDetailActivity.readTextFile(is);
        } catch (Exception e) {
            Global.errorLog(e);
        }
        oldContent = content;
        editText.setText(oldContent);
    }

    @Override
    public void onBackPressed() {
        if (!editText.getText().toString().equals(oldContent)) {
            showDialog("确定放弃此次编辑？", (dialog, which) -> finish());
        } else {
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
                umengEvent(UmengEvent.E_FILE, "点击编辑");

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

                EventBus.getDefault().post(new EventFileModify());
                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
