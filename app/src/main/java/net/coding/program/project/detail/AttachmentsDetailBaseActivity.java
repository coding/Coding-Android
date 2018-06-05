package net.coding.program.project.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.Share;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.CodingToolbarBackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.project.detail.file.FileDynamicActivity;
import net.coding.program.project.detail.file.FileDynamicActivity_;
import net.coding.program.project.detail.file.FileSaveHelp;
import net.coding.program.project.detail.file.ShareFileLinkActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * 展示某一项目文件目录下面文件的BsseActivity
 * Created by yangzhen
 */
@EActivity
public class AttachmentsDetailBaseActivity extends CodingToolbarBackActivity {

    private static final int RESULT_SHARE_LINK = 1;
    private static final String TAG_SHARE_LINK_ON = "TAG_SHARE_LINK_ON";
    private static String TAG = AttachmentsDetailBaseActivity.class.getSimpleName();
    protected File mFile;

    @Extra
    protected File mExtraFile;
    @Extra
    boolean mHideHistory = false;
    @Extra
    int mProjectObjectId;
    @Extra
    ProjectObject mProject;
    @Extra
    AttachmentFileObject mAttachmentFileObject;

    String urlDownload = "";
    AsyncHttpClient client;
    String fileInfoFormat =
            "文件类型: %s\n" +
                    "文件大小: %s\n" +
                    "创建时间: %s\n" +
                    "最近更新: %s\n" +
                    "创建人: %s";
    private String HOST_FILE_DELETE = Global.HOST_API + "/project/%d/file/delete?fileIds=%s";
    private String urlDownloadBase = Global.HOST_API + "/project/%d/files/%s/download";
    private boolean isDownloading = false;
    private FileSaveHelp mFileSaveHelp;
    private String type;

    @AfterViews
    protected final void initAttachmentsDetailBaseActivity() {
        setActionBarTitle(mAttachmentFileObject.getName());

        mFileSaveHelp = new FileSaveHelp(this);
        client = MyAsyncHttpClient.createClient(AttachmentsDetailBaseActivity.this);

        if (mExtraFile != null) {
            mFile = mExtraFile;
        } else {
            mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mAttachmentFileObject.getSaveName(mProjectObjectId));
        }

        View dynamicLayout = findViewById(R.id.bottomToolBar);
        dynamicLayout.setVisibility(mHideHistory ? View.GONE : View.VISIBLE);

        if (mProject == null) {
            dynamicLayout.setEnabled(false);
            String url = Global.HOST_API + "/project/" + mProjectObjectId;
            MyAsyncHttpClient.get(this, url, new MyJsonResponse(this) {
                @Override
                public void onMySuccess(JSONObject response) {
                    super.onMySuccess(response);
                    try {
                        mProject = new ProjectObject(response.optJSONObject("data"));
                        dynamicLayout.setEnabled(true);
                    } catch (Exception e) {
                        Global.errorLog(e);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mExtraFile != null) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(getMenuResourceId(), menu);
        if (!mAttachmentFileObject.isOwner()) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected int getMenuResourceId() {
        return R.menu.project_attachment_image;
    }

    @Override
    protected ProjectObject getProject() {
        return mProject;
    }

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FILE_DELETE)) {
            if (code == 0) {
                hideProgressDialog();
                showButtomToast("删除完成");
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AttachmentFileObject.RESULT, mAttachmentFileObject);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                showErrorMsg(code, response);
            }
        } else if (tag.equals(TAG_SHARE_LINK_ON)) {
            if (code == 0) {
                umengEvent(UmengEvent.FILE, "开启共享");
                Share mShare = new Share(response.optJSONObject("data"));
                mAttachmentFileObject.setShereLink(mShare.getUrl());

                setResult(RESULT_OK);
                copyShareLink();
            } else {
                showErrorMsg(code, response);
            }
        }
    }

    public String getFileDownloadPath() {
        return mFileSaveHelp.getFileDownloadPath();
    }

    //@Click(R.id.btnLeft)
    @OptionsItem
    protected void action_delete() {
        String messageFormat = "确定要删除文件 \"%s\" 么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDetailBaseActivity.this, R.style.MyAlertDialogStyle);
        builder.setTitle("删除文件").setMessage(String.format(messageFormat, mAttachmentFileObject.getName()))
                .setPositiveButton("确定", (dialog, which) -> {
                    showDialogLoading();
                    deleteNetwork(String.format(HOST_FILE_DELETE, mProjectObjectId, mAttachmentFileObject.file_id), HOST_FILE_DELETE);
                }).setNegativeButton("取消", null)
                .show();

    }

    @OptionsItem
    protected final void action_download() {
        //showButtomToast("savePic");
        if (mFile != null && mFile.exists() && mFile.isFile()) {
            showButtomToast("文件已经下载");
            return;
        } else if (isDownloading) {
            showButtomToast("文件正在下载");
            return;
        }
        urlDownload = String.format(urlDownloadBase, mProjectObjectId, mAttachmentFileObject.file_id);
        if (mFileSaveHelp.needShowHint()) {
            String msgFormat = "您的文件将下载到以下路径：\n%s\n您也可以去设置界面设置您的下载路径";

            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setTitle("提示")
                    .setMessage(String.format(msgFormat, mFileSaveHelp.getDefaultPath()))
                    .setPositiveButton("确定", (dialog, which) -> download(urlDownload))
                    .show();

            mFileSaveHelp.alwaysHideHint();
        } else {
            download(urlDownload);
        }
    }

    @OptionsItem
    protected final void action_copy() {
        String preViewUrl = mAttachmentFileObject.owner_preview;
        int pos = preViewUrl.lastIndexOf("imagePreview");
        if (pos != -1) {
            preViewUrl = preViewUrl.substring(0, pos) + "download";
        }
        Global.tipCopyLink(this, preViewUrl);
    }

    @OptionsItem
    protected final void action_link_public() {
        ShareFileLinkActivity_.intent(this)
                .mAttachmentFileObject(mAttachmentFileObject)
                .mProject(mProject)
                .startForResult(RESULT_SHARE_LINK);
    }

    private void copyShareLink() {
        String shareLink = mAttachmentFileObject.getShareLink();
        Global.copy(this, shareLink);
        showButtomToast("共享链接已复制");
    }

    @OnActivityResult(RESULT_SHARE_LINK)
    void onResultShareLink(int result, Intent intent) {
        if (result == RESULT_OK) {
            setResult(result, intent);
            onRefresh();
        }
    }

    protected void onRefresh() {
    }

    @OptionsItem
    protected final void action_open_by_other() {
        if (mFile != null && mFile.exists()) {
//            try {
//                Intent mResultIntent = new Intent(Intent.ACTION_VIEW);
//                Uri fileUri = Uri.fromFile(mFile);
//                type = getContentResolver().getType(fileUri);
//                mResultIntent.setDataAndType(fileUri,
//                        type);
//                startActivity(mResultIntent);
//            } catch (Exception e) {
//                Global.errorLog(e);
//                showMiddleToast("没有能打开此文件的程序");
//            }
            AttachmentsDownloadDetailActivity.openFile(this, mFile);
        } else {
            showMiddleToast("文件未下载");
        }
    }

    @OptionsItem
    void action_info() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("文件信息")
                .setMessage(String.format(fileInfoFormat,
                        mAttachmentFileObject.fileType,
                        Global.HumanReadableFilesize(mAttachmentFileObject.getSize()),
                        Global.dayToNow(mAttachmentFileObject.created_at),
                        Global.dayToNow(mAttachmentFileObject.updated_at),
                        mAttachmentFileObject.owner.name))
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    protected View.OnClickListener clickBottomBar = v -> {
        int id = v.getId();
        if (id == R.id.clickFileDynamic) {
            clickFileDynamic();
        } else if (id == R.id.clickFileHistory) {
            clickFileHistory();
        }
    };

    protected void clickFileDynamic() {
        umengEvent(UmengEvent.E_FILE, "点击文件动态");
        FileDynamicActivity.ProjectFileParam param =
                new FileDynamicActivity.ProjectFileParam(mAttachmentFileObject, mProject);
        FileDynamicActivity_.intent(this)
                .mProjectFileParam(param)
                .start();
    }

    protected void clickFileHistory() {
        umengEvent(UmengEvent.E_FILE, "点击历史版本");
        FileDynamicActivity.ProjectFileParam param =
                new FileDynamicActivity.ProjectFileParam(mAttachmentFileObject, mProject);
        FileHistoryActivity_.intent(this)
                .mProjectFileParam(param)
                .start();
    }

    private void download(String url) {
        Log.v(TAG, "download:" + url);
        isDownloading = true;

        client.get(AttachmentsDetailBaseActivity.this, url, new FileAsyncHttpResponseHandler(mFile) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                showButtomToast("下载失败");
                isDownloading = false;
                onDownloadFinish(false);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int progresss = (int) ((totalSize > 0 && bytesWritten >= 0) ? bytesWritten * 100 / totalSize : 0);
                onDownloadProgress(progresss);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                Log.v(TAG, "onSuccess:" + statusCode + " " + headers.toString());
                umengEvent(UmengEvent.E_FILE, "下载成功");
                showButtomToast("下载完成");
                isDownloading = false;
                onDownloadFinish(true);

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(response)));
            }

        });
    }

    protected void onDownloadFinish(boolean success) {
    }

    protected void onDownloadProgress(int progress) {
    }
}
