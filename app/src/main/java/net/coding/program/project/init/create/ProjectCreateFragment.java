package net.coding.program.project.init.create;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.CameraPhotoUtil;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.FileProviderHelp;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.compatible.UriCompat;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/**
 * Created by jack wang on 2015/3/31.
 */

@EFragment(R.layout.init_fragment_project_create)
@OptionsMenu(R.menu.menu_fragment_create)
public class ProjectCreateFragment extends BaseFragment {

    public static final int RESULT_REQUEST_PHOTO = 2003;
    private static final String TAG_CREATE_PROJECT = "TAG_CREATE_PROJECT";
    private static final String TAG = "ProjectCreateFragment";
    private final int RESULT_REQUEST_PHOTO_CROP = 2006;
    String currentType = "私有";

    ProjectInfo projectInfo;
    MenuItem mMenuSave;

    @ViewById
    ImageView projectIcon;
    @ViewById
    EditText projectName, description;
    @ViewById
    CheckBox generateReadme;

    private Uri fileUri;
    private Uri fileCropUri;
    private String defaultIconUrl;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    @AfterViews
    protected void init() {
        projectInfo = new ProjectInfo();
        imageLoadTool.loadImage(projectIcon, IconRandom.getRandomUrl(), ImageLoadTool.optionsRounded2, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (!isResumed()) {
                    return;
                }

                File imageFile = imageLoadTool.imageLoader.getDiskCache().get(imageUri);
                if (imageFile == null) { // 不可能为空，但看umeng日志又有可能
                    showMiddleToast("载入默认项目图标失败");
                    return;
                }

                File newFile = new File(imageFile.getPath() + ".png");

                if (newFile.exists() || imageFile.renameTo(newFile)) {
                    defaultIconUrl = newFile.getPath();
                } else {
                    defaultIconUrl = imageFile.getPath();
                }
            }
        });

        projectName.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        });
        Global.popSoftkeyboard(getActivity(), description, false);
    }

    @SuppressLint("CheckResult")
    @Click
    void projectIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle("选择图片")
                .setCancelable(true)
                .setItems(R.array.camera_gallery, (dialog, which) -> {
                    if (which == 0) {
                        new RxPermissions(getActivity())
                                .request(PermissionUtil.CAMERA_STORAGE)
                                .subscribe(granted -> {
                                    if (granted) {
                                        camera();
                                    }
                                });
                    } else {
                        photo();
                    }
                })
                .show();
    }

    private void camera() {
        File tempFile = CameraPhotoUtil.getCacheFile(getActivity());
        fileUri = FileProviderHelp.getUriForFile(getActivity(), tempFile);

        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0及以上
            intentFromCapture.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            intentFromCapture.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        startActivityForResult(intentFromCapture, RESULT_REQUEST_PHOTO);
    }

    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    fileUri = data.getData();
                }

                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                Global.startPhotoZoom(this, getActivity(), fileUri, fileCropUri,  RESULT_REQUEST_PHOTO_CROP);
            }

        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String filePath = FileUtil.getPath(getActivity(), fileCropUri);
                    projectIcon.setImageURI(fileCropUri);
                    projectInfo.icon = filePath;

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenuSave = menu.findItem(R.id.action_finish);
        updateSendButton();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }

        int itemId_ = item.getItemId();
        if (itemId_ == R.id.action_finish) {
            actionDone();
            return true;
        }
        return false;
    }

    private void updateSendButton() {
        if (projectName.getText().toString().isEmpty()
                ) {
            enableSendButton(false);
        } else {
            enableSendButton(true);
        }
    }

    private void enableSendButton(boolean enable) {
        if (mMenuSave == null) {
            return;
        }

        if (enable) {
            mMenuSave.setIcon(R.drawable.ic_menu_ok);
            mMenuSave.setEnabled(true);
        } else {
            mMenuSave.setIcon(R.drawable.ic_menu_ok_unable);
            mMenuSave.setEnabled(false);
        }
    }

    private void actionDone() {
        initProjectInfo();
    }

    private void initProjectInfo() {
        projectInfo.name = projectName.getText().toString().trim();
        if (TextUtils.isEmpty(projectInfo.name)) {
            showButtomToast("项目名不能为空...");
            return;
        }
        if (!InputCheck.textValidate(projectInfo.name)) {
            showWarningDialog();
            return;
        }
        projectInfo.description = description.getText().toString().trim();
        /*projectInfo.icon="";*/
        showProgressBar(true, "正在创建项目...");
        createProject();
    }

    @CheckedChange
    void generateReadme(boolean checked) {
        projectInfo.gitReadmeEnabled = checked;
    }

    private void createProject() {
        final String host = UriCompat.createProject();
        RequestParams params = new RequestParams();

        params.put("name", projectInfo.name);
        params.put("description", projectInfo.description);
        params.put("type", projectInfo.type);
        params.put("gitEnabled", projectInfo.gitEnable);
        params.put("gitReadmeEnabled", projectInfo.gitReadmeEnabled);
        params.put("gitIgnore", projectInfo.gitIgnore);
        params.put("gitLicense", projectInfo.gitLicense);
        params.put("importFrom", projectInfo.importFrom);
        params.put("vcsType", projectInfo.vcsType);
        try {
            if (!TextUtils.isEmpty(projectInfo.icon)) {
                Log.d(TAG, "icon=" + projectInfo.icon);
                params.put("icon", new File(projectInfo.icon));
            } else if (!TextUtils.isEmpty(defaultIconUrl)) {
                params.put("icon", new File(defaultIconUrl));
            }
        } catch (Exception e) {
            Log.d(TAG, "" + e.toString());
        }

        postNetwork(host, params, TAG_CREATE_PROJECT);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(TAG_CREATE_PROJECT)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "新建项目");
                umengEvent(UmengEvent.E_PROJECT, "新建项目");

                EventBus.getDefault().post(new EventRefresh(true));
                String path = respanse.optString("data");
                ProjectHomeActivity_
                        .intent(this)
                        .mJumpParam(new ProjectJumpParam(path))
                        .mNeedUpdateList(true)
                        .start();
                getActivity().finish();

                showButtomToast("项目创建成功...");
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void showWarningDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.init_dialog_text_entry2, null);
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle).setTitle("提示")
                .setView(textEntryView)
                .setPositiveButton("关闭", (dialog1, which) -> dialog1.dismiss())
                .show();
    }

    public static class IconRandom {

        public static final String[] iconUrls = {
                "https://coding.net/static/project_icon/scenery-1.png",
                "https://coding.net/static/project_icon/scenery-2.png",
                "https://coding.net/static/project_icon/scenery-3.png",
                "https://coding.net/static/project_icon/scenery-4.png",
                "https://coding.net/static/project_icon/scenery-5.png",
                "https://coding.net/static/project_icon/scenery-6.png",
                "https://coding.net/static/project_icon/scenery-7.png",
                "https://coding.net/static/project_icon/scenery-8.png",
                "https://coding.net/static/project_icon/scenery-9.png",
                "https://coding.net/static/project_icon/scenery-10.png",
                "https://coding.net/static/project_icon/scenery-11.png",
                "https://coding.net/static/project_icon/scenery-12.png",
                "https://coding.net/static/project_icon/scenery-13.png",
                "https://coding.net/static/project_icon/scenery-14.png",
                "https://coding.net/static/project_icon/scenery-15.png",
                "https://coding.net/static/project_icon/scenery-16.png",
                "https://coding.net/static/project_icon/scenery-17.png",
                "https://coding.net/static/project_icon/scenery-18.png",
                "https://coding.net/static/project_icon/scenery-19.png",
                "https://coding.net/static/project_icon/scenery-20.png",
                "https://coding.net/static/project_icon/scenery-21.png",
                "https://coding.net/static/project_icon/scenery-22.png",
                "https://coding.net/static/project_icon/scenery-23.png",
                "https://coding.net/static/project_icon/scenery-24.png"
        };

        public static String getRandomUrl() {
            int index = (int) (Math.random() * iconUrls.length);
            return iconUrls[index];
        }

    }

    public final class ProjectInfo {
        String name;
        String description;
        int type = 2; // 默认私有
        boolean gitEnable = true;
        boolean gitReadmeEnabled = false;
        String gitIgnore = "no";
        String gitLicense = "no";
        String importFrom = "";
        String vcsType = "git";
        String icon = "";
    }


}
