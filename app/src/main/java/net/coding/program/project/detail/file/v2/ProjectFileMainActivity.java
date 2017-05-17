package net.coding.program.project.detail.file.v2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.photopick.ImageInfo;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.common.widget.FileListHeadItem2;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.Pager;
import net.coding.program.network.model.file.CodingFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static net.coding.program.maopao.MaopaoAddActivity.PHOTO_MAX_COUNT;

/**
 * Created by chenchao on 2017/5/15.
 */

@EActivity(R.layout.project_file_listview)
@OptionsMenu(R.menu.project_file_listview)
public class ProjectFileMainActivity extends BackActivity implements UploadCallback {

    public static final int RESULT_REQUEST_PICK_PHOTO = 1003;
    public static final int FILE_SELECT_CODE = 10;
    public static final int FILE_DELETE_CODE = 11;
    public static final int FILE_MOVE_CODE = 12;
    public static final int RESULT_MOVE_FOLDER = 13;

    @Extra
    ProjectObject project;

    @Extra
    CodingFile parentFolder = new CodingFile();

    @ViewById
    CommonListView listView;

    @ViewById(R.id.folder_actions_layout)
    View foldeBottomBar;

    @ViewById(R.id.files_actions_layout)
    View fileBottomBar;

    private ViewGroup listHead;

    Set<CodingFile> selectFiles = new HashSet<>();

    List<CodingFile> listData = new ArrayList<>();
    private ProjectFileAdapter listAdapter;

    ActionMode actionMode = null;

    @AfterViews
    void initProjectFileMainActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));
        listView.setEmptyView(R.layout.fragment_enterprise_project_empty, R.layout.fragment_enterprise_project_empty);


        listHead = (ViewGroup) getLayoutInflater().inflate(R.layout.upload_file_layout, listView, false);
        listView.setNormalHeader(listHead);

        listAdapter = new ProjectFileAdapter(listData, selectFiles);
        listView.setAdapter(listAdapter);

        Network.getRetrofit(this)
                .getFileList(project.owner_user_name, project.name, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<CodingFile>>(this) {
                    @Override
                    public void onSuccess(Pager<CodingFile> data) {
                        super.onSuccess(data);

                        listData.clear();
                        listData.addAll(data.list);
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    @OptionsItem
    void actionEdit() {
        setEditMode(true);
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

            fileBottomBar.setVisibility(View.VISIBLE);
            foldeBottomBar.setVisibility(View.GONE);

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
            fileBottomBar.setVisibility(View.GONE);
            foldeBottomBar.setVisibility(View.VISIBLE);
            setEditMode(false);
        }
    };

    @Click(R.id.common_folder_bottom_add)
    void folderAdd() {

    }

    @Click(R.id.common_folder_bottom_upload)
    void fileUpload() {
        new AlertDialog.Builder(this)
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
                                        FILE_SELECT_CODE);
                            } catch (android.content.ActivityNotFoundException ex) {
                                showButtomToast("请安装文件管理器");
                            }
                            break;
                        default:
                            break;
                    }
                }).show();
    }

    private void startPhotoPickActivity() {
        if (!PermissionUtil.writeExtralStorage(this)) {
            return;
        }

        Intent intent = new Intent(this, PhotoPickActivity.class);
        intent.putExtra(PhotoPickActivity.EXTRA_MAX, PHOTO_MAX_COUNT);
        startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
    }

    @OnActivityResult(FILE_SELECT_CODE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtil.getPath(this, uri);
            File selectedFile = new File(path);
//            uploadFilePrepare(selectedFile);
            uploadFileInfo(selectedFile);
        }
    }

    @OnActivityResult(RESULT_REQUEST_PICK_PHOTO)
    void onPickResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                @SuppressWarnings("unchecked")
                ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
                List<File> files = new ArrayList<>();
                for (ImageInfo pickPhot : pickPhots) {
                    File outputFile = new PhotoOperate(this).scal(pickPhot.path);
                    files.add(outputFile);
//                    uploadFilePrepare(outputFile);
                }
                uploadFilePrepareList(files);
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
                folder.fileId +
                "/files/existed?names=" +
                encodeName;
    }

    private void uploadFilePrepare(File selectedFile) {
//        String httpHost = getHttpFileExist(selectedFile.getName(), parentFolder);
//        getNetwork(httpHost, TAG_HTTP_FILE_EXIST, -1, selectedFile);
    }

    private List<String> tags = new ArrayList<>();
    private boolean isUpload = false;

    private void uploadFilePrepareList(List<File> files) {
        isUpload = true;
        tags.clear();
        for (File file : files) {
            String tag = file.getName();
            tags.add(tag);
            String httpHost = getHttpFileExist(file.getName(), parentFolder);
            getNetwork(httpHost, file.getName(), -1, file);
        }
    }

    private void uploadFile() {
//        String path = "";
//        File file = new File(path);
//        MediaType type = MediaType.parse("image");
//        RequestBody body = RequestBody.create(type, file);
//        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
//        RequestBody idBody = RequestBody.create(MultipartBody.FORM, "aaa");
//        Network.getRetrofit(getActivity())
//                .postImage(part)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new BaseObserver<Attachment>(getActivity()) {
//                               @Override
//                               public void onSuccess(Attachment data) {
//                                   super.onSuccess(data);
//
//                                   String imageUri = data.url;
//                                   String messageData = String.format("<img class=\"chat-image\" src=\"%s\"/>", imageUri);
//                                   String messageDesc = IMAGE_DESC;
//                                   sendMessage(messageData, messageDesc, sending);
//                               }
//
//                               @Override
//                               public void onFail(int errorCode, @NonNull String error) {
//                                   super.onFail(errorCode, error);
//                                   sending.setStyle(CustomMessage.Style.sendingFail);
//                               }
//                           }
//                );

    }

    private void uploadFileInfo(File file) {
        FileListHeadItem2 uploadItem = new FileListHeadItem2(this);
        listHead.addView(uploadItem);

        FileListHeadItem2.Param param = new FileListHeadItem2.Param(project.getId(), parentFolder.fileId, file);
        uploadItem.setData(param, this, getImageLoad());

    }

    private void actionAll() {
        for (CodingFile item : listData) {
            selectFiles.add(item);
        }
        listAdapter.notifyDataSetChanged();
    }

    private void actionInverse() {
        for (CodingFile item : listData) {
            if (selectFiles.contains(item)) {
                selectFiles.remove(item);
            } else {
                selectFiles.add(item);
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSuccess(CodingFile codingFile) {
        if (isFinishing()) {
            return;
        }

        umengEvent(UmengEvent.FILE, "上传文件");

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

        listAdapter.notifyDataSetChanged();
        setResult(Activity.RESULT_OK);

//        BlankViewDisplay.setBlank(listData.size(), this, true, blankLayout, mClickReload);
    }
}
