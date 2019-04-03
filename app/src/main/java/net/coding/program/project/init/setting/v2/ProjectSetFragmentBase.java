package net.coding.program.project.init.setting.v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.CameraPhotoUtil;
import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.FileProviderHelp;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/**
 * Created by chenchao on 2017/5/23.
 * 基本的项目设置
 */

@EFragment(R.layout.init_fragment_project_set)
public class ProjectSetFragmentBase extends BaseFragment {

    public static final int RESULT_REQUEST_PHOTO = 3003;
    public final int RESULT_REQUEST_PHOTO_CROP = 3006;

    protected final String host = Global.HOST_API + "/project";

    @FragmentArg
    protected ProjectObject mProjectObject;

    @ViewById
    protected ImageView projectIcon;

    private Uri fileUri;
    private Uri fileCropUri;

    String iconPath;

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

    @SuppressLint("CheckResult")
    @Click
    protected void projectIcon() {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setTitle("选择图片")
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
                    iconPath = FileUtil.getPath(getActivity(), fileCropUri);
                    projectIcon.setImageURI(fileCropUri);
                    showProgressBar(true, "正在上传图片...");
                    String uploadUrl = host + "/" + mProjectObject.getId() + "/project_icon";
                    RequestParams params = new RequestParams();
                    params.put("file", new File(iconPath));
                    postNetwork(uploadUrl, params, uploadUrl);
                } catch (Exception e) {
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
