package net.coding.program.project.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.ImagePagerFragment;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.FileUtil;
import net.coding.program.common.Global;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.network.DownloadManagerPro;
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
import org.androidannotations.annotations.ViewById;
import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EActivity(R.layout.activity_attachments_download)
public class AttachmentsDownloadDetailActivity extends BackActivity {

    private static final int STATE_NEEDDOWNLOAD = 0;
    private static final int STATE_STARTDOWNLOAD = 1;
    private static final int STATE_FINISHDOWNLOAD = 2;

    private static final int RESULT_SHARE_LINK = 1;
    private static final int RESULT_EDIT_FILE = 3;

    private static String TAG = AttachmentsDownloadDetailActivity.class.getSimpleName();
    @Extra
    int mProjectObjectId;
    @Extra
    AttachmentFileObject mAttachmentFileObject;
    @Extra
    AttachmentFolderObject mAttachmentFolderObject;
    @Extra
    ProjectObject mProject;

    @Extra
    boolean mHideHistoryLayout = false;

    @ViewById
    ImageView icon;
    @ViewById
    TextView iconTxt;
    @ViewById
    TextView name;
    @ViewById
    TextView content;
    @ViewById
    ProgressBar progressBar;
    @ViewById
    TextView btnDownload;
    @ViewById
    TextView btnOpen;
    @ViewById
    TextView btnLeft;
    @ViewById
    TextView btnRight;
    @ViewById
    LinearLayout mainLayout;
    @ViewById
    RelativeLayout rlDownload;
    @ViewById
    TextView tvDownload;
    @ViewById
    View blankLayout;
    @ViewById
    View layout_dynamic_history;

    String downloadFormat = "下载中...(%s/%s)";
    @ViewById
    ImageView ivDownloadCancel;
    String urlFiles = Global.HOST_API + "/project/%d/files/%s/view";
    //    String urlPages = Global.HOST_API + "/project/%d/files/image/%s?folderId=%s&orderByDesc=true";
    String urlDownload = Global.HOST_API + "/project/%d/files/%s/download";
    AttachmentFileObject mFileObject = new AttachmentFileObject();
    AsyncHttpClient client;
    File mFile;
    SharedPreferences.Editor downloadListEditor;
    boolean fileUrlSuccess = false;
    String fileInfoFormat =
            "文件类型: %s\n" +
                    "文件大小: %s\n" +
                    "创建时间: %s\n" +
                    "最近更新: %s\n" +
                    "创建人: %s";
    boolean isCanceled = false;
    private String HOST_FILE_DELETE = Global.HOST_API + "/project/%d/file/delete?fileIds=%s";
    private DownloadManager downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private long downloadId = 0L;
    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;
    private MyHandler handler;
    private SharedPreferences share;
    private SharedPreferences downloadList;
    private String defaultPath;

    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }

    public static void openFile(Activity activity, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型

        Uri uri = Uri.fromFile(file);
        //ContentResolver cR = getContentResolver();
        //String mime = cR.getType(uri);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        int index = file.getName().lastIndexOf('.') + 1;
        String ext = file.getName().substring(index).toLowerCase();
        String type = mime.getMimeTypeFromExtension(ext);

        //设置intent的data和Type属性。
        intent.setDataAndType(uri, type);
        //跳转
        try {
            activity.startActivity(intent);
            return;
        } catch (android.content.ActivityNotFoundException ex) {
        }
        Toast.makeText(activity, "没有能打开这个文件的应用", Toast.LENGTH_SHORT).show();
    }

    @Click
    final void ivDownloadCancel() {
        isCanceled = true;
        downloadManager.remove(downloadId);
        updateView();
    }

    @Click
    final void btnOpen() {
        //File mFile = getDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileObject.name);
        if (mFile.exists() && mFile.isFile())
            openFile(this, mFile);
        else {
            showButtomToast("无法打开，请重新下载");
            showState(STATE_NEEDDOWNLOAD);
        }
    }

    @Click
    final void btnDownload() {
        if (!share.contains(FileUtil.DOWNLOAD_SETTING_HINT)) {
            String msgFormat = "您的文件将下载到以下路径：\n%s\n您也可以去设置界面设置您的下载路径";

            AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDownloadDetailActivity.this);
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

    @OnActivityResult(RESULT_EDIT_FILE)
    protected void resultEditFile(int result, Intent intent) {
        setResult(result, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (fileUrlSuccess) {
            getMenuInflater().inflate(R.menu.project_attachment_download, menu);
            if (!mAttachmentFileObject.isOwner()) {
                menu.findItem(R.id.action_delete).setVisible(false);
            }
//        } else {
//            getMenuInflater().inflate(R.menu.menu_empty, menu);
//        }

        return super.onCreateOptionsMenu(menu);
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

    @OptionsItem
    protected final void action_delete() {
        String messageFormat = "确定要删除文件 \"%s\" 么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDownloadDetailActivity.this);
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

    @AfterViews
    protected final void initAttachmentsDownloadDetailActivity() {
        share = AttachmentsDownloadDetailActivity.this.getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;
        mFileObject = mAttachmentFileObject;

        if (mAttachmentFileObject != null) {
            File file = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mAttachmentFileObject.getSaveName(mProjectObjectId));
            if (file.exists() && file.isFile()) {
                jumpTextHtmlActivity();
                showState(STATE_FINISHDOWNLOAD);
            }
        }

        getSupportActionBar().setTitle(mAttachmentFileObject.getName());
        handler = new MyHandler();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManagerPro = new DownloadManagerPro(downloadManager);

        completeReceiver = new CompleteReceiver();
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadList = AttachmentsDownloadDetailActivity.this.getSharedPreferences(FileUtil.DOWNLOAD_LIST, Context.MODE_PRIVATE);
        downloadListEditor = downloadList.edit();

        client = MyAsyncHttpClient.createClient(AttachmentsDownloadDetailActivity.this);

//        urlPages = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id, mAttachmentFolderObject.file_id);
        urlFiles = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id);
        urlDownload = String.format(urlDownload, mProjectObjectId, mAttachmentFileObject.file_id);
        if (mAttachmentFileObject == null) {
            mainLayout.setVisibility(View.GONE);
            showDialogLoading();
            getFileUrlFromNetwork();
        } else {
            bindView();
        }

        if (mHideHistoryLayout) {
            layout_dynamic_history.setVisibility(View.INVISIBLE);
        }
    }

    private void jumpTextHtmlActivity() {
        if (mAttachmentFileObject.isMd()) {
            AttachmentsHtmlDetailActivity_
                    .intent(this)
                    .mProjectObjectId(mProjectObjectId)
                    .mAttachmentFolderObject(mAttachmentFolderObject)
                    .mAttachmentFileObject(mAttachmentFileObject)
                    .mHideHistory(mHideHistoryLayout)
                    .mProject(mProject)
                    .startForResult(RESULT_EDIT_FILE);
        } else if (mAttachmentFileObject.isTxt()) {
            AttachmentsTextDetailActivity_
                    .intent(this)
                    .mProjectObjectId(mProjectObjectId)
                    .mAttachmentFolderObject(mAttachmentFolderObject)
                    .mProject(mProject)
                    .mAttachmentFileObject(mAttachmentFileObject)
                    .mHideHistory(mHideHistoryLayout)
                    .startForResult(RESULT_EDIT_FILE);
        }
    }

    private void getFileUrlFromNetwork() {
        getNetwork(urlFiles, urlFiles);
    }

    private void bindView() {
        hideProgressDialog();

        icon.setVisibility(View.VISIBLE);
        icon.setImageResource(mFileObject.getIconResourceId());

        iconTxt.setVisibility(View.GONE);

        name.setText(mFileObject.getName());

        content.setText(Global.HumanReadableFilesize(mFileObject.getSize()));

        tvDownload.setText(String.format(downloadFormat, Global.HumanReadableFilesize(0.0), Global.HumanReadableFilesize(mFileObject.getSize())));
        progressBar.setMax(mFileObject.getSize());
        mainLayout.setVisibility(View.VISIBLE);

        mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.getSaveName(mProjectObjectId));
        Log.d(TAG, "downloadId:" + downloadId);

        File file = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mAttachmentFileObject.getSaveName(mProjectObjectId));
        if (file.exists() && file.isFile()) {
            showState(STATE_FINISHDOWNLOAD);
        } else {
            showState(STATE_NEEDDOWNLOAD);
        }
    }

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlFiles)) {
            if (code == 0) {
                fileUrlSuccess = true;
                invalidateOptionsMenu();

                JSONObject file = response.getJSONObject("data").getJSONObject("file");
                mFileObject = new AttachmentFileObject(file);
                mAttachmentFileObject = mFileObject;
                downloadId = downloadList.getLong(mFileObject.file_id, 0L);

                bindView();

                if (mFile.exists() && mFile.isFile()) {
                    Log.d(TAG, "mFile exists:");
                    if (downloadId != 0L) {
                        updateView();
                    }
                    showState(STATE_FINISHDOWNLOAD);
                } else {
                    Log.d(TAG, "mFile not exists:");
                    showState(STATE_NEEDDOWNLOAD);
                }

            } else {
                hideProgressDialog();
                showErrorMsg(code, response);
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
            }
        } else if (tag.equals(HOST_FILE_DELETE)) {
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
        return FileSaveHelp.getFileDownloadPath(this);
    }

    public void showState(int state) {
        switch (state) {
            case STATE_NEEDDOWNLOAD:
                btnDownload.setVisibility(View.VISIBLE);
                rlDownload.setVisibility(View.GONE);
                btnOpen.setVisibility(View.GONE);
                break;
            case STATE_STARTDOWNLOAD:
                btnDownload.setVisibility(View.GONE);
                rlDownload.setVisibility(View.VISIBLE);
                btnOpen.setVisibility(View.GONE);
                break;
            case STATE_FINISHDOWNLOAD:
                btnDownload.setVisibility(View.GONE);
                rlDownload.setVisibility(View.GONE);
                btnOpen.setVisibility(View.VISIBLE);
                layout_dynamic_history.setVisibility(View.VISIBLE);
                break;
        }

        if (mHideHistoryLayout) {
            layout_dynamic_history.setVisibility(View.INVISIBLE);
        }
    }

    private void download(String url) {
        try {
            mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.getSaveName(mProjectObjectId));

            PersistentCookieStore cookieStore = new PersistentCookieStore(AttachmentsDownloadDetailActivity.this);
            String cookieString = "";
            for (Cookie cookie : cookieStore.getCookies()) {
                cookieString += cookie.getName() + "=" + cookie.getValue() + ";";
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.addRequestHeader("Cookie", cookieString);
            request.setDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.getSaveName(mProjectObjectId));
            request.setTitle(mFileObject.getName());
            // request.setDescription(mFileObject.name);
            // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);
            // request.allowScanningByMediaScanner();
            // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            // request.setShowRunningNotification(false);

            downloadId = downloadManager.enqueue(request);
            downloadListEditor.putLong(mFileObject.file_id, downloadId);
            downloadListEditor.commit();
        } catch (Exception e) {
            Toast.makeText(this, R.string.no_system_download_service, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        /** observer download change **/
        if (downloadObserver == null)
            downloadObserver = new DownloadChangeObserver();
        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(downloadObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (completeReceiver != null) {
            unregisterReceiver(completeReceiver);
        }
    }

    public void updateView() {
        if (downloadId != 0L) {
            int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
            Log.v("updateView", bytesAndStatus[0] + " " + bytesAndStatus[1] + " " + bytesAndStatus[2]);

            handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
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
            mAttachmentFileObject = (AttachmentFileObject) intent.getSerializableExtra("data");
            setResult(result, intent);
        }
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

    protected void markUsed(RedPointTip.Type type) {
        RedPointTip.markUsed(this, type);
        updateRedPoinitStyle();
    }

    void updateRedPoinitStyle() {
        final int[] buttons = new int[]{
        };

        final RedPointTip.Type[] types = new RedPointTip.Type[]{
        };

        for (int i = 0; i < buttons.length; ++i) {
            setRedPointStyle(buttons[i], types[i]);
        }
    }

    protected void setRedPointStyle(int buttonId, RedPointTip.Type type) {
        View item = findViewById(buttonId);
        View redPoint = item.findViewById(R.id.badge);
        boolean show = RedPointTip.show(this, type);
        redPoint.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    int status = (Integer) msg.obj;
                    if (isDownloading(status)) {
                        showState(STATE_STARTDOWNLOAD);

                        if (msg.arg2 < 0) {
                            tvDownload.setText(String.format(downloadFormat, Global.HumanReadableFilesize(0.00), Global.HumanReadableFilesize(mFileObject.getSize())));
                            progressBar.setProgress(0);

                        } else {
                            tvDownload.setText(String.format(downloadFormat, Global.HumanReadableFilesize(msg.arg1), Global.HumanReadableFilesize(msg.arg2)));
                            progressBar.setProgress(msg.arg1);

                        }
                    } else {

                        if (status == DownloadManager.STATUS_FAILED) {
                            //downloadButton.setText(getString(R.string.app_status_download_fail));
                            showButtomToast("下载失败，请重试");
                            showState(STATE_NEEDDOWNLOAD);
                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloadListEditor.remove(mFileObject.file_id);
                            downloadListEditor.commit();
                            showState(STATE_FINISHDOWNLOAD);
                            downloadId = 0L;

//                            jumpTextHtmlActivity();
//                            if (mAttachmentFileObject.isMd())
//                            finish();
                            Intent intent = new Intent();
                            mAttachmentFileObject.isDownload = true;
                            intent.putExtra(AttachmentFileObject.RESULT, mAttachmentFileObject);
                            if (mAttachmentFileObject.needJump()) {
                                intent.putExtra(AttachmentsActivity.FileActions.ACTION_NAME,
                                        AttachmentsActivity.FileActions.ACTION_DOWNLOAD_OPEN);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                intent.putExtra(AttachmentsActivity.FileActions.ACTION_NAME,
                                        AttachmentsActivity.FileActions.ACTION_DOWNLOAD_OPEN);
                                setResult(RESULT_OK, intent);
                            }

                        } else {
                            showState(STATE_NEEDDOWNLOAD);
                        }
                    }
                    break;
            }
        }
    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                updateView();
                if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
                    //showButtomToast("Complete");
                }
            }
        }
    }

}
