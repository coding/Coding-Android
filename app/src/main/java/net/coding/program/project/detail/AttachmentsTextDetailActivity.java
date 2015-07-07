package net.coding.program.project.detail;

import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import net.coding.program.ImagePagerFragment;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.model.AttachmentFileObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 展示某一项目文档目录下面TXT文件的Activity
 * Created by yangzhen
 */
@EActivity(R.layout.activity_attachments_text)
public class AttachmentsTextDetailActivity extends AttachmentsDetailBaseActivity {

    @ViewById
    TextView textView;

    @ViewById
    View blankLayout;

    boolean downloadFileSuccess = false;

    String urlFiles = Global.HOST + "/api/project/%s/files/%s/view";

    AttachmentFileObject mFiles = new AttachmentFileObject();

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @AfterViews
    void init1() {
        urlFiles = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id);

        showDialogLoading();
        getFileUrlFromNetwork();
    }

    private void getFileUrlFromNetwork() {
        getNetwork(urlFiles, urlFiles);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (downloadFileSuccess) {
            return super.onCreateOptionsMenu(menu);
        } else {
            return true;
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        super.parseJson(code, respanse, tag, pos, data);
        if (tag.equals(urlFiles)) {
            if (code == 0) {
                hideProgressDialog();
                JSONObject file = respanse.getJSONObject("data").getJSONObject("file");
                mFiles = new AttachmentFileObject(file);
                String content = respanse.getJSONObject("data").optString("content");
                textView.setText(content);
                invalidateOptionsMenu();

            } else {
                if (code == ImagePagerFragment.HTTP_CODE_FILE_NOT_EXIST) {
                    BlankViewDisplay.setBlank(0, this, true, blankLayout, null);
                } else {
                    BlankViewDisplay.setBlank(0, this, false, blankLayout, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getFileUrlFromNetwork();
                        }
                    });
                }

                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }

    @OptionsItem
    public void action_add() {
    }

}
