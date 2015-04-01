package net.coding.program.project.init.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.photopick.CameraPhotoUtil;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jack wang on 2015/3/31.
 */
@EFragment(R.layout.init_fragment_project_set)
@OptionsMenu(R.menu.menu_fragment_create)
public class ProjectSetFragment extends BaseFragment{

    private static final String TAG="ProjectSetFragment";

    public static final int RESULT_REQUEST_PHOTO = 3003;

    private final int RESULT_REQUEST_PHOTO_CROP = 3006;

    final String host = Global.HOST + "/api/project";

    ProjectObject mProjectObject;

    String iconPath;

    boolean isBackToRefresh=false;

    private Uri fileUri;

    private Uri fileCropUri;

    @ViewById
    ImageView projectIcon;

    @ViewById
    View iconPrivate;

    @ViewById
    TextView projectName;

    @ViewById
    EditText description;

    @ViewById
    View item;

    @ViewById(R.id.title)
    TextView advanceText;

    @AfterViews
    protected void init(){
        mProjectObject= (ProjectObject) getArguments().getSerializable("projectObject");
        advanceText.setText("高级设置");
        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
        projectName.setText(mProjectObject.name);
        description.setText(mProjectObject.description);
        if (!mProjectObject.isPublic()){
            iconPrivate.setVisibility(View.VISIBLE);
        }
    }

    @Click
    void projectIcon(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择图片")
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


    @Click
    void item(){
        Intent intent=new Intent(getActivity(),ProjectAdvanceSetActivity_.class);
        intent.putExtra("projectObject",mProjectObject);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.action_finish) {
            showProgressBar(true,"正在修改...");
            action_done();
            return true;
        }
        return false;
    }

    private void action_done() {
        RequestParams params=new RequestParams();
        params.put("name",mProjectObject.name);
        params.put("description",description.getText().toString().trim());
        params.put("id",mProjectObject.getId());
        params.put("default_branch","master");
        putNetwork(host, params, host, null);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(host)) {
            if (code == 0) {
                /*mProjectObject = new ProjectObject(respanse.getJSONObject("data"));*/
                showButtomToast("修改成功");
                isBackToRefresh=true;
            } else {
                isBackToRefresh=false;
                showErrorMsg(code, respanse);
            }
        }else {
            if (code == 0) {
                /*mProjectObject = new ProjectObject(respanse.getJSONObject("data"));*/
                showButtomToast("图片上传成功...");
                isBackToRefresh=true;
            } else {
                isBackToRefresh=false;
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    fileUri = data.getData();
                }
                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                cropImageUri(fileUri, fileCropUri, 600, 600, RESULT_REQUEST_PHOTO_CROP);
            }

        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    iconPath = Global.getPath(getActivity(), fileCropUri);
                    projectIcon.setImageURI(fileCropUri);
                    showProgressBar(true,"正在上传图片...");
                    String uploadUrl=host+"/"+mProjectObject.getId()+"/project_icon";
                    RequestParams params=new RequestParams();
                    params.put("file",new File(iconPath));
                    postNetwork(uploadUrl,params,uploadUrl);
                } catch (Exception e) {
                }
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void cropImageUri(Uri uri, Uri outputUri, int outputX, int outputY, int requestCode) {
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


}
