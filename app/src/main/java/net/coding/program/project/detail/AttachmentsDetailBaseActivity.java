package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.FileUtil;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * 展示某一项目文档目录下面文件的BsseActivity
 * Created by yangzhen
 */
@EActivity
@OptionsMenu(R.menu.project_attachment_image)
public class AttachmentsDetailBaseActivity extends BaseFragmentActivity {
    private static String TAG = AttachmentsDetailBaseActivity.class.getSimpleName();

    @Extra
    String mProjectObjectId;

    @Extra
    AttachmentFileObject mAttachmentFileObject;

    @Extra
    AttachmentFolderObject mAttachmentFolderObject;

    private String HOST_FILE_DELETE = Global.HOST + "/api/project/%s/file/delete?fileIds=%s";
    private String urlDownloadBase = Global.HOST + "/api/project/%s/files/%s/download";
    String urlDownload = "";

    File mFile;

    private SharedPreferences share;
    private String defaultPath;

    AsyncHttpClient client;

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    String fileInfoFormat =
            "文件类型: %s\n" +
                    "文件大小: %s\n" +
                    "创建时间: %s\n" +
                    "最近更新: %s\n" +
                    "创建人: %s";

    @OptionsItem
    void action_info() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("文件信息")
                .setMessage(String.format(fileInfoFormat,
                        mAttachmentFileObject.fileType,
                        Global.HumanReadableFilesize(mAttachmentFileObject.size),
                        Global.dayToNow(mAttachmentFileObject.created_at),
                        Global.dayToNow(mAttachmentFileObject.updated_at),
                        mAttachmentFileObject.owner.name))
                .setPositiveButton("确定", null)
                .show();
        dialogTitleLineColor(dialog);
    }

    @OptionsItem
    protected void action_more() {
        showRightTopPop();
    }

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(mAttachmentFileObject.name);

        share = AttachmentsDetailBaseActivity.this.getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;
        client = MyAsyncHttpClient.createClient(AttachmentsDetailBaseActivity.this);

        mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mAttachmentFileObject.name);
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
        String path;
        if (share.contains(FileUtil.DOWNLOAD_PATH)) {
            path = share.getString(FileUtil.DOWNLOAD_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER);
        } else {
            path = defaultPath;
        }
        return path;
    }

    //@Click(R.id.btnLeft)
    @OptionsItem
    protected void action_delete() {
        String messageFormat = "确定要删除文件 \"%s\" 么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDetailBaseActivity.this);
        builder.setTitle("删除文件").setMessage(String.format(messageFormat, mAttachmentFileObject.name))
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

    //@Click(R.id.btnRight)
    //@OptionsItem
    protected void action_download() {
        //showButtomToast("savePic");
        if (mFile != null && mFile.exists() && mFile.isFile() && mFile.length() == mAttachmentFileObject.size) {
            showButtomToast("文件已经下载");
            return;
        } else if (isDownloading) {
            showButtomToast("文件正在下载");
            return;
        }
        urlDownload = String.format(urlDownloadBase, mProjectObjectId, mAttachmentFileObject.file_id);
        if (!share.contains(FileUtil.DOWNLOAD_SETTING_HINT)) {
            String msgFormat = "您的文件将下载到以下路径：\n%s\n您也可以去设置界面设置您的下载路径";

            AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDetailBaseActivity.this);
            builder.setTitle("提示")
                    .setMessage(String.format(msgFormat, defaultPath)).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    download(urlDownload);
                }
            });
            //builder.create().show();
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogTitleLineColor(dialog);

            SharedPreferences.Editor editor = share.edit();
            editor.putBoolean(FileUtil.DOWNLOAD_SETTING_HINT, true);
            editor.commit();
        } else {
            download(urlDownload);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    private boolean isDownloading = false;

    private void download(String url) {
        Log.v(TAG, "download:" + url);
        isDownloading = true;

        client.get(AttachmentsDetailBaseActivity.this, url, new FileAsyncHttpResponseHandler(mFile) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                showButtomToast("下载失败");
                isDownloading = false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                Log.v(TAG, "onSuccess:" + statusCode + " " + headers.toString());
                showButtomToast("下载完成");
                isDownloading = false;

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

    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    action_download();
                    break;
                case 1:
                    if (!mAttachmentFileObject.isOwner()) {
                        return;
                    } else {
                        action_delete();
                    }
                    break;
            }
            mRightTopPopupWindow.dismiss();
        }
    };


    /**
     * 为了实现设计的样式，右上角下拉没有用actionbar自带的，而是用了PopupWindow
     */
    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow = null;

    public void initRightTopPop() {
        if (mRightTopPopupWindow == null) {
            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList<DialogUtil.RightTopPopupItem>();
            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_save), R.drawable.ic_menu_download);
            popupItemArrayList.add(downloadItem);
            DialogUtil.RightTopPopupItem deleteItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_delete), R.drawable.ic_menu_delete_selector);
            popupItemArrayList.add(deleteItem);
            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(AttachmentsDetailBaseActivity.this, popupItemArrayList, onRightTopPopupItemClickListener);
        }
    }

    public void showRightTopPop() {

        if (mRightTopPopupWindow == null) {
            initRightTopPop();
        }

        DialogUtil.RightTopPopupItem downloadItem = mRightTopPopupWindow.adapter.getItem(0);
        DialogUtil.RightTopPopupItem deleteItem = mRightTopPopupWindow.adapter.getItem(1);

        if (!mAttachmentFileObject.isOwner()) {
            deleteItem.enabled = false;
        } else {
            deleteItem.enabled = true;
        }

        mRightTopPopupWindow.adapter.notifyDataSetChanged();

        Rect rectgle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int StatusBarHeight = rectgle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        //int TitleBarHeight= contentViewTop - StatusBarHeight;
        mRightTopPopupWindow.adapter.notifyDataSetChanged();
        mRightTopPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mRightTopPopupWindow.showAtLocation(((ViewGroup) findViewById(android.R.id.content)).getChildAt(0), Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);

    }

}
