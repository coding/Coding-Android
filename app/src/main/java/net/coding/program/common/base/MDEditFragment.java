package net.coding.program.common.base;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.CameraPhotoUtil;
import net.coding.program.common.Global;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.project.detail.EditPreviewMarkdown;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EFragment(R.layout.fragment_mdedit)
public class MDEditFragment extends BaseFragment {

    private final String tipFont = "在此输入文字";
    private final String HOST_UPLOAD_PHOTO_PUBLIC = Global.HOST_API + "/project/%s/upload_public_image";
    private final String HOST_UPLOAD_PHOTO_PRIVATE = Global.HOST_API + "/project/%s/file/upload";
    private final String HOST_UPLOAD_PHOTO_PUBLIC_PATH = Global.HOST_API + "%s/upload_public_image";
    //    private final String host_upload_photo = "https://coding.net/api/project/%d/file/upload";
    private final String HOST_UPLOAD_PHOTO_PRIVATE_PATH = Global.HOST_API + "%s/file/upload";
    private final int RESULT_REQUEST_PHOTO = 3005;
    @ViewById
    protected EditText edit;
    private Uri fileUri;
    private Uri fileCropUri;
    private String hostUploadPhoto = "";

    private EditPreviewMarkdown projectData;

    // 返回的可能是 path，也可能是 projectId
    protected String getProjectPath() {
        return projectData.getProjectPath();
    }

    @AfterViews
    protected final void initBase1() {
        projectData = (EditPreviewMarkdown) getActivity();
        String path = projectData.getProjectPath();
        String template;
        if (projectData.isProjectPublic()) {
            if (path.startsWith("/")) {
                template = HOST_UPLOAD_PHOTO_PUBLIC_PATH;
            } else {
                template = HOST_UPLOAD_PHOTO_PUBLIC;
            }
            hostUploadPhoto = String.format(template, path);
        } else {
            if (path.startsWith("/")) {
                template = HOST_UPLOAD_PHOTO_PRIVATE_PATH;
            } else {
                template = HOST_UPLOAD_PHOTO_PRIVATE;
            }
            hostUploadPhoto = String.format(template, path);
        }

        String customUploadPhoto = getCustomUploadPhoto();
        if (!TextUtils.isEmpty(customUploadPhoto)) {
            hostUploadPhoto = getCustomUploadPhoto();
        }
    }

    protected String getCustomUploadPhoto() {
        return "";
    }

    @Click
    public void mdBold(View v) {
        insertString(" **", tipFont, "** ");
    }

    @Click
    public void mdItalic(View v) {
        insertString(" *", tipFont, "* ");
    }

    @Click
    public void mdHyperlink(View view) {
        insertString("[", tipFont, "]()");
    }

    @Click
    public void mdLinkQuote(View view) {
        insertString("\n> ", tipFont, "");
    }

    @Click
    public void mdCode(View v) {
        insertString("\n```\n",
                tipFont,
                "\n```\n");
    }

    @Click
    public void mdTitle(View view) {
        insertString("## ", tipFont, " ##");
    }

    @Click
    public void mdList(View v) {
        insertString("\n - ", tipFont, "");
    }

    @Click
    public void mdDivide(View v) {
        insertString("\n----------\n", tipFont, "");
    }

    @Click
    public void mdPhoto(View v) {
        popPickDialog();
    }

    @SuppressLint("CheckResult")
    private void popPickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle("上传图片")
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
                }).show();
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
                    Uri url = data.getData();
                    if (url != null) {
                        fileUri = url;
                    }
                }

                showProgressBar(true, "正在上传图片...");
                setProgressBarProgress();

                updateImage(fileUri);
            }
        }
    }

    protected void updateImage(Uri updateFileUri) {
        try {
            File outputFile = new PhotoOperate(getActivity()).getFile(updateFileUri);
            RequestParams params = new RequestParams();
            params.put("dir", 0);
            params.put("file", outputFile);
            postNetwork(hostUploadPhoto, params, hostUploadPhoto);

        } catch (Exception e) {
            showProgressBar(false);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostUploadPhoto)) {
            showProgressBar(false);
            if (code == 0) {
                String fileUri;

                JSONObject jsonData = respanse.optJSONObject("data");
                if (jsonData != null) {
                    AttachmentFileObject fileObject = new AttachmentFileObject(jsonData);
                    fileUri = fileObject.owner_preview;
                } else {
                    fileUri = respanse.optString("data", "");
                }

                uploadImageSuccess(fileUri);

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    protected void uploadImageSuccess(String fileUri) {
        String mdPhotoUri = String.format("![图片](%s)\n", fileUri);
        insertString(mdPhotoUri, "", "");
    }

    private void insertString(String begin, String middle, String end) {
        edit.requestFocus();
        Global.popSoftkeyboard(getActivity(), edit, true);

        String insertString = String.format("%s%s%s", begin, middle, end);
        int insertPos = edit.getSelectionStart();
        int selectBegin = insertPos - begin.length();
        int selectEnd = selectBegin + insertString.length();

        Editable editable = edit.getText();
        String currentInput = editable.toString();

        if (0 <= selectBegin &&
                selectEnd <= currentInput.length() &&
                insertString.equals(currentInput.substring(selectBegin, selectEnd))) { //
            editable.replace(selectBegin, selectEnd, middle);
            edit.setSelection(selectBegin, selectBegin + middle.length());
        } else {
            editable.replace(insertPos, edit.getSelectionEnd(), insertString);
            edit.setSelection(insertPos + begin.length(), insertPos + begin.length() + middle.length());
        }
    }
}
