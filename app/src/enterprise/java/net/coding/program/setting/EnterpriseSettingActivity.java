package net.coding.program.setting;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.CameraPhotoUtil;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.EnterpriseDetail;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.widget.FileProviderHelp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

@EActivity(R.layout.activity_enterprise_setting)
public class EnterpriseSettingActivity extends BackActivity {
    private static final int REQUEST_UPDATE_NAME = 1000;
    private static final int RESULT_REQUEST_PHOTO = 1001;
    private static final int RESULT_REQUEST_PHOTO_CROP = 1002;

    private static final String UPLOAD_IMG = "UPLOAD_IMG";
    private static final String PUT_IMG = "PUT_IMG";
    private static final String GET_IMG_DATA = "GET_IMG_DATA";

    private final String uploadImgHost = getUploadImgHost();
    private final String updateImgHost = getUpdateHost();
    private final String getImgData = getImgHost();

    @ViewById
    CircleImageView enterpriseHead;
    @ViewById
    TextView enterpriseNameTv;
    @ViewById
    TextView personIp;

    private String getUploadImgHost() {
        String host = String.format("%s/user", Global.HOST_API);
        return host + "/avatar";
    }

    private String getUpdateHost() {
        String host = String.format("%s/team/%s", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        return host + "/avatar";
    }

    private String getImgHost() {
        String host = String.format("%s/team/%s", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        return host + "/get";
    }

    @AfterViews
    void initView() {
        setActionBarTitle(getString(R.string.enterprise_setting));
        ImageLoader.getInstance().displayImage(EnterpriseInfo.instance().getAvatar(),
                enterpriseHead, EnterpriseAccountActivity.enterpriseIconOptions);
        enterpriseNameTv.setText(EnterpriseInfo.instance().getName());
        personIp.setText(EnterpriseInfo.instance().getGlobalkey());
    }

    @Click
    void enterpriseName() {
        EnterpriseNameActivity_.intent(this).startForResult(REQUEST_UPDATE_NAME);
    }

    @Click
    void enterpriseHeadLayout() {
        setIcon();
    }

    void setIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("更换头像")
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
    }

    private Uri fileUri;
    private Uri fileCropUri;

    private void camera() {
        File tempFile = CameraPhotoUtil.getCacheFile(this);
        fileUri = FileProviderHelp.getUriForFile(this, tempFile);

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

    @OnActivityResult(REQUEST_UPDATE_NAME)
    void updateNameResult(int result) {
        if (result == Activity.RESULT_OK) {
            enterpriseNameTv.setText(EnterpriseInfo.instance().getName());
            setResult(Activity.RESULT_OK);
        }
    }

    @OnActivityResult(RESULT_REQUEST_PHOTO)
    void updateHeadResult(int result, Intent data) {
        if (result == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                fileUri = data.getData();
            }

            fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
            Global.startPhotoZoom(this, fileUri, fileCropUri,  RESULT_REQUEST_PHOTO_CROP);
        }
    }

    @OnActivityResult(RESULT_REQUEST_PHOTO_CROP)
    void updatePhotoCropResult(int result, Intent data) {
        if (result == Activity.RESULT_OK) {
            try {
                String filePath = FileUtil.getPath(this, fileCropUri);
                RequestParams params = new RequestParams();
                params.put("images.jpg", new File(filePath));
                postNetwork(uploadImgHost, params, UPLOAD_IMG);
                showDialogLoading();
            } catch (Exception e) {
                Global.errorLog(e);
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(UPLOAD_IMG)) {
            if (code == 0) {
                String url = respanse.optString("data");
                putImg(url);
            } else {
                showErrorMsg(code, respanse);
                hideProgressDialog();
            }
        } else if (tag.equals(PUT_IMG)) {
            hideProgressDialog();
            if (code == 0) {
                getImgData();
            } else {
                showErrorMsg(code, respanse);
                hideProgressDialog();
            }
        } else if (tag.equals(GET_IMG_DATA)) {
            hideProgressDialog();
            if (code == 0) {
                JSONObject dataObject = respanse.optJSONObject("data");
                EnterpriseDetail detail = new EnterpriseDetail(dataObject);
                EnterpriseInfo.instance().update(this, detail);
                ImageLoader.getInstance().displayImage(EnterpriseInfo.instance().getAvatar(), enterpriseHead, ImageLoadTool.options);
            } else {
                showErrorMsg(code, respanse);
            }
        }

        setResult(Activity.RESULT_OK);
    }

    private void putImg(String url) {
        RequestParams params = new RequestParams();
        params.put("url", url);
        putNetwork(updateImgHost, params, PUT_IMG);
    }

    public void getImgData() {
        getNetwork(getImgHost(), GET_IMG_DATA);
    }
}
