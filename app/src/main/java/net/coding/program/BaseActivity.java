package net.coding.program;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import net.coding.program.common.CustomDialog;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.umeng.UmengActivity;
import net.coding.program.user.UserDetailActivity_;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cc191954 on 14-8-16.
 * 封装了图片下载并缓存
 */
public class BaseActivity extends UmengActivity implements NetworkCallback {

    protected LayoutInflater mInflater;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    private ProgressDialog mProgressDialog;

    private NetworkImpl networkImpl;

    protected FootUpdate mFootUpdate = new FootUpdate();

    protected void showProgressBar(boolean show) {
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }
    }

    protected boolean progressBarIsShowing() {
        return mProgressDialog.isShowing();
    }

    protected View.OnClickListener mOnClickUser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String globalKey = (String) v.getTag();

            Intent intent = new Intent(BaseActivity.this, UserDetailActivity_.class);
            intent.putExtra("globalKey", globalKey);
            startActivity(intent);
        }
    };

    protected void showErrorMsg(int code, JSONObject json) {
        if (code == NetworkImpl.NETWORK_ERROR) {
            showButtomToast("连接服务器失败，请检查网络或稍后重试");
        } else {
            String msg = Global.getErrorMsg(json);
            if (!msg.isEmpty()) {
                showButtomToast(msg);
            }
        }
    }

    protected ImageLoadTool getImageLoad() {
        return imageLoadTool;
    }

    protected boolean isLoadingLastPage(String tag) {
        return networkImpl.isLoadingLastPage(tag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkImpl = new NetworkImpl(this, this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        mInflater = getLayoutInflater();
        initSetting();

        UnreadNotify.update(this);
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        super.onDestroy();
    }

    protected void initSetting() {
        networkImpl.initSetting();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
    }

    protected void getNextPageNetwork(String url, final String tag) {
        networkImpl.getNextPageNetwork(url, tag);
    }

    protected void postNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Post);
    }

    protected void postNetwork(String url, RequestParams params, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, params, tag, dataPos, data, NetworkImpl.Request.Post);
    }

    @Override
    public void getNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Get);
    }

    protected void getNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Get);
    }

    protected void putNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Put);
    }

    protected void putNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Put);
    }

    protected void deleteNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Delete);
    }

    protected void deleteNetwork(String url, final String tag, Object id) {
        networkImpl.loadData(url, null, tag, -1, id, NetworkImpl.Request.Delete);
    }

    protected void deleteNetwork(String url, final String tag, int dataPos, Object id) {
        networkImpl.loadData(url, null, tag, dataPos, id, NetworkImpl.Request.Delete);
    }

    protected void showDialog(String msg, DialogInterface.OnClickListener clickOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog dialog = builder.setTitle(msg)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();
        dialogTitleLineColor(dialog);
    }

    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();
        dialogTitleLineColor(dialog);
    }

    protected void showButtomToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void showMiddleToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void iconfromNetwork(ImageView view, String url) {
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url));
    }

    protected void imagefromNetwork(ImageView view, String url) {
        imageLoadTool.loadImageFromUrl(view, url);
    }

    protected void imagefromNetwork(ImageView view, String url, DisplayImageOptions options) {
        imageLoadTool.loadImageFromUrl(view, url, options);
    }

    public void camera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_PHOTO);
    }

    protected Uri getOutputMediaFileUri() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp
                + ".jpg");

        return Uri.fromFile(mediaFile);
    }

    public void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);

//        if (Global.isKK()) {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("image/*");
//            startActivityForResult(intent, RESULT_REQUEST_PHOTO);
//        } else {
//            Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
//                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(choosePictureIntent, RESULT_REQUEST_PHOTO);
//        }
    }

    protected final int RESULT_REQUEST_PHOTO = 1005;
    protected final int RESULT_REQUEST_PHOTO_CROP = 1006;

    protected Uri fileUri;
    protected Uri fileCropUri;

    protected void cropImageUri(Uri uri, Uri outputUri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }

    public final void dialogTitleLineColor(Dialog dialog) {
        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    /**
     * 载入动画
     */
    private DialogUtil.LoadingPopupWindow mDialogProgressPopWindow = null;

    public void initDialogLoading() {
        if (mDialogProgressPopWindow == null) {
            PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
                public void onDismiss() {
                    hideProgressDialog();
                }
            };

            mDialogProgressPopWindow = DialogUtil.initProgressDialog(this, onDismissListener);
        }
    }

    public void showDialogLoading(String title) {
        initDialogLoading();
        DialogUtil.showProgressDialog(this, mDialogProgressPopWindow, title);
    }

    public void showDialogLoading() {
        showDialogLoading("");
    }

    public void hideProgressDialog() {
        if (mDialogProgressPopWindow != null) {
            DialogUtil.hideDialog(mDialogProgressPopWindow);
        }
    }

}
