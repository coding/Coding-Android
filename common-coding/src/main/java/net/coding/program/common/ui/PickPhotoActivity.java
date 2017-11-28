package net.coding.program.common.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.CameraPhotoUtil;

import org.androidannotations.annotations.EActivity;

@EActivity
public abstract class PickPhotoActivity extends BackActivity {

    protected abstract void pickImageCallback(Uri uri, String path);

    public static final int RESULT_REQUEST_PHOTO = 3003;
    public final int RESULT_REQUEST_PHOTO_CROP = 3006;

    protected final String host = Global.HOST_API + "/project";


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

    protected void pickPhoto() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
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
                    iconPath = FileUtil.getPath(this, fileCropUri);
                    pickImageCallback(fileCropUri, iconPath);

                } catch (Exception e) {
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
