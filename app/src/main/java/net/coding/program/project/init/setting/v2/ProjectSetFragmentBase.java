package net.coding.program.project.init.setting.v2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.pickphoto.CameraPhotoUtil;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.FileUtil;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.io.File;

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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraPhotoUtil.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, RESULT_REQUEST_PHOTO);
    }

    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
    }

    @Click
    protected void projectIcon() {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setTitle("选择图片")
                .setCancelable(true)
                .setItems(R.array.camera_gallery, (dialog, which) -> {
                    if (which == 0) {
                        camera();
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
                if (data != null) {
                    fileUri = data.getData();
                }
                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                Global.cropImageUri(this, fileUri, fileCropUri, 600, 600, RESULT_REQUEST_PHOTO_CROP);
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
