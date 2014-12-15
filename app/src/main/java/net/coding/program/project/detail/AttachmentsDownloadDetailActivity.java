package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.FileUtil;
import net.coding.program.common.network.DownloadManagerPro;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

@EActivity(R.layout.activity_attachments_download)
@OptionsMenu(R.menu.project_attachment_download)
public class AttachmentsDownloadDetailActivity extends BaseFragmentActivity {

    private static String TAG = AttachmentsDownloadDetailActivity.class.getSimpleName();

    @Extra
    String mProjectObjectId;

    @Extra
    AttachmentFileObject mAttachmentFileObject;

    @Extra
    AttachmentFolderObject mAttachmentFolderObject;

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

    String downloadFormat = "下载中...(%s/%s)";

    @ViewById
    ImageView ivDownloadCancel;


    String urlFiles = Global.HOST + "/api/project/%s/files/%s/view";
    String urlPages = Global.HOST + "/api/project/%s/files/image/%s?folderId=%s&orderByDesc=true";
    String urlMdPreview = Global.HOST + "/api/markdown/preview";
    String urlDownload = Global.HOST + "/api/project/%s/files/%s/download";
    private String HOST_FILE_DELETE = Global.HOST + "/api/project/%s/file/delete?fileIds=%s";

    AttachmentFileObject mFileObject = new AttachmentFileObject();

    AsyncHttpClient client;

    private DownloadManager downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private long downloadId = 0L;
    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;

    private MyHandler handler;

    File mFile;

    private SharedPreferences share;
    private SharedPreferences downloadList;
    SharedPreferences.Editor downloadListEditor;
    private String defaultPath;

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

    //@OptionsItem
    protected void action_delete() {
        String messageFormat = "确定要删除文件 \"%s\" 么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsDownloadDetailActivity.this);
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

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(mAttachmentFileObject.name);
        handler = new MyHandler();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManagerPro = new DownloadManagerPro(downloadManager);

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        share = AttachmentsDownloadDetailActivity.this.getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;

        downloadList = AttachmentsDownloadDetailActivity.this.getSharedPreferences(FileUtil.DOWNLOAD_LIST, Context.MODE_PRIVATE);
        downloadListEditor = downloadList.edit();

        client = MyAsyncHttpClient.createClient(AttachmentsDownloadDetailActivity.this);
        mainLayout.setVisibility(View.GONE);
        urlFiles = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id);
        urlPages = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id, mAttachmentFolderObject.file_id);
        urlDownload = String.format(urlDownload, mProjectObjectId, mAttachmentFileObject.file_id);
        showDialogLoading();
        getNetwork(urlFiles, urlFiles);

        btnDownload.setOnClickListener(onDownloadListener);
        btnOpen.setOnClickListener(onOpenListener);
        ivDownloadCancel.setOnClickListener(onCancelListener);

    }

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlFiles)) {
            if (code == 0) {
                hideProgressDialog();
                JSONObject file = response.getJSONObject("data").getJSONObject("file");
                mFileObject = new AttachmentFileObject(file);
                downloadId = downloadList.getLong(mFileObject.file_id, 0L);


                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(mFileObject.getIconResourceId());

                iconTxt.setVisibility(View.GONE);

                name.setText(mFileObject.name);

                content.setText(Global.HumanReadableFilesize(mFileObject.size));

                tvDownload.setText(String.format(downloadFormat, Global.HumanReadableFilesize(0.0), Global.HumanReadableFilesize(mFileObject.size)));
                progressBar.setMax(mFileObject.size);
                mainLayout.setVisibility(View.VISIBLE);

                mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.name);
                Log.d(TAG, "downloadId:" + downloadId);
                if (mFile.exists() && mFile.isFile() && mFile.length() == mFileObject.size) {
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
        String path;
        if (share.contains(FileUtil.DOWNLOAD_PATH)) {
            path = share.getString(FileUtil.DOWNLOAD_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER);
        } else {
            path = defaultPath;
        }
        return path;
    }

    private static final int STATE_NEEDDOWNLOAD = 0;
    private static final int STATE_STARTDOWNLOAD = 1;
    private static final int STATE_FINISHDOWNLOAD = 2;

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
                break;
        }
    }

    View.OnClickListener onDownloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
    };

    View.OnClickListener onOpenListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //File mFile = getDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileObject.name);
            if (mFile.exists() && mFile.isFile())
                openFile(mFile);
            else {
                showButtomToast("无法打开，请重新下载");
                showState(STATE_NEEDDOWNLOAD);
            }
        }
    };

    boolean isCanceled = false;

    View.OnClickListener onCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isCanceled = true;
            downloadManager.remove(downloadId);
            updateView();

        }
    };

    private void download(String url) {

        mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.name);

        PersistentCookieStore cookieStore = new PersistentCookieStore(AttachmentsDownloadDetailActivity.this);
        String cookieString = "";
        for (Cookie cookie : cookieStore.getCookies()) {
            cookieString += cookie.getName() + "=" + cookie.getValue() + ";";
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.addRequestHeader("Cookie", cookieString);
        request.setDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.name);
        request.setTitle(mFileObject.name);
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

    }

    private void openFile(File file) {

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
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            showButtomToast("没有能打开这个文件的应用");
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
        unregisterReceiver(completeReceiver);
    }

    public void updateView() {
        if (downloadId != 0L) {
            int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
            Log.v("updateView", bytesAndStatus[0] + " " + bytesAndStatus[1] + " " + bytesAndStatus[2]);

            handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
        }
    }

    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
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


    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow = null;

    public void initRightTopPop() {
        if (mRightTopPopupWindow == null) {
            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList<DialogUtil.RightTopPopupItem>();
            //DialogUtil.RightTopPopupItem moveItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_move), R.drawable.ic_menu_move);
            //popupItemArrayList.add(moveItem);
            DialogUtil.RightTopPopupItem deleteItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_delete), R.drawable.ic_menu_delete_selector);
            popupItemArrayList.add(deleteItem);
            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(AttachmentsDownloadDetailActivity.this, popupItemArrayList, onRightTopPopupItemClickListener);
        }
    }

    public void showRightTopPop() {

        if (mRightTopPopupWindow == null) {
            initRightTopPop();
        }

        //DialogUtil.RightTopPopupItem moveItem = mRightTopPopupWindow.adapter.getItem(0);
        DialogUtil.RightTopPopupItem deleteItem = mRightTopPopupWindow.adapter.getItem(0);

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
        mRightTopPopupWindow.showAtLocation(icon, Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);

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
                            tvDownload.setText(String.format(downloadFormat, Global.HumanReadableFilesize(0.00), Global.HumanReadableFilesize(mFileObject.size)));
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
                        } else {
                            showState(STATE_NEEDDOWNLOAD);
                        }
                    }
                    break;
            }
        }
    }

    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
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

    ;

}
