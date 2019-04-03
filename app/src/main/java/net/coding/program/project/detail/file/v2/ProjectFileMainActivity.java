package net.coding.program.project.detail.file.v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageInfo;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.AttachmentFolderObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.CodingToolbarBackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.BottomToolBar;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.common.widget.FileListHeadItem2;
import net.coding.program.common.widget.FileListHeadItem3;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.CodingRequest;
import net.coding.program.network.FileDownloadCallback;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpPageResult;
import net.coding.program.network.model.Pager;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.pickphoto.PhotoPickActivity;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity_;
import net.coding.program.project.detail.AttachmentsFolderSelectorActivity_;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPhotoDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;
import net.coding.program.project.detail.file.FileSaveHelp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static net.coding.program.common.Global.PHOTO_MAX_COUNT;

/**
 * Created by chenchao on 2017/5/15.
 * 新的项目文件列表
 */
@EActivity(R.layout.project_file_listview)
public class ProjectFileMainActivity extends CodingToolbarBackActivity implements UploadCallback,
        FileDownloadCallback, ProjectFileAdapter.UpdateMenu {

    public static final int RESULT_REQUEST_PICK_PHOTO = 8;
    public static final int RESULT_MOVE_FOLDER = 9;
    public static final int FILE_SELECT_FILE = 10;
    public static final int RESULT_FILE_DETAIL = 11;

    @Extra
    ProjectObject project;

    @Extra
    CodingFile parentFolder = new CodingFile();

    @ViewById
    CommonListView listView;

    @ViewById
    BottomToolBar bottomLayout, bottomLayoutBatch;

    private ViewGroup listHead;

    Set<CodingFile> selectFiles = new HashSet<>();
    Set<CodingFile> actionFiles = new HashSet<>();  // 实际操作的 item，有时候是单选，这时候与 select 就不同了

    List<CodingFile> listData = new ArrayList<>();
    private ProjectFileAdapter listAdapter;

    ActionMode actionMode = null;

    @AfterViews
    void initProjectFileMainActivity() {
        if (!TextUtils.isEmpty(parentFolder.name)) {
            setActionBarTitle(parentFolder.name);
        } else {
            setActionBarTitle("文件");
        }

        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));

        listHead = (ViewGroup) getLayoutInflater().inflate(R.layout.upload_file_layout, listView, false);
        listView.setNormalHeader(listHead);

        listAdapter = new ProjectFileAdapter(listData, selectFiles, this)
                .setClickMore(v -> {
                    CodingFile codingFile = (CodingFile) v.getTag();
                    if (codingFile.isDownloaded()) {
                        listViewItemClicked(codingFile);
                    } else if (codingFile.isDownloading()) {
                        if (codingFile.task != null) {
                            codingFile.task.pause();
                        }
                        updateItem(codingFile.url, 0);
                    } else {
                        actionDownload(codingFile);
                        updateItem(codingFile.url, 1);
                    }
                })
                .setOnClickListItem(v -> listViewItemClicked((CodingFile) v.getTag()))
                .setOnLongClickListItem(v -> {
                    showPop((CodingFile) v.getTag());
                    return true;
                });

        listView.setAdapter(listAdapter);


        listView.setDefaultOnRefreshListener(() -> onRefresh());
        onRefresh();

        bottomLayout.setClick(clickBottom);
        bottomLayoutBatch.setClick(clickBottom);
    }

    @Override
    public void update() {
        invalidateOptionsMenu();
        updateBlank();
    }

    @Nullable
    @Override
    protected ProjectObject getProject() {
        return project;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!listData.isEmpty()) {
            getMenuInflater().inflate(R.menu.project_file_listview, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected void onRefresh() {
        CodingRequest retrofit = Network.getRetrofit(this, listView);
        Observable<HttpPageResult<CodingFile>> fileList;
        if (parentFolder != null) {
            if (parentFolder.isShareFolder()) {
                fileList = retrofit.getShareFileList(project.owner_user_name, project.name);
            } else {
                fileList = retrofit.getFileList(project.owner_user_name, project.name, parentFolder.id);
            }
        } else {
            fileList = retrofit.getFileList(project.owner_user_name, project.name, 0);
        }

        fileList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<CodingFile>>(ProjectFileMainActivity.this, listView) {
                    @Override
                    public void onSuccess(Pager<CodingFile> data) {
                        super.onSuccess(data);

                        listData.clear();

                        if (parentFolder.id == 0) {
                            CodingFile shareFolder = CodingFile.craeteShareFolder();
                            listData.add(shareFolder);
                        }
                        listData.addAll(data.list);

                        String downloadPath = FileSaveHelp.getFileDownloadPath(ProjectFileMainActivity.this);
                        for (CodingFile item : listData) {
                            setDownloadStatus(item, downloadPath, project.getId());
                        }

                        listAdapter.notifyDataSetChangedCustom();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    void listViewItemClicked(CodingFile fileObject) {
        if (listAdapter.isEditMode()) {
            if (!fileObject.isFolder()) {
                listAdapter.invert(fileObject);
            }
        } else {
            fileItemJump(fileObject, project, this);
        }
    }

    public static void fileItemJump(AttachmentFileObject attachmentFile, ProjectObject project, Context context) {
        fileItemJump(attachmentFile, project, context, false, 0);
    }

    public static void fileItemJump(AttachmentFileObject attachmentFile, ProjectObject project, Context context, boolean hideHistory, int result) {
        if (AttachmentFileObject.isTxt(attachmentFile.fileType)) {
            AttachmentsTextDetailActivity_
                    .intent(context)
                    .mProjectObjectId(project.getId())
                    .mAttachmentFileObject(attachmentFile)
                    .mProject(project)
                    .mHideHistory(hideHistory)
                    .startForResult(RESULT_FILE_DETAIL);

        } else if (AttachmentFileObject.isMd(attachmentFile.fileType)) {
            AttachmentsHtmlDetailActivity_
                    .intent(context)
                    .mProjectObjectId(project.getId())
                    .mAttachmentFileObject(attachmentFile)
                    .mProject(project)
                    .mHideHistory(hideHistory)
                    .startForResult(RESULT_FILE_DETAIL);

        } else if (attachmentFile.isImage()) {
            AttachmentsPhotoDetailActivity_
                    .intent(context)
                    .mProjectObjectId(project.getId())
                    .mAttachmentFileObject(attachmentFile)
                    .mProject(project)
                    .mHideHistory(hideHistory)
                    .startForResult(RESULT_FILE_DETAIL);

        } else {
            AttachmentsDownloadDetailActivity_.intent(context)
                    .mProjectObjectId(project.getId())
                    .mAttachmentFileObject(attachmentFile)
                    .mHideHistoryLayout(hideHistory)
                    .mProject(project)
                    .startForResult(RESULT_FILE_DETAIL);
        }
    }

    public static void fileItemJump(CodingFile fileObject, ProjectObject project, Context context) {
        if (fileObject.isFolder()) {
            ProjectFileMainActivity_.intent(context)
                    .project(project)
                    .parentFolder(fileObject)
                    .startForResult(RESULT_FILE_DETAIL);
        } else {
            AttachmentFileObject attachmentFile = new AttachmentFileObject(fileObject);
            fileItemJump(attachmentFile, project, context);
        }
    }

    private void setDownloadStatus(CodingFile codingFile, String downloadPath, int projectId) {
        if (codingFile.isFolder()) {
            codingFile.downloadProgress = 0;
        } else {
            File localFile = FileUtil.getDestinationInExternalPublicDir(downloadPath, codingFile.getSaveName(projectId));
            if (localFile.exists() && localFile.isFile()) {
                codingFile.downloadProgress = CodingFile.MAX_PROGRESS;
            } else {
                codingFile.downloadProgress = 0;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DownloadHelp.instance().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadHelp.instance().unregister(this);
    }

    @OptionsItem
    void actionEdit() {
        setEditMode(true);
    }

    @OptionsItem
    void actionSearch() {
        SearchProjectFileActivity.setsCodingFiles(listData);
        SearchProjectFileActivity_.intent(this)
                .project(project)
                .folder(parentFolder)
                .start();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    private void setEditMode(boolean editMode) {
        listAdapter.setEditMode(editMode);
        if (editMode) {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
        } else {
            actionMode = null;
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.project_attachment_file_edit, menu);

            bottomLayoutBatch.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.GONE);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_all:
                    actionAll();
                    return true;
                case R.id.action_inverse:
                    actionInverse();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            bottomLayoutBatch.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
            setEditMode(false);
        }
    };

    void actionFolderAdd() {
        LayoutInflater li = LayoutInflater.from(this);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setHint("请输入文件夹名称");
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("新建文件夹")
                .setView(v1)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newName = input.getText().toString();
                    String namePatternStr = "[,`~!@#$%^&*:;()'\"><|.\\ /=]";
                    Pattern namePattern = Pattern.compile(namePatternStr);
                    if (newName.equals("")) {
                        showButtomToast("名字不能为空");
                    } else if (namePattern.matcher(newName).find()) {
                        showButtomToast("文件夹名：" + newName + " 不能采用");
                    } else {
                        Network.getRetrofit(this)
                                .createFolder(project.owner_user_name, project.name, newName, parentFolder.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new HttpObserver<CodingFile>(this) {
                                    @Override
                                    public void onSuccess(CodingFile data) {
                                        super.onSuccess(data);

                                        data.owner = GlobalData.sUserObject;

                                        umengEvent(UmengEvent.FILE, "新建文件夹");
                                        parentFolder.count++;
                                        int insertPos = 0;
                                        if (!listData.isEmpty() && listData.get(0).isShareFolder()) {
                                            insertPos = 1;
                                        }
                                        listData.add(insertPos, data);
                                        listAdapter.notifyDataSetChangedCustom();
                                        setResult(Activity.RESULT_OK);
                                    }
                                });
                    }
                }).setNegativeButton("取消", null)
                .show();

        input.requestFocus();
    }

    View.OnClickListener clickBottom = v -> {
        switch (v.getId()) {
            case R.id.actionAddFolder:
                actionFolderAdd();
                break;
            case R.id.actionUpload:
                actionFileUpload();
                break;
            case R.id.filesMove:
                actionFilesMove();
                break;
            case R.id.filesDownload:
                actionFilesDownload();
                break;
            case R.id.filesDelete:
                actionFilesDelete();
                break;
        }
    };

    void actionFileUpload() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setItems(R.array.file_type, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startPhotoPickActivity();
                            break;
                        case 1:
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            try {
                                startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
                                        FILE_SELECT_FILE);
                            } catch (android.content.ActivityNotFoundException ex) {
                                showButtomToast("请安装文件管理器");
                            }
                            break;
                        default:
                            break;
                    }
                }).show();
    }

    void actionFilesMove() {
        fillActionData();
        actionMove();
    }

    void actionFilesDownload() {
        fillActionData();
        actionDownload();
    }

    void actionFilesDelete() {
        fillActionData();
        actionDelete();
    }

    private void fillActionData() {
        actionFiles.clear();
        actionFiles.addAll(selectFiles);
    }

    private void actionMove(CodingFile codingFile) {
        actionFiles.clear();
        actionFiles.add(codingFile);
        actionMove();
    }

    private void actionMove() {
        if (checkIsEmpty()) {
            return;
        }

        AttachmentsFolderSelectorActivity_.intent(this)
                .mProjectObjectId(project.getId())
                .startForResult(RESULT_MOVE_FOLDER);
    }

    private void actionDownload(CodingFile single) {
        actionFiles.clear();
        actionFiles.add(single);

        DownloadHelp.instance().addTask(actionFiles, MyAsyncHttpClient.getLoginCookie(this),
                FileSaveHelp.getFileDownloadAbsolutePath(this), project.getId());
    }

    private void actionDownload() {
        actionFiles.clear();
        for (CodingFile item : selectFiles) {
            if (!item.isFolder()) {
                actionFiles.add(item);
            }
        }

        if (checkIsEmpty()) {
            return;
        }

        DownloadHelp.instance().addTask(actionFiles, MyAsyncHttpClient.getLoginCookie(this),
                FileSaveHelp.getFileDownloadAbsolutePath(this), project.getId());
    }

//    private void download(ArrayList<AttachmentFileObject> mFileObjects) {
//        try {
//            if (!PermissionUtil.writeExtralStorage(this)) {
//                return;
//            }
//
//            for (AttachmentFileObject mFileObject : mFileObjects) {
//                final String urlDownload = Global.HOST_API + "%s/files/%s/download";
//                String url = String.format(urlDownload, "/project/" + project.getId(), mFileObject.file_id);
//
//                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//                request.addRequestHeader("Cookie", MyAsyncHttpClient.getLoginCookie(this));
//
//
//                request.setDestinationInExternalPublicDir(getFileDownloadPath(), mFileObject.getSaveName(getProjectId()));
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
//                request.setTitle(mFileObject.getName());
//                request.setVisibleInDownloadsUi(false);
//
//
//                long downloadId = downloadManager.enqueue(request);
//                downloadListEditor.putLong(mFileObject.file_id + mFileObject.getHistory_id(), downloadId);
////                backgroundUpdate(downloadId);
//            }
//            downloadListEditor.commit();
//            mUpdateDownloadHandler.start();
//            checkFileDownloadStatus();
//        } catch (Exception e) {
//            Toast.makeText(this, R.string.no_system_download_service, Toast.LENGTH_LONG).show();
//        }
//    }

    @OnActivityResult(RESULT_MOVE_FOLDER)
    void onResultFolderMove(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (actionFiles.isEmpty()) {
            return;
        }

        AttachmentFolderObject selectedFolder = (AttachmentFolderObject) data.getSerializableExtra("mAttachmentFolderObject");
        if (selectedFolder.file_id.equals(String.valueOf(parentFolder.id))) {
            return;
        }

        ArrayList<Integer> fileIds = getActionItemsIds();
        Network.getRetrofit(this)
                .moveFolder(project.owner_user_name, project.name, Integer.valueOf(selectedFolder.file_id), fileIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {

                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        selectFiles.removeAll(actionFiles);
                        listData.removeAll(actionFiles);
                        actionFiles.clear();

                        listAdapter.notifyDataSetChangedCustom();
                        showButtomToast("移动成功");

                        setResult(RESULT_OK);
                    }
                });
    }

    @OnActivityResult(RESULT_FILE_DETAIL)
    void onResultDetail(int resultCode) {
        if (resultCode == RESULT_OK) {
            onRefresh();
            setResult(RESULT_OK);
        }
    }

    private boolean checkIsEmpty() {
        if (actionFiles.isEmpty()) {
            showButtomToast("没有选中文件");
        }

        return actionFiles.isEmpty();
    }

    private void actionDelete(CodingFile codingFile) {
        actionFiles.clear();
        actionFiles.add(codingFile);
        actionDelete();
    }

    private void actionDelete() {
        if (checkIsEmpty()) {
            return;
        }

        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("删除文件")
                .setMessage("确定要删除所选文件么？")
                .setPositiveButton("确定", (dialog, which) -> deleteFiles())
                .setNegativeButton("取消", null)
                .show();
    }

    private void showPop(CodingFile codingFile) {
        CodingFile selectedFileObject = codingFile;
        if (selectedFileObject.isShareFolder()) {
            return;
        }

        if (selectedFileObject.isFolder()) {
            CodingFile selectedFolderObject = codingFile;

            String[] itemNames;
            if (selectedFolderObject.id == 0) {
                itemNames = new String[]{};
            } else if (selectedFolderObject.count != 0) {
                itemNames = new String[]{"重命名", "移动到"};
            } else {
                itemNames = new String[]{"重命名", "移动到", "删除"};
            }
            if (itemNames.length == 0) {
                return;
            }

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setItems(itemNames, (dialog, which) -> {
                if (which == 0) {
                    doRename(codingFile);
                } else if (which == 1) {
                    actionMove(codingFile);
                } else {
                    if (codingFile.isDeleteable()) {
                        actionDelete(codingFile);
                    } else {
                        showButtomToast("请先清空文件夹");
                    }
                }
            });
            builder.show();

        } else {
            String[] itemTitles;
            if (selectedFileObject.isOwner()) {
                itemTitles = new String[]{"重命名", "移动到", "删除"};
            } else {
                itemTitles = new String[]{"重命名", "移动到"};
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setItems(itemTitles, (dialog, which) -> {
                if (which == 0) {
                    doRename(codingFile);
                } else if (which == 1) {
                    actionMove(codingFile);
                } else {
                    actionDelete(codingFile);
                }
            });
            builder.show();
        }
    }

    private void doRename(CodingFile folderObject) {
        if (folderObject.isFolder()) {
            folderRename(folderObject);
        } else {
            fileRename(folderObject);
        }
    }

    private void fileRename(CodingFile folderObject) {
        LayoutInflater li = LayoutInflater.from(this);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setText(folderObject.name);
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("重命名")
                .setView(v1)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newName = input.getText().toString();
                    //从网页版扒来的正则
                    if (newName.isEmpty()) {
                        showButtomToast("名字不能为空");
                    } else {
                        if (!newName.equals(folderObject.name)) {
                            Network.getRetrofit(this)
                                    .renameFile(project.owner_user_name, project.name, folderObject.id, newName)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new BaseHttpObserver(this) {
                                        @Override
                                        public void onSuccess() {
                                            super.onSuccess();
                                            folderObject.name = newName;
                                            listAdapter.notifyDataSetChangedCustom();
                                            showButtomToast("重命名成功");
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();

        input.requestFocus();
    }


    private void folderRename(CodingFile folderObject) {
        LayoutInflater li = LayoutInflater.from(this);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setText(folderObject.name);
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("重命名")
                .setView(v1)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newName = input.getText().toString();
                    //从网页版扒来的正则
                    String namePatternStr = "[,`~!@#$%^&*:;()'\"><|.\\ /=]";
                    Pattern namePattern = Pattern.compile(namePatternStr);
                    if (newName.equals("")) {
                        showButtomToast("名字不能为空");
                    } else if (namePattern.matcher(newName).find()) {
                        showButtomToast("文件夹名：" + newName + " 不能采用");
                    } else {
                        if (!newName.equals(folderObject.name)) {
                            Network.getRetrofit(this)
                                    .renameFold(project.owner_user_name, project.name, folderObject.id, newName)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new HttpObserver<Boolean>(this) {
                                        @Override
                                        public void onSuccess(Boolean data) {
                                            super.onSuccess(data);

                                            folderObject.name = newName;
                                            listAdapter.notifyDataSetChangedCustom();

                                            showButtomToast("重命名成功");
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();

        input.requestFocus();
    }

    void deleteFiles() {
        ArrayList<Integer> fileIds = getActionItemsIds();
        Network.getRetrofit(this)
                .deleteFiles(project.getId(), fileIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Integer>(this) {
                    @Override
                    public void onSuccess(Integer data) {
                        super.onSuccess(data);
                        selectFiles.removeAll(actionFiles);
                        listData.removeAll(actionFiles);
                        actionFiles.clear();

                        listAdapter.notifyDataSetChangedCustom();
                        showButtomToast("删除成功");

                        updateBlank();

                        setResultChanged();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    private void setResultChanged() {
        setResult(RESULT_OK);
    }

    @NonNull
    private ArrayList<Integer> getActionItemsIds() {
        ArrayList<Integer> fileIds = new ArrayList<>();
        for (CodingFile item : actionFiles) {
            fileIds.add(item.id);
        }
        return fileIds;
    }

    @SuppressLint("CheckResult")
    private void startPhotoPickActivity() {
        new RxPermissions(this)
                .request(PermissionUtil.STORAGE)
                .subscribe(granted -> {
                    if (granted) {

                        Intent intent = new Intent(this, PhotoPickActivity.class);
                        intent.putExtra(PhotoPickActivity.Companion.getEXTRA_MAX(), PHOTO_MAX_COUNT);
                        startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
                    }
                });
    }

    @OnActivityResult(FILE_SELECT_FILE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtil.getPath(this, uri);
            File selectedFile = new File(path);
            uploadFileInfo(selectedFile);
        }
    }

    @OnActivityResult(RESULT_REQUEST_PICK_PHOTO)
    void onPickResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                @SuppressWarnings("unchecked")
                ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
                for (ImageInfo item : pickPhots) {
                    File outputFile = new File(PhotoOperate.translatePath(item.path));
                    uploadFileInfo(outputFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getHttpFileExist(String name, CodingFile folder) {
        String encodeName = Global.encodeUtf8(name);
        return Global.HOST_API +
                project.getProjectPath() +
                "/dir/" +
                folder.id +
                "/files/existed?names=" +
                encodeName;
    }

    private void uploadFileInfo(File file) {
        if (GlobalData.isPrivateEnterprise()) {
            FileListHeadItem3 uploadItemView = new FileListHeadItem3(this);
            listHead.addView(uploadItemView);

            FileListHeadItem3.Param param = new FileListHeadItem3.Param(project.getId(),
                    parentFolder.id, file);
            uploadItemView.setData(param, this, getImageLoad());
        } else {
            FileListHeadItem2 uploadItem = new FileListHeadItem2(this);
            listHead.addView(uploadItem);

            FileListHeadItem2.Param param = new FileListHeadItem2.Param(project.getId(), parentFolder.id, file);
            uploadItem.setData(param, this, getImageLoad());
        }

        updateBlank();
    }

    private void actionAll() {
        for (CodingFile item : listData) {
            if (item.isShareFolder()) {
                continue;
            }

            selectFiles.add(item);
        }
        listAdapter.notifyDataSetChangedCustom();
    }

    private void actionInverse() {
        for (CodingFile item : listData) {
            if (item.isShareFolder()) {
                continue;
            }

            if (selectFiles.contains(item)) {
                selectFiles.remove(item);
            } else {
                selectFiles.add(item);
            }
        }
        listAdapter.notifyDataSetChangedCustom();
    }

    @Override
    public void onSuccess(CodingFile codingFile) {
        if (isFinishing()) {
            return;
        }

        umengEvent(UmengEvent.E_FILE, "上传成功");

        int firstFilePos = 0; // 第一个文件的位置（文件排在文件前面）
        int sameNamePos = -1; // 同名文件的位置

        for (int i = 0; i < listData.size(); ++i) {
            CodingFile item = listData.get(i);
            if (!item.isFolder()) {
                if (firstFilePos == 0) {
                    firstFilePos = i;
                }

                if (item.getName().equals(codingFile.getName())) {
                    sameNamePos = i;
                    break;
                }
            }
        }

        if (sameNamePos != -1) {
            listData.set(sameNamePos, codingFile);
        } else {
            listData.add(firstFilePos, codingFile);
        }

        listAdapter.notifyDataSetChangedCustom();
        setResult(Activity.RESULT_OK);

//        BlankViewDisplay.setBlank(listData.size(), this, true, blankLayout, mClickReload);

    }

    @Override
    public void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        String taskUrl = task.getUrl();

        int progress;
        if (totalBytes > 10000) { // 传大文件可能超出 int 的范围
            progress = soFarBytes / (totalBytes / 100);
        } else {
            progress = soFarBytes * 100 / totalBytes;
        }
        if (progress < 1) {
            progress = 1;
        }

        updateItem(taskUrl, progress);
        Logger.d(String.format("progress %s\t%s\t%s\t%s", task.getId(), soFarBytes, totalBytes, progress));
    }


    @Override
    public void completed(BaseDownloadTask task) {
        umengEvent(UmengEvent.E_FILE, "下载成功");
        Logger.d(String.format("completed %s\n", task.getUrl()));
        updateItem(task.getUrl(), CodingFile.MAX_PROGRESS);

        setResult(RESULT_OK);
    }

    @Override
    public void error(BaseDownloadTask task, Throwable e) {
        Logger.d(String.format("error %s\n", task.getUrl()));
        updateItem(task.getUrl(), 0);
    }

    private void updateItem(String taskUrl, int progress) {
        for (int i = 0; i < listData.size(); ++i) {
            CodingFile item = listData.get(i);
            if (!item.isFolder() && item.url.equals(taskUrl)) {
                item.downloadProgress = progress;
                if (item.downloadProgress == 0
                        || item.downloadProgress == CodingFile.MAX_PROGRESS) {
                    item.task = null;
                }
                listAdapter.notifyItemChanged(i + 1);
                break;
            }
        }
    }

    private void updateBlank() {
        int count = listHead.getChildCount() + listData.size();
        listView.update(this, CommonListView.Style.success, count);
    }

}
