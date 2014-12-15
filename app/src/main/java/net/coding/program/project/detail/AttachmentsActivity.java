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
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.FileUtil;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.network.DownloadManagerPro;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 展示某一项目文档目录下面文件的Activity
 * 原本没有二级目录，这个Activity是只用来处理AttachmentFileObject的
 * 之后加了二级目录，那么有些实现方式就不太合适了
 * Created by yangzhen
 */
@EActivity(R.layout.activity_attachments)
//@OptionsMenu(R.menu.project_attachment_file)
public class AttachmentsActivity extends BaseActivity implements FootUpdate.LoadMore {
    private static String TAG = AttachmentsActivity.class.getSimpleName();
    @Extra
    String mProjectObjectId;

    @Extra
    AttachmentFolderObject mAttachmentFolderObject;

    String urlFiles = Global.HOST + "/api/project/%s/files/%s?height=90&width=90&pageSize=9999";
    String urlUpload = Global.HOST + "/api/project/%s/file/upload";
    private String HOST_FILE_DELETE = Global.HOST + "/api/project/%s/file/delete?%s";
    private String HOST_FILE_MOVETO = Global.HOST + "/api/project/%s/files/moveto/%s?%s";

    private String HOST_FILECOUNT = Global.HOST + "/api/project/%s/folders/all_file_count";

    private String HOST_FOLDER_NAME = Global.HOST + "/api/project/%s/dir/%s/name/%s";
    private String HOST_FOLDER_NEW = Global.HOST + "/api/project/%s/mkdir";
    private String HOST_FOLDER_DELETE_FORMAT = Global.HOST + "/api/project/%s/rmdir/%s";
    private String HOST_FOLDER_DELETE;

    private String urlDownload = Global.HOST + "/api/project/%s/files/%s/download";

    private HashMap<String, Integer> fileCountMap = new HashMap<String, Integer>();
    //var EDITABLE_FILE_REG=/\.(txt|md|html|htm)$/
    // /\.(pdf)$/

    private String descTemplate = "发布于%s";

    ArrayList<AttachmentFileObject> mFilesArray = new ArrayList<AttachmentFileObject>();

    boolean mNoMore = false;

    private DownloadManager downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;

    private MyHandler handler;

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @ViewById
    ListView listView;

    @ViewById
    RelativeLayout uploadLayout;

    private SharedPreferences share;
    private SharedPreferences downloadList;
    SharedPreferences.Editor downloadListEditor;
    private String defaultPath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_attachment_file, menu);

        if (!mAttachmentFolderObject.parent_id.equals("0") || mAttachmentFolderObject.file_id.equals("0"))
            menu.findItem(R.id.action_new_folder).setVisible(false);
        return true;
    }

    @AfterViews
    void init() {
        uploadLayout.setVisibility(View.GONE);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(mAttachmentFolderObject.name);

        handler = new MyHandler();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManagerPro = new DownloadManagerPro(downloadManager);

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadList = getSharedPreferences(FileUtil.DOWNLOAD_LIST, Context.MODE_PRIVATE);
        downloadListEditor = downloadList.edit();

        urlFiles = String.format(urlFiles, mProjectObjectId, mAttachmentFolderObject.file_id);
        urlUpload = String.format(urlUpload, mProjectObjectId);
        barParams = (LinearLayout.LayoutParams) uploadStatusProgress.getLayoutParams();
        barParamsRemain = (LinearLayout.LayoutParams) uploadStatusProgressRemain.getLayoutParams();

        HOST_FILECOUNT = String.format(HOST_FILECOUNT, mProjectObjectId);

        share = AttachmentsActivity.this.getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AttachmentFileObject data = mFilesArray.get(position);

                if (isEditMode) {
                    if (!data.isFolder) {
                        data.isSelected = !data.isSelected;
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (data.isFolder) {
                        AttachmentsActivity_.intent(AttachmentsActivity.this).mAttachmentFolderObject(data.folderObject).mProjectObjectId(mProjectObjectId).startForResult(ProjectAttachmentFragment.RESULT_REQUEST_FILES);
                    } else if (data.isImage()) {
                        AttachmentsPicDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).fileList(getPicFiles()).startForResult(FILE_DELETE_CODE);
                    } else if (data.isHtml() || data.isMd()) {
                        AttachmentsHtmlDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                    } else if (data.isTxt()) {
                        AttachmentsTextDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                    } else {
                        AttachmentsDownloadDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                    }
                }

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showPop(view, position);
                return true;
            }
        });

        initBottomPop();

        loadMore();
    }

    /**
     * 获取当前文档列表中的所有图片文档，提供给AttachmentsPicDetailActivity
     *
     * @return 当前文档列表中的所有图片文档
     */
    private ArrayList<AttachmentFileObject> getPicFiles() {
        ArrayList<AttachmentFileObject> picFiles = new ArrayList<AttachmentFileObject>();
        for (AttachmentFileObject file : mFilesArray) {
            if (file.isImage()) {
                picFiles.add(file);
            }
        }
        return picFiles;
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(urlFiles, urlFiles);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlFiles)) {
            if (code == 0) {
                JSONArray files = respanse.getJSONObject("data").getJSONArray("list");

                if (mFilesArray.size() == 0) {
                    ArrayList<AttachmentFolderObject> subFolders = mAttachmentFolderObject.sub_folders;
                    for (AttachmentFolderObject subFolder : subFolders) {
                        mFilesArray.add(AttachmentFileObject.parseFileObject(subFolder));
                    }

                }

                for (int i = 0; i < files.length(); ++i) {
                    AttachmentFileObject fileObject = new AttachmentFileObject(files.getJSONObject(i));

                    setDownloadStatus(fileObject);
                    mFilesArray.add(fileObject);
                }

                int page = respanse.getJSONObject("data").optInt("page");
                int totalPage = respanse.getJSONObject("data").optInt("totalPage");
                if (page == totalPage)
                    mNoMore = true;
                adapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FILE_DELETE)) {
            if (code == 0) {
                hideProgressDialog();
                showButtomToast("删除完成");
                mFilesArray.removeAll(selectFile);
                adapter.notifyDataSetChanged();
                setResult(Activity.RESULT_OK);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FILE_MOVETO)) {
            if (code == 0) {
                showButtomToast("移动成功");
                mFilesArray.removeAll(selectFile);
                adapter.notifyDataSetChanged();
                setResult(Activity.RESULT_OK);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FILECOUNT)) {
            if (code == 0) {
                JSONArray counts = respanse.getJSONArray("data");

                for (int i = 0; i < counts.length(); ++i) {
                    JSONObject countItem = counts.optJSONObject(i);
                    fileCountMap.put(countItem.optString("folder"), countItem.optInt("count"));
                }

                for (AttachmentFileObject fileObject : mFilesArray) {
                    if (fileObject.isFolder) {
                        fileObject.folderObject.setCount(fileCountMap.get(fileObject.folderObject.file_id));
                        fileObject.name = fileObject.folderObject.getNameCount();
                    }
                }
                adapter.notifyDataSetChanged();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FOLDER_NAME)) {
            if (code == 0) {
                showButtomToast("重命名成功");
                AttachmentFileObject folderObject = mFilesArray.get(pos);

                folderObject.folderObject.name = (String) data;
                folderObject.name = folderObject.folderObject.getNameCount();
                adapter.notifyDataSetChanged();
                //mData.clear();
                //AttachmentFolderObject folderObject = (AttachmentFolderObject)data;
                //loadMore();
            } else {
                showButtomToast("重命名失败");
            }
        } else if (tag.equals(HOST_FOLDER_DELETE)) {
            if (code == 0) {
                //setRefreshing(false);
                AttachmentFileObject folderObject = mFilesArray.get(pos);
                mFilesArray.remove(pos);
                selectFolder.remove(0);
                if (selectFolder.size() > 0) {
                    deleteFolders();
                } else {
                    showButtomToast("删除完成");
                    adapter.notifyDataSetChanged();
                }
                setResult(Activity.RESULT_OK);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FOLDER_NEW)) {
            if (code == 0) {
                AttachmentFolderObject folder = new AttachmentFolderObject(respanse.getJSONObject("data"));
                mAttachmentFolderObject.sub_folders.add(0, folder);
                mFilesArray.add(0, AttachmentFileObject.parseFileObject(folder));
                adapter.notifyDataSetChanged();
                setResult(Activity.RESULT_OK);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    final public static int FILE_SELECT_CODE = 10;
    final public static int FILE_DELETE_CODE = 11;
    final public static int FILE_MOVE_CODE = 12;

    private boolean isUploading = false;

    @OptionsItem
    void action_upload() {
        if (isUploading) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            showButtomToast("请安装文件管理器");
        }
    }

    @OnActivityResult(FILE_SELECT_CODE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtil.getPath(this, uri);
            File selectedFile = new File(path);
            uploadFile(selectedFile);
        }
    }

    private String uploadHitLeftFormat = "正在上传%s项";
    private String uploadHitMiddleFormat = "%s%%";
    private String uploadHitRightFormat = "%s/S";
    private String uploadHitCompleteFormat = "上传完成，本次共上传%s个文件";
    @ViewById
    ImageView uploadCloseBtn;
    @ViewById
    TextView uploadDoneHint;
    @ViewById
    TextView uploadLeftHint;
    @ViewById
    TextView uploadRightHint;
    @ViewById
    TextView uploadMiddleHint;
    @ViewById
    RelativeLayout uploadDoneLayout;
    @ViewById
    RelativeLayout uploadStatusLayout;
    @ViewById
    ImageView uploadStatusProgress;
    @ViewById
    ImageView uploadStatusProgressRemain;
    @ViewById
    RelativeLayout uploadFailureLayout;
    @ViewById
    ImageView uploadFailureCloseBtn;

    LinearLayout.LayoutParams barParams;
    LinearLayout.LayoutParams barParamsRemain;

    private void uploadFile(File selectedFile) {

        try {
            RequestParams params = new RequestParams();
            params.put("dir", mAttachmentFolderObject.file_id);
            params.put("file", selectedFile);

            isUploading = true;

            showUploadStatus(UPLOAD_STATUS_UPLOADING);

            AsyncHttpClient client = MyAsyncHttpClient.createClient(AttachmentsActivity.this);

            JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.v(TAG, "onSuccess");
                    try {
                        int code = response.getInt("code");

                        if (code == 1000) {
                            AttachmentsActivity.this.startActivity(new Intent(AttachmentsActivity.this, LoginActivity_.class));
                        }
                        if (code == 0) {
                            AttachmentFileObject newFile = new AttachmentFileObject(response.getJSONObject("data"));
                            setDownloadStatus(newFile);
                            mFilesArray.add(mAttachmentFolderObject.sub_folders.size(), newFile);
                            adapter.notifyDataSetChanged();
                            setResult(Activity.RESULT_OK);
                            showUploadStatus(UPLOAD_STATUS_FINISH);
                        } else {
                            showErrorMsg(code, response);
                        }

                    } catch (Exception e) {
                        Global.errorLog(e);
                    }
                }

                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.v(TAG, "onFailure");
                    try {
                        showErrorMsg(NetworkImpl.NETWORK_ERROR, errorResponse);
                        showUploadStatus(UPLOAD_STATUS_FAILURE);
                    } catch (Exception e) {
                        Global.errorLog(e);
                    }
                }

                @Override
                public void onFinish() {
                    Log.v(TAG, "onFinish");
                    isUploading = false;
                }

                @Override
                public void onProgress(int bytesWritten, int totalSize) {
                    Log.v(TAG, String.format("Progress %d from %d (%2.0f%%)", bytesWritten, totalSize, (totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1));
                    setUploadStatus(bytesWritten, totalSize);
                }
            };
            client.post(urlUpload, params, jsonHttpResponseHandler);

        } catch (FileNotFoundException e) {
            showButtomToast("文件未找到");
        }

    }


    private final int UPLOAD_STATUS_UPLOADING = 0;
    private final int UPLOAD_STATUS_FINISH = 1;
    private final int UPLOAD_STATUS_CLOSE = 2;
    private final int UPLOAD_STATUS_FAILURE = 3;

    private long uploadStartTime = 0l;

    private void showUploadStatus(int status) {
        switch (status) {
            case UPLOAD_STATUS_UPLOADING:
                uploadFailureLayout.setVisibility(View.GONE);
                barParams.weight = 0;
                uploadStatusProgress.requestLayout();
                uploadMiddleHint.setText(String.format(uploadHitMiddleFormat, 0));
                barParamsRemain.weight = 100;
                uploadStatusProgressRemain.requestLayout();
                uploadDoneLayout.setVisibility(View.GONE);
                uploadStatusLayout.setVisibility(View.VISIBLE);
                uploadLayout.setVisibility(View.VISIBLE);
                uploadStartTime = System.currentTimeMillis();
                break;
            case UPLOAD_STATUS_FINISH:
                uploadFailureLayout.setVisibility(View.GONE);
                uploadDoneLayout.setVisibility(View.VISIBLE);
                uploadStatusLayout.setVisibility(View.GONE);
                uploadLayout.setVisibility(View.VISIBLE);
                barParams.weight = 100;
                uploadStatusProgress.requestLayout();
                barParamsRemain.weight = 0;
                uploadStatusProgressRemain.requestLayout();
                break;
            case UPLOAD_STATUS_FAILURE:
                uploadFailureLayout.setVisibility(View.VISIBLE);
                uploadDoneLayout.setVisibility(View.GONE);
                uploadStatusLayout.setVisibility(View.GONE);
                uploadLayout.setVisibility(View.VISIBLE);
                barParams.weight = 100;
                uploadStatusProgress.requestLayout();
                barParamsRemain.weight = 0;
                uploadStatusProgressRemain.requestLayout();
                break;
            case UPLOAD_STATUS_CLOSE:
                uploadLayout.setVisibility(View.GONE);
                break;

        }
    }

    @Click({R.id.uploadCloseBtn, R.id.uploadFailureCloseBtn})
    void closeUploadBar() {
        showUploadStatus(UPLOAD_STATUS_CLOSE);
    }

    private void setUploadStatus(int bytesWritten, int totalSize) {
        long uploadCurTime = System.currentTimeMillis();
        int progress = (int) ((totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1);//bytesWritten * 100 /totalSize ;
        uploadMiddleHint.setText(String.format(uploadHitMiddleFormat, progress));
        barParams.weight = progress;
        uploadStatusProgress.requestLayout();
        barParamsRemain.weight = 100 - progress;
        uploadStatusProgressRemain.requestLayout();
        uploadRightHint.setText(String.format(uploadHitRightFormat, Global.HumanReadableFilesize(bytesWritten / (uploadCurTime - uploadStartTime) * 1000)));
        Log.d(TAG, barParams.weight + " " + barParamsRemain.weight + " " + (bytesWritten / (uploadCurTime - uploadStartTime) * 1000) + " " + String.format(uploadHitRightFormat, Global.HumanReadableFilesize(bytesWritten / (uploadCurTime - uploadStartTime) * 1000)));
    }

    @OnActivityResult(FILE_DELETE_CODE)
    void onFileResult(int resultCode, Intent data) {
        Log.d("onFileResult", resultCode + "");

        if (resultCode == Activity.RESULT_OK) {
            AttachmentFileObject deletedFileObject = (AttachmentFileObject) data.getSerializableExtra("mAttachmentFileObject");
            Log.d("onFileResult", resultCode + " " + deletedFileObject.name);
            for (AttachmentFileObject file : mFilesArray) {
                if (file.file_id.equals(deletedFileObject.file_id)) {
                    mFilesArray.remove(file);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
            setResult(Activity.RESULT_OK);
        }
    }

    @OnActivityResult(ProjectAttachmentFragment.RESULT_REQUEST_FILES)
    void onFolderResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            getNetwork(HOST_FILECOUNT, HOST_FILECOUNT);
        }
    }

    private boolean isEditMode = false;
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mFilesArray.size();
        }

        @Override
        public Object getItem(int position) {
            return mFilesArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.project_attachment_file_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon_txt = (TextView) convertView.findViewById(R.id.icon_txt);
                holder.content = (TextView) convertView.findViewById(R.id.comment);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                //holder.btn = (TextView) convertView.findViewById(R.id.btn_right);

                holder.file_info_layout = (LinearLayout) convertView.findViewById(R.id.file_info_layout);
                holder.folder_name = (TextView) convertView.findViewById(R.id.folder_name);

                holder.more = (RelativeLayout) convertView.findViewById(R.id.more);

                holder.downloadIcon = (ImageView) convertView.findViewById(R.id.downloadIcon);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.bottomLine = convertView.findViewById(R.id.bottomLine);

                holder.icon_layout = (RelativeLayout) convertView.findViewById(R.id.icon_layout);

                holder.desc_layout = (LinearLayout) convertView.findViewById(R.id.desc_layout);
                holder.progress_layout = (LinearLayout) convertView.findViewById(R.id.progress_layout);
                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
                holder.cancel = (TextView) convertView.findViewById(R.id.cancel);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AttachmentFileObject data = mFilesArray.get(position);
            holder.name.setText(data.name);

            if (data.isFolder) {
                holder.icon.setImageResource(R.drawable.ic_project_attachment_folder);
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setBackgroundResource(android.R.color.transparent);
                holder.icon_txt.setVisibility(View.GONE);
                holder.file_info_layout.setVisibility(View.GONE);
                holder.folder_name.setText(data.name);
                holder.folder_name.setVisibility(View.VISIBLE);
            } else if (data.isImage()) {
                //Log.d("imagePattern", "data.preview:" + data.preview);
                imagefromNetwork(holder.icon, data.preview, ImageLoadTool.optionsRounded);
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setBackgroundResource(R.drawable.shape_image_icon_bg);
                holder.icon_txt.setVisibility(View.GONE);
                holder.file_info_layout.setVisibility(View.VISIBLE);
                holder.folder_name.setVisibility(View.GONE);
            } else {
                imagefromNetwork(holder.icon, "drawable://" + data.getIconResourceId(), ImageLoadTool.optionsRounded);
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setBackgroundResource(android.R.color.transparent);
                holder.icon_txt.setVisibility(View.GONE);
                holder.file_info_layout.setVisibility(View.VISIBLE);
                holder.folder_name.setVisibility(View.GONE);
            }

            /*holder.btn.setTag(data);
            holder.btn.setOnClickListener(btnClickListener);

            if (data.isImage() || data.isHtml() || data.isMd() || data.isTxt()) {
                //holder.btn.setVisibility(View.VISIBLE);
                holder.btn.setText("查看");
            } else {
                //holder.btn.setVisibility(View.GONE);
                holder.btn.setText("下载");
            }*/

            holder.content.setText(Global.HumanReadableFilesize(data.size));
            holder.desc.setText(String.format(descTemplate, Global.dayToNow(data.created_at)));
            holder.username.setText(data.owner.name);

            if (position == mFilesArray.size() - 1) {
                if (!mNoMore) {
                    loadMore();
                }
            }

            holder.checkBox.setTag(new Integer(position));
            if (isEditMode) {
                if (!data.isFolder)
                    holder.checkBox.setVisibility(View.VISIBLE);
                else
                    holder.checkBox.setVisibility(View.INVISIBLE);

                if (data.isSelected) {
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setChecked(false);
                }
                //((RelativeLayout.LayoutParams) holder.bottomLine.getLayoutParams()).addRule(RelativeLayout.LEFT_OF, R.id.icon);
                ((RelativeLayout.LayoutParams) holder.bottomLine.getLayoutParams()).leftMargin = Global.dpToPx(58);
            } else {
                holder.checkBox.setVisibility(View.GONE);
                //((RelativeLayout.LayoutParams) holder.bottomLine.getLayoutParams()).removeRule(RelativeLayout.LEFT_OF);
                ((RelativeLayout.LayoutParams) holder.bottomLine.getLayoutParams()).leftMargin = 0;
            }
            holder.checkBox.setOnCheckedChangeListener(onCheckedChangeListener);

            holder.more.setOnClickListener(onMoreClickListener);
            holder.more.setTag(new Integer(position));


            if (data.downloadId != 0L) {
                holder.cancel.setTag(new Integer(position));
                int status = data.bytesAndStatus[2];
                holder.progressBar.setMax(data.size);
                if (AttachmentsDownloadDetailActivity.isDownloading(status)) {
                    if (data.bytesAndStatus[1] < 0) {
                        holder.progressBar.setProgress(0);
                    } else {
                        holder.progressBar.setProgress(data.bytesAndStatus[0]);

                    }
                    data.isDownload = false;
                    holder.desc_layout.setVisibility(View.GONE);
                    holder.content.setVisibility(View.GONE);
                    holder.more.setVisibility(View.GONE);
                    holder.progress_layout.setVisibility(View.VISIBLE);
                } else {
                    if (status == DownloadManager.STATUS_FAILED) {
                        data.isDownload = false;
                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        data.isDownload = true;
                        downloadListEditor.remove(data.file_id);
                        downloadListEditor.commit();
                    } else {
                        data.isDownload = false;
                    }

                    data.downloadId = 0L;

                    holder.desc_layout.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.more.setVisibility(View.VISIBLE);
                    holder.progress_layout.setVisibility(View.GONE);
                }
            } else {
                holder.desc_layout.setVisibility(View.VISIBLE);
                holder.content.setVisibility(View.VISIBLE);
                holder.more.setVisibility(View.VISIBLE);
                holder.progress_layout.setVisibility(View.GONE);
            }

            if (data.isDownload) {
                holder.downloadIcon.setImageResource(R.drawable.ic_attachment_state_1);
            } else {
                holder.downloadIcon.setImageResource(R.drawable.ic_attachment_state_0);
            }

            holder.cancel.setOnClickListener(cancelClickListener);

            return convertView;
        }

        /*private View.OnClickListener btnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachmentFileObject data = (AttachmentFileObject) v.getTag();
                if (data.isImage()) {
                    AttachmentsPicDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                } else if (data.isHtml() || data.isMd()) {
                    AttachmentsHtmlDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                } else if (data.isTxt()) {
                    AttachmentsTextDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                } else {
                    AttachmentsDownloadDetailActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).mAttachmentFolderObject(mAttachmentFolderObject).mAttachmentFileObject(data).startForResult(FILE_DELETE_CODE);
                }
            }
        };*/

        private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AttachmentFileObject data = mFilesArray.get((Integer) buttonView.getTag());
                data.isSelected = isChecked;
            }
        };
        private View.OnClickListener onMoreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPop(view, (Integer) view.getTag());
            }
        };

        private View.OnClickListener cancelClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachmentFileObject data = mFilesArray.get((Integer) v.getTag());

                long downloadId = data.downloadId;
                Log.d(TAG, "cancel:" + downloadId);
                downloadManager.remove(downloadId);
                data.downloadId = 0L;
                adapter.notifyDataSetChanged();
            }
        };

    };

    static class ViewHolder {
        ImageView icon;
        TextView icon_txt;
        TextView name;
        TextView content;
        TextView desc;
        //TextView btn;

        LinearLayout file_info_layout;
        TextView folder_name;

        CheckBox checkBox;

        RelativeLayout more;

        ImageView downloadIcon;
        TextView username;
        View bottomLine;

        RelativeLayout icon_layout;

        LinearLayout desc_layout, progress_layout;
        ProgressBar progressBar;
        TextView cancel;
    }

    @OptionsItem
    void action_edit() {
        doEdit();
    }

    ActionMode mActionMode;

    private void doEdit() {
        if (mActionMode != null) {
            return;
        }

        mActionMode = startActionMode(mActionModeCallback);
        setListEditMode(true);
    }

    private void setListEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        adapter.notifyDataSetChanged();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();

            inflater.inflate(R.menu.project_attachment_file_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;// Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                /*case R.id.action_delete:
                    action_delete();
                    return true;*/
                case R.id.action_all:
                    action_all();
                    return true;
                case R.id.action_inverse:
                    action_inverse();
                    return true;
                /*case R.id.action_move:
                    action_move();
                    return true;*/
                case R.id.action_more:
                    showRightTopPop();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            setListEditMode(false);
        }
    };

    ArrayList<AttachmentFileObject> selectFile;

    /**
     * 删除选中的文件
     */
    void action_delete() {
        selectFile = new ArrayList<AttachmentFileObject>();
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (fileObject.isSelected && !fileObject.isFolder)
                selectFile.add(fileObject);
        }
        if (selectFile.size() == 0) {
            showButtomToast("没有选中文件");
            return;
        }
        String messageFormat = "确定要删除%s个文件么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsActivity.this);
        builder.setTitle("删除文件").setMessage(String.format(messageFormat, selectFile.size()))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFiles();
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

    /**
     * 删除单个选中的文件
     *
     * @param selectedFile 当前选中的文件
     */
    void action_delete_single(AttachmentFileObject selectedFile) {
        if (selectedFile == null) {
            showButtomToast("没有选中文件");
            return;
        }
        selectFile = new ArrayList<AttachmentFileObject>();
        selectFile.add(selectedFile);

        String messageFormat = "确定要删除文件 \"%s\" 么？";
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsActivity.this);
        builder.setTitle("删除文件").setMessage(String.format(messageFormat, selectedFile.name))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFiles();
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

    void deleteFiles() {
        String param = "";
        int i = 0;
        for (AttachmentFileObject file : selectFile) {
            if (i > 0) {
                param += "&";
            }
            param += "fileIds=" + file.file_id;
            i++;
        }
        if (selectFile.size() > 0) {
            deleteNetwork(String.format(HOST_FILE_DELETE, mProjectObjectId, param), HOST_FILE_DELETE);
        }
    }

    void action_all() {
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (!fileObject.isFolder)
                fileObject.isSelected = true;
        }
        adapter.notifyDataSetChanged();
    }

    void action_inverse() {
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (!fileObject.isFolder)
                fileObject.isSelected = !fileObject.isSelected;
        }
        adapter.notifyDataSetChanged();
    }

    void action_move() {
        selectFile = new ArrayList<AttachmentFileObject>();
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (fileObject.isSelected && !fileObject.isFolder)
                selectFile.add(fileObject);
        }
        //showButtomToast("selected count:" + selectFolder.size());
        if (selectFile.size() == 0) {
            showButtomToast("没有选中文件");
            return;
        }

        showMoveDialog();
    }

    void action_move_single(AttachmentFileObject selectedFile) {
        if (selectedFile == null) {
            showButtomToast("没有选中文件");
            return;
        }
        selectFile = new ArrayList<AttachmentFileObject>();
        selectFile.add(selectedFile);

        showMoveDialog();
    }

    /**
     * 显示文档目录选择界面
     */
    private void showMoveDialog() {
        AttachmentsFolderSelectorActivity_.intent(AttachmentsActivity.this).mProjectObjectId(mProjectObjectId).startForResult(FILE_MOVE_CODE);
    }

    @OnActivityResult(FILE_MOVE_CODE)
    void onFileMoveResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AttachmentFolderObject selectedFolder = (AttachmentFolderObject) data.getSerializableExtra("mAttachmentFolderObject");
            String param = "";
            if (selectedFolder.file_id.equals(mAttachmentFolderObject.file_id)) {
                return;
            }
            int i = 0;
            for (AttachmentFileObject file : selectFile) {
                if (i > 0) {
                    param += "&";
                }
                param += "fileId=" + file.file_id;
                i++;
            }
            putNetwork(String.format(HOST_FILE_MOVETO, mProjectObjectId, selectedFolder.file_id, param), null, HOST_FILE_MOVETO);

        }
    }

    /**
     * 弹出框
     */
    private DialogUtil.BottomPopupWindow mAttachmentPopupWindow = null;//文档目录的底部弹出框
    private DialogUtil.BottomPopupWindow mAttachmentFilePopupWindow = null;//文档文件的底部弹出框
    private int selectedPosition;

    public void initBottomPop() {
        if (mAttachmentPopupWindow == null) {
            ArrayList<DialogUtil.BottomPopupItem> popupItemArrayList = new ArrayList<DialogUtil.BottomPopupItem>();
            DialogUtil.BottomPopupItem renameItem = new DialogUtil.BottomPopupItem("重命名", R.drawable.ic_popup_attachment_rename);
            popupItemArrayList.add(renameItem);
            DialogUtil.BottomPopupItem deleteItem = new DialogUtil.BottomPopupItem("删除", R.drawable.ic_popup_attachment_delete_selector);
            popupItemArrayList.add(deleteItem);
            mAttachmentPopupWindow = DialogUtil.initBottomPopupWindow(AttachmentsActivity.this, "", popupItemArrayList, onPopupItemClickListener);
        }

        if (mAttachmentFilePopupWindow == null) {
            ArrayList<DialogUtil.BottomPopupItem> popupItemArrayList = new ArrayList<DialogUtil.BottomPopupItem>();
            DialogUtil.BottomPopupItem moveItem = new DialogUtil.BottomPopupItem("移动", R.drawable.ic_popup_attachment_move_selector);
            popupItemArrayList.add(moveItem);

            DialogUtil.BottomPopupItem downloadItem = new DialogUtil.BottomPopupItem("下载", R.drawable.ic_popup_attachment_download_selector);
            popupItemArrayList.add(downloadItem);

            DialogUtil.BottomPopupItem deleteItem = new DialogUtil.BottomPopupItem("删除", R.drawable.ic_popup_attachment_delete_selector);
            popupItemArrayList.add(deleteItem);
            mAttachmentFilePopupWindow = DialogUtil.initBottomPopupWindow(AttachmentsActivity.this, "", popupItemArrayList, onFilePopupItemClickListener);
        }
    }

    public void showPop(View view, int position) {
        if (mAttachmentPopupWindow == null) {
            initBottomPop();
        }
        selectedPosition = position;
        AttachmentFileObject selectedFileObject = mFilesArray.get(selectedPosition);
        if (selectedFileObject.isFolder) {
            AttachmentFolderObject selectedFolderObject = selectedFileObject.folderObject;

            if (mAttachmentPopupWindow == null) {
                initBottomPop();
            }

            DialogUtil.BottomPopupItem renameItem = mAttachmentPopupWindow.adapter.getItem(0);
            DialogUtil.BottomPopupItem deleteItem = mAttachmentPopupWindow.adapter.getItem(1);
            if (selectedFolderObject.file_id.equals("0")) {
                renameItem.enabled = false;
                deleteItem.enabled = false;
            } else if (selectedFolderObject.count != 0) {
                renameItem.enabled = true;
                deleteItem.enabled = false;
            } else {
                renameItem.enabled = true;
                deleteItem.enabled = true;
            }
            mAttachmentPopupWindow.adapter.notifyDataSetChanged();
            mAttachmentPopupWindow.tvTitle.setText(selectedFolderObject.name);

            mAttachmentPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        } else {
            if (selectedFileObject.downloadId != 0L)
                return;
            if (mAttachmentFilePopupWindow == null) {
                initBottomPop();
            }

            DialogUtil.BottomPopupItem moveItem = mAttachmentFilePopupWindow.adapter.getItem(0);
            DialogUtil.BottomPopupItem downloadItem = mAttachmentFilePopupWindow.adapter.getItem(1);
            DialogUtil.BottomPopupItem deleteItem = mAttachmentFilePopupWindow.adapter.getItem(2);

            if (selectedFileObject.isOwner()) {
                moveItem.enabled = true;
                downloadItem.enabled = true;
                deleteItem.enabled = true;
            } else {
                moveItem.enabled = true;
                downloadItem.enabled = true;
                deleteItem.enabled = false;
            }

            mAttachmentFilePopupWindow.adapter.notifyDataSetChanged();
            mAttachmentFilePopupWindow.tvTitle.setText(selectedFileObject.name);

            mAttachmentFilePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    }

    private AdapterView.OnItemClickListener onPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    doRename(selectedPosition, mFilesArray.get(selectedPosition).folderObject);
                    break;
                case 1:
                    AttachmentFolderObject selectedFolderObject = mFilesArray.get(selectedPosition).folderObject;
                    if (selectedFolderObject.isDeleteable()) {
                        action_delete_single(selectedFolderObject);
                    } else {
                        showButtomToast("请先清空文件夹");
                        return;
                    }
                    break;
            }
            mAttachmentPopupWindow.dismiss();
        }
    };

    private AdapterView.OnItemClickListener onFilePopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    // 移动
                    action_move_single(mFilesArray.get(selectedPosition));
                    break;
                case 1:
                    // 下载
                    action_download_single(mFilesArray.get(selectedPosition));
                    break;
                case 2:
                    // 删除
                    action_delete_single(mFilesArray.get(selectedPosition));
                    break;
            }
            mAttachmentFilePopupWindow.dismiss();
        }
    };

    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    action_move();
                    break;
                case 1:
                    action_download();
                    break;
                case 2:
                    if (isChooseOthers()) {
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
            DialogUtil.RightTopPopupItem moveItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_move), R.drawable.ic_menu_move);
            popupItemArrayList.add(moveItem);
            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_download), R.drawable.ic_menu_download);
            popupItemArrayList.add(downloadItem);
            DialogUtil.RightTopPopupItem deleteItem = new DialogUtil.RightTopPopupItem(getString(R.string.action_delete), R.drawable.ic_menu_delete_selector);
            popupItemArrayList.add(deleteItem);
            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(AttachmentsActivity.this, popupItemArrayList, onRightTopPopupItemClickListener);
        }
    }

    public void showRightTopPop() {

        if (mRightTopPopupWindow == null) {
            initRightTopPop();
        }

        DialogUtil.RightTopPopupItem moveItem = mRightTopPopupWindow.adapter.getItem(0);
        DialogUtil.RightTopPopupItem deleteItem = mRightTopPopupWindow.adapter.getItem(2);

        if (isChooseOthers()) {
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
        mRightTopPopupWindow.showAtLocation(getCurrentFocus(), Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);

    }

    /**
     * 是否选中了别人创建的文件，别人创建的文件，没有权限删除
     *
     * @return 是否选中了别人创建的文件
     */
    private boolean isChooseOthers() {
        boolean hasOtherFile = false;
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (fileObject.isSelected && !fileObject.isFolder && !fileObject.isOwner()) {
                hasOtherFile = true;
                break;
            }
        }
        return hasOtherFile;
    }


    private void doRename(final int position, final AttachmentFolderObject folderObject) {
        if (folderObject.file_id.equals("0")) {
            showButtomToast("默认文件夹无法重命名");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsActivity.this);
        LayoutInflater li = LayoutInflater.from(AttachmentsActivity.this);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setText(folderObject.name);
        builder.setTitle("重命名")
                .setView(v1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                //从网页版扒来的正则
                String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ /=]";
                Pattern namePattern = Pattern.compile(namePatternStr);
                if (newName.equals("")) {
                    showButtomToast("名字不能为空");
                } else if (namePattern.matcher(newName).find()) {
                    showButtomToast("文件夹名：" + newName + " 不能采用");
                } else {
                    if (!newName.equals(folderObject.name)) {
                        HOST_FOLDER_NAME = String.format(HOST_FOLDER_NAME, mProjectObjectId, folderObject.file_id, newName);
                        putNetwork(HOST_FOLDER_NAME, HOST_FOLDER_NAME, position, newName);
                    }
                }
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
        input.requestFocus();
    }

    ArrayList<AttachmentFolderObject> selectFolder;

    void action_delete_single(AttachmentFolderObject selectedFolderObject) {
        if (selectedFolderObject == null)
            return;

        selectFolder = new ArrayList<AttachmentFolderObject>();
        selectFolder.add(selectedFolderObject);
        String messageFormat = "确定删除文件夹 \"%s\" ？";
        showDialog("删除文件夹", String.format(messageFormat, selectedFolderObject.name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFolders();
            }
        });
    }

    void deleteFolders() {
        if (selectFolder.size() > 0) {
            //setRefreshing(true);
            HOST_FOLDER_DELETE = String.format(HOST_FOLDER_DELETE_FORMAT, mProjectObjectId, selectFolder.get(0).file_id);
            deleteNetwork(HOST_FOLDER_DELETE, HOST_FOLDER_DELETE, selectedPosition, selectFolder.get(0));
        }
    }

    @OptionsItem
    void action_new_folder() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsActivity.this);
        LayoutInflater li = LayoutInflater.from(AttachmentsActivity.this);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setHint("请输入文件夹名称");
        builder.setTitle("新建文件夹")
                .setView(v1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ /=]";
                Pattern namePattern = Pattern.compile(namePatternStr);
                if (newName.equals("")) {
                    showButtomToast("名字不能为空");
                } else if (namePattern.matcher(newName).find()) {
                    showButtomToast("文件夹名：" + newName + " 不能采用");
                } else {
                    HOST_FOLDER_NEW = String.format(HOST_FOLDER_NEW, mProjectObjectId);
                    RequestParams params = new RequestParams();
                    params.put("name", newName);
                    params.put("parentId", mAttachmentFolderObject.file_id);
                    postNetwork(HOST_FOLDER_NEW, params, HOST_FOLDER_NEW);
                }
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
        input.requestFocus();
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

    public void setDownloadStatus(AttachmentFileObject mFileObject) {
        File mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.name);

        if (mFile.exists() && mFile.isFile() && mFile.length() == mFileObject.size) {

            Long downloadId = downloadList.getLong(mFileObject.file_id, 0L);
            if (downloadId != 0L) {
                mFileObject.downloadId = downloadId;
                updateFileDownloadStatus(mFileObject);
                mFileObject.isDownload = false;
            } else {
                mFileObject.isDownload = true;
            }

        } else {
            mFileObject.downloadId = 0L;
            mFileObject.isDownload = false;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        /** observer download change **/
        if (downloadObserver == null)
            downloadObserver = new DownloadChangeObserver();
        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
        //updateView();

        checkFileDownloadStatus();
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

    // 更新文件的下载状态
    private void checkFileDownloadStatus() {
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (!fileObject.isFolder) {
                setDownloadStatus(fileObject);
                //Log.d("onResume", "update status:" + fileObject.name + " " + fileObject.isDownload);
            }
        }
        adapter.notifyDataSetChanged();
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

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        }
    }

    ;

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    }

    public void updateFileDownloadStatus(AttachmentFileObject mFileObject) {
        if (mFileObject.downloadId != 0L) {
            mFileObject.bytesAndStatus = downloadManagerPro.getBytesAndStatus(mFileObject.downloadId);
            Log.v("updateFileDownloadStatus", mFileObject.name + ":" + mFileObject.bytesAndStatus[0] + " " + mFileObject.bytesAndStatus[1] + " " + mFileObject.bytesAndStatus[2]);

            //handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
        }
    }

    ArrayList<AttachmentFileObject> downloadFiles;

    private void action_download_single(final AttachmentFileObject selectedFile) {
        if (selectedFile == null) {
            showButtomToast("没有选中文件");
            return;
        }
        File mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), selectedFile.name);
        if (mFile.exists() && mFile.isFile() && mFile.length() == selectedFile.size) {
            return;
        }
        if (!share.contains(FileUtil.DOWNLOAD_SETTING_HINT)) {
            String msgFormat = "您的文件将下载到以下路径：\n%s\n您也可以去设置界面设置您的下载路径";

            AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsActivity.this);
            builder.setTitle("提示")
                    .setMessage(String.format(msgFormat, defaultPath)).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    download(selectedFile);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogTitleLineColor(dialog);

            SharedPreferences.Editor editor = share.edit();
            editor.putBoolean(FileUtil.DOWNLOAD_SETTING_HINT, true);
            editor.commit();
        } else {
            download(selectedFile);
        }
    }

    void action_download() {
        final ArrayList<AttachmentFileObject> downloadFiles = new ArrayList<AttachmentFileObject>();
        for (AttachmentFileObject fileObject : mFilesArray) {
            if (fileObject.isSelected && !fileObject.isFolder) {
                File mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), fileObject.name);
                if (mFile.exists() && mFile.isFile() && mFile.length() == fileObject.size) {
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

            AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentsActivity.this);
            builder.setTitle("提示")
                    .setMessage(String.format(msgFormat, defaultPath)).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    download(downloadFiles);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogTitleLineColor(dialog);

            SharedPreferences.Editor editor = share.edit();
            editor.putBoolean(FileUtil.DOWNLOAD_SETTING_HINT, true);
            editor.commit();
        } else {
            download(downloadFiles);
        }
    }

    private void download(ArrayList<AttachmentFileObject> mFileObjects) {
        for (AttachmentFileObject mFileObject : mFileObjects) {
            String url = String.format(urlDownload, mProjectObjectId, mFileObject.file_id);

            PersistentCookieStore cookieStore = new PersistentCookieStore(AttachmentsActivity.this);
            String cookieString = "";
            for (Cookie cookie : cookieStore.getCookies()) {
                cookieString += cookie.getName() + "=" + cookie.getValue() + ";";
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.addRequestHeader("Cookie", cookieString);
            request.setDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.name);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setTitle(mFileObject.name);
            request.setVisibleInDownloadsUi(false);

            long downloadId = downloadManager.enqueue(request);
            downloadListEditor.putLong(mFileObject.file_id, downloadId);
        }
        downloadListEditor.commit();

    }

    private void download(AttachmentFileObject mFileObject) {
        ArrayList<AttachmentFileObject> mFileObjects = new ArrayList<AttachmentFileObject>();
        mFileObjects.add(mFileObject);
        download(mFileObjects);
    }

}
