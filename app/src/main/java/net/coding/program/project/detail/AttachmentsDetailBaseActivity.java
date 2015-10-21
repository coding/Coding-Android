package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.FileUtil;
import net.coding.program.common.Global;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.file.FileDynamicActivity;
import net.coding.program.project.detail.file.FileDynamicActivity_;
import net.coding.program.project.detail.file.FileSaveHelp;
import net.coding.program.project.detail.file.ShareFileLinkActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 展示某一项目文档目录下面文件的BsseActivity
 * Created by yangzhen
 */
@EActivity
public class AttachmentsDetailBaseActivity extends BackActivity {

    private static final int RESULT_SHARE_LINK = 1;
    private static String TAG = AttachmentsDetailBaseActivity.class.getSimpleName();
    protected File mFile;
    @Extra
    boolean mHideHistory = false;
    @Extra
    int mProjectObjectId;
    @Extra
    ProjectObject mProject;
    @Extra
    AttachmentFileObject mAttachmentFileObject;
    @Extra
    AttachmentFolderObject mAttachmentFolderObject;
    @Extra
    protected File mExtraFile;

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
        getSupportActionBar().setTitle(mAttachmentFileObject.getName());

        mFileSaveHelp = new FileSaveHelp(this);
        client = MyAsyncHttpClient.createClient(AttachmentsDetailBaseActivity.this);

        if (mExtraFile != null) {
            mFile = mExtraFile;
        } else {
            mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mAttachmentFileObject.getSaveName(mProjectObjectId));
        }

        findViewById(R.id.layout_dynamic_history).setVisibility(mHideHistory ? View.GONE : View.VISIBLE);
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
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FILE_DELETE)) {
            if (code == 0) {
                hideProgressDialog();
                showButtomToast("删除完成");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("mAttachmentFileObject", mAttachmentFileObject);
                setResult(RESULT_OK, resultIntent);
                finish();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDetailBaseActivity.this);
        builder.setTitle("删除文件").setMessage(String.format(messageFormat, mAttachmentFileObject.getName()))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialogLoading("正在删除");
                        deleteNetwork(String.format(HOST_FILE_DELETE, mProjectObjectId, mAttachmentFileObject.file_id), HOST_FILE_DELETE);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //builder.create().show();
        AlertDialog dialog = builder.create();
        dialog.show();
        dialogTitleLineColor(dialog);

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

            AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDetailBaseActivity.this);
            builder.setTitle("提示")
                    .setMessage(String.format(msgFormat, mFileSaveHelp.getDefaultPath())).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    download(urlDownload);
                }
            });
            //builder.create().show();
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogTitleLineColor(dialog);

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
        Global.copy(this, preViewUrl);
        showButtomToast("已复制 " + preViewUrl);
    }

    @OptionsItem
    protected final void action_link_public() {
        ShareFileLinkActivity_.intent(this)
                .mAttachmentFileObject(mAttachmentFileObject)
                .mProject(mProject)
                .startForResult(RESULT_SHARE_LINK);
    }

    @OnActivityResult(RESULT_SHARE_LINK)
    void onResultShareLink(int result, Intent intent) {
        if (result == RESULT_OK) {
            setResult(result, intent);
            mAttachmentFileObject = (AttachmentFileObject) intent.getSerializableExtra("data");
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("文件信息")
                .setMessage(String.format(fileInfoFormat,
                        mAttachmentFileObject.fileType,
                        Global.HumanReadableFilesize(mAttachmentFileObject.getSize()),
                        Global.dayToNow(mAttachmentFileObject.created_at),
                        Global.dayToNow(mAttachmentFileObject.updated_at),
                        mAttachmentFileObject.owner.name))
                .setPositiveButton("确定", null)
                .show();
        dialogTitleLineColor(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Click
    protected void clickFileDynamic() {
        FileDynamicActivity.ProjectFileParam param =
                new FileDynamicActivity.ProjectFileParam(mAttachmentFileObject, mProject);
        FileDynamicActivity_.intent(this)
                .mProjectFileParam(param)
                .start();
    }

    @Click
    protected void clickFileHistory() {
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
            public void onSuccess(int statusCode, Header[] headers, File response) {
                Log.v(TAG, "onSuccess:" + statusCode + " " + headers.toString());
                showButtomToast("下载完成");
                isDownloading = false;
                onDownloadFinish(true);

                /*MediaScannerConnection.scanFile(AttachmentsPicDetailActivity.this,
                        new String[]{response.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });*/
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(response)));
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.v(TAG, String.format("Progress %d from %d (%2.0f%%)", bytesWritten, totalSize, (totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1));
            }
        });
    }

    protected void onDownloadFinish(boolean success) {
    }


//    @Click
//    protected void clickFileDynamic() {
//        FileDynamicActivity.ProjectFileParam param =
//                new FileDynamicActivity.ProjectFileParam(mAttachmentFileObject, mProjectObjectId);
//        FileDynamicActivity_.intent(this)
//                .mProjectFileParam(param)
//                .start();
//    }
//
//    @Click
//    protected void clickFileHistory() {
//        FileDynamicActivity.ProjectFileParam param =
//                new FileDynamicActivity.ProjectFileParam(mAttachmentFileObject, mProjectObjectId);
//        FileHistoryActivity_.intent(this)
//                .mProjectFileParam(param)
//                .start();
//    }
}
