package net.coding.program.project.init.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.photopick.CameraPhotoUtil;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.project.ProjectHomeActivity_;
import net.coding.program.project.detail.ProjectActivity;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by jack wang on 2015/3/31.
 */

@EFragment(R.layout.init_fragment_project_create)
@OptionsMenu(R.menu.menu_fragment_create)
public class ProjectCreateFragment extends BaseFragment {

    public static final int RESULT_REQUEST_PHOTO = 2003;
    public static final int RESULT_REQUEST_PICK_TYPE = 2004;
    private static final String TAG = "ProjectCreateFragment";
    final String host = Global.HOST_API + "/project";
    private final int RESULT_REQUEST_PHOTO_CROP = 2006;
    String currentType = ProjectTypeActivity.TYPE_PRIVATE;

    ProjectInfo projectInfo;
    MenuItem mMenuSave;
    @ViewById
    ImageView projectIcon;
    @ViewById
    EditText projectName;
    @ViewById
    EditText description;
    @ViewById
    View item;
    @ViewById
    TextView projectTypeText;
    private Uri fileUri;
    private Uri fileCropUri;
    private String defaultIconUrl;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    @AfterViews
    protected void init() {
        projectInfo = new ProjectInfo();
        projectTypeText.setText(currentType);
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

    @Click
    void item() {
        Intent intent = new Intent(getActivity(), ProjectTypeActivity_.class);
        intent.putExtra("type", currentType);
        startActivityForResult(intent, RESULT_REQUEST_PICK_TYPE);
    }

    @Click
    void projectIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择图片")
                .setCancelable(true)
                .setItems(R.array.camera_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            camera();
                        } else {
                            photo();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        CustomDialog.dialogTitleLineColor(getActivity(), dialog);
    }

    private void camera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraPhotoUtil.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, RESULT_REQUEST_PHOTO);
    }

    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    fileUri = data.getData();
                }
                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                Global.cropImageUri(this, fileUri, fileCropUri, 600, 600, RESULT_REQUEST_PHOTO_CROP);
            }

        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String filePath = Global.getPath(getActivity(), fileCropUri);
                    projectIcon.setImageURI(fileCropUri);
                    projectInfo.icon = filePath;

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }
        } else if (requestCode == RESULT_REQUEST_PICK_TYPE) {
            if (resultCode == Activity.RESULT_OK) {
                String type = data.getStringExtra("type");
                if (TextUtils.isEmpty(type)) {
                    return;
                }
                currentType = type;
                projectTypeText.setText(currentType);
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
            action_done();
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

    private void action_done() {
        initProjectInfo();
    }

    private void initProjectInfo() {
        projectInfo.name = projectName.getText().toString().trim();
        if (TextUtils.isEmpty(projectInfo.name)) {
            showButtomToast("项目名不能为空...");
            return;
        }
        if (!InitProUtils.textValidate(projectInfo.name)) {
            showWarningDialog();
            return;
        }
        projectInfo.description = description.getText().toString().trim();
        projectInfo.type = "2";//默认私有
        if (currentType.equals(ProjectTypeActivity.TYPE_PUBLIC)) {
            projectInfo.type = "1";
        }
        projectInfo.gitEnable = "true";
        projectInfo.gitReadmeEnabled = "false";
        projectInfo.gitIgnore = "no";
        projectInfo.gitLicense = "no";
        projectInfo.importFrom = "";
        projectInfo.vcsType = "git";
        /*projectInfo.icon="";*/
        showProgressBar(true, "正在创建项目...");
        createProject();
    }

    private void createProject() {
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

        postNetwork(host, params, host);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(host)) {
            if (code == 0) {
//                InitProUtils.intentToMain(getActivity());
                umengEvent(UmengEvent.PROJECT, "新建项目");
                String path = respanse.optString("data");
                ProjectHomeActivity_
                        .intent(this)
                        .mJumpParam(new ProjectActivity.ProjectJumpParam(path))
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.setTitle("提示")
                .setView(textEntryView)
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        CustomDialog.dialogTitleLineColor(getActivity(), dialog);
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
        String type;
        String gitEnable;
        String gitReadmeEnabled;
        String gitIgnore;
        String gitLicense;
        String importFrom;
        String vcsType;
        String icon;
    }


}
