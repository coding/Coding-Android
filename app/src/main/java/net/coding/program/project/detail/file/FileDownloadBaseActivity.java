package net.coding.program.project.detail.file;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.network.DownloadManagerPro;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.FileUtil;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.project.detail.AttachmentsActivity;

import org.apache.http.cookie.Cookie;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chenchao on 15/8/24.
 * 封装下载相关的模块
 */
public abstract class FileDownloadBaseActivity extends BackActivity implements WeakRefHander.Callback {

    private static String TAG = AttachmentsActivity.class.getSimpleName();

    private SharedPreferences share;
    private String defaultPath;
    private DownloadManager downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private DownloadChangeObserver downloadObserver;
    private MyHandler handler;
    private WeakRefHander mUpdateDownloadHandler;
    private ArrayList<AttachmentFileObject> downloadFiles;
    private SharedPreferences.Editor downloadListEditor;
    private SharedPreferences downloadList;

    abstract public void checkFileDownloadStatus();

    abstract public String getProjectPath();

    abstract public int getProjectId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new MyHandler();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManagerPro = new DownloadManagerPro(downloadManager);

        downloadList = getSharedPreferences(FileUtil.DOWNLOAD_LIST, Context.MODE_PRIVATE);
        downloadListEditor = downloadList.edit();

        /** register download success broadcast **/
        share = getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;

        mUpdateDownloadHandler = new WeakRefHander(this, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        /** observer download change **/
        if (downloadObserver == null)
            downloadObserver = new DownloadChangeObserver();
        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
        //updateView();

        checkFileDownloadStatus1();
        mUpdateDownloadHandler.start();
    }

    @Override
    public boolean handleMessage(Message msg) {

        checkFileDownloadStatus1();
        return true;
    }

    protected void updateFileDownloadStatus(AttachmentFileObject mFileObject) {
        if (mFileObject.downloadId != 0L) {
            mFileObject.bytesAndStatus = downloadManagerPro.getBytesAndStatus(mFileObject.downloadId);
            Log.v("updateFileDownloadStat", mFileObject.getName() + ":" + mFileObject.bytesAndStatus[0] + " " + mFileObject.bytesAndStatus[1] + " " + mFileObject.bytesAndStatus[2]);

            //handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
        }
    }

    private void checkFileDownloadStatus1() {
        if (downloadFiles == null || downloadFiles.isEmpty()) {
            mUpdateDownloadHandler.stop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(downloadObserver);
        mUpdateDownloadHandler.stop();
    }

    protected void action_download(ArrayList<AttachmentFileObject> mFilesArray) {
        final ArrayList<AttachmentFileObject> downloadFiles = new ArrayList<>();
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (fileObject.isSelected && !fileObject.isFolder) {
                File mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), fileObject.getSaveName(getProjectId()));
                if (mFile.exists() && mFile.isFile()) {
                    continue;
                }
                downloadFiles.add(fileObject);
            }
        }
        if (downloadFiles.size() == 0) {
            showButtomToast("没有选中文件");
            return;
        }
        if (!share.contains(FileUtil.DOWNLOAD_SETTING_HINT)) {
            String msgFormat = "您的文件将下载到以下路径：\n%s\n您也可以去设置界面设置您的下载路径";

            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(String.format(msgFormat, defaultPath))
                    .setPositiveButton("确定", (dialog, which) -> download(downloadFiles))
                    .show();

            SharedPreferences.Editor editor = share.edit();
            editor.putBoolean(FileUtil.DOWNLOAD_SETTING_HINT, true);
            editor.commit();
        } else {
            download(downloadFiles);
        }
    }

    protected void download(AttachmentFileObject mFileObject) {
        ArrayList<AttachmentFileObject> mFileObjects = new ArrayList<>();
        mFileObjects.add(mFileObject);
        download(mFileObjects);
    }

    protected void action_download_single(final AttachmentFileObject selectedFile) {
        if (selectedFile == null) {
            showButtomToast("没有选中文件");
            return;
        }
        File mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), selectedFile.getSaveName(getProjectId()));
        if (mFile.exists() && mFile.isFile()) {
            return;
        }
        if (!share.contains(FileUtil.DOWNLOAD_SETTING_HINT)) {
            String msgFormat = "您的文件将下载到以下路径：\n%s\n您也可以去设置界面设置您的下载路径";

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示")
                    .setMessage(String.format(msgFormat, defaultPath))
                    .setPositiveButton("确定", (dialog, which) -> download(selectedFile))
                    .show();

            SharedPreferences.Editor editor = share.edit();
            editor.putBoolean(FileUtil.DOWNLOAD_SETTING_HINT, true);
            editor.commit();
        } else {
            download(selectedFile);
        }
    }

    private void download(ArrayList<AttachmentFileObject> mFileObjects) {
        try {
            for (AttachmentFileObject mFileObject : mFileObjects) {
                final String urlDownload = Global.HOST_API + "%s/files/%s/download";
                String url = String.format(urlDownload, getProjectPath(), mFileObject.file_id);

                PersistentCookieStore cookieStore = new PersistentCookieStore(this);
                String cookieString = "";
                for (Cookie cookie : cookieStore.getCookies()) {
                    cookieString += cookie.getName() + "=" + cookie.getValue() + ";";
                }

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.addRequestHeader("Cookie", cookieString);
                request.setDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.getSaveName(getProjectId()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                request.setTitle(mFileObject.getName());
                request.setVisibleInDownloadsUi(false);


                long downloadId = downloadManager.enqueue(request);
                downloadListEditor.putLong(mFileObject.file_id + mFileObject.getHistory_id(), downloadId);
//                backgroundUpdate(downloadId);
            }
            downloadListEditor.commit();
            mUpdateDownloadHandler.start();
            checkFileDownloadStatus();
        } catch (Exception e) {
            Toast.makeText(this, R.string.no_system_download_service, Toast.LENGTH_LONG).show();
        }
    }

    public String getFileDownloadPath() {
        return FileSaveHelp.getFileDownloadPath(this);
    }

    protected void removeDownloadFile(long downloadId) {
        downloadManager.remove(downloadId);
        Log.d(TAG, "cancel:" + downloadId);
    }

    protected void downloadFileSuccess(String fileId) {
        downloadListEditor.remove(fileId);
        downloadListEditor.commit();
    }

    protected long getDownloadId(AttachmentFileObject projectFile) {
        return downloadList.getLong(projectFile.file_id + projectFile.getHistory_id(), 0);
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            checkFileDownloadStatus();
        }

    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    }
}
