package net.coding.program.project.detail.file;

import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.PostRequest;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_share_file_link)
public class ShareFileLinkActivity extends BackActivity {

    private static final String TAG_SHARE_LINK_ON = "TAG_SHARE_LINK_ON";
    private static final String TAG_SHARE_LINK_OFF = "TAG_SHARE_LINK_OFF";

    @Extra
    ProjectObject mProject;

    @Extra
    AttachmentFileObject mAttachmentFileObject;

    @ViewById
    View layoutShareLink;

    @ViewById
    CheckBox clickSettingShare;

    @ViewById
    TextView linkContent;
    private CompoundButton.OnCheckedChangeListener settingShareLister = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (clickSettingShare.isChecked()) {
                PostRequest request = mAttachmentFileObject.getHttpShareLinkOn(mProject);
                postNetwork(request, TAG_SHARE_LINK_ON);
            } else {
                String url = mAttachmentFileObject.getHttpShareLinkOff();
                deleteNetwork(url, TAG_SHARE_LINK_OFF);
            }

            showProgressBar(true, "");
        }

    };

    @AfterViews
    protected final void initShareFileLinkActivity() {
        clickSettingShare.setOnCheckedChangeListener(null);
        if (mAttachmentFileObject.isShared()) {
            clickSettingShare.setChecked(true);
        } else {
            clickSettingShare.setChecked(false);
        }
        clickSettingShare.setOnCheckedChangeListener(settingShareLister);

        bindData();
    }

    @Click
    void clickShareLink() {
        String shareLink = mAttachmentFileObject.getShareLink();
        Global.copy(this, shareLink);
        showButtomToast("共享链接已复制");
        finish();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_SHARE_LINK_ON)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "开启共享");
                AttachmentFileObject.Share mShare = new AttachmentFileObject.Share(respanse.optJSONObject("data"));
                mAttachmentFileObject.setShereLink(mShare.getUrl());
                bindData();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_SHARE_LINK_OFF)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "关闭共享");
                mAttachmentFileObject.setShereLink("");
                bindData();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void bindData() {
        if (mAttachmentFileObject.isShared()) {
            layoutShareLink.setVisibility(View.VISIBLE);
            linkContent.setText(mAttachmentFileObject.getShareLink());
        } else {
            layoutShareLink.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("data", mAttachmentFileObject);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
