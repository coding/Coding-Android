package net.coding.program.project.detail;

import android.widget.TextView;

import net.coding.program.Global;
import net.coding.program.R;
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

    String urlFiles = Global.HOST + "/api/project/%s/files/%s/view";

    AttachmentFileObject mFiles = new AttachmentFileObject();

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @AfterViews
    void init() {
        super.init();

        urlFiles = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id);

        showDialogLoading();
        getNetwork(urlFiles, urlFiles);
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

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }

    @OptionsItem
    public void action_add() {

    }

}
