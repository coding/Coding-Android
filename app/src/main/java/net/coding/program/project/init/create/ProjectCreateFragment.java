package net.coding.program.project.init.create;

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
import net.coding.program.project.init.InitProUtils;

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
import java.util.Random;

/**
 * Created by jack wang on 2015/3/31.
 */

@EFragment(R.layout.init_fragment_project_create)
@OptionsMenu(R.menu.menu_fragment_create)
public class ProjectCreateFragment extends BaseFragment{

    private static final String TAG="ProjectCreateFragment";

    public static final int RESULT_REQUEST_PHOTO = 2003;

    private final int RESULT_REQUEST_PHOTO_CROP = 2006;

    public static final int RESULT_REQUEST_PICK_TYPE = 2004;


    final String host = Global.HOST + "/api/project";

    String currentType=ProjectTypeActivity.TYPE_PRIVATE;

    ProjectInfo projectInfo;

    private Uri fileUri;

    private Uri fileCropUri;

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

    @AfterViews
    protected void init(){
        projectInfo=new ProjectInfo();
        projectTypeText.setText(currentType);
        iconfromNetwork(projectIcon, IconRandom.getRandomUrl(), ImageLoadTool.optionsRounded2);
    }

    @Click
    void item(){
        Intent intent=new Intent(getActivity(),ProjectTypeActivity_.class);
        intent.putExtra("type",currentType);
        startActivityForResult(intent, RESULT_REQUEST_PICK_TYPE);
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
                    String filePath = Global.getPath(getActivity(), fileCropUri);
                    projectIcon.setImageURI(fileCropUri);
                    projectInfo.icon=filePath;

                } catch (Exception e) {
                }
            }
        }else if (requestCode==RESULT_REQUEST_PICK_TYPE){
            if (resultCode==Activity.RESULT_OK){
                String type=data.getStringExtra("type");
                if (TextUtils.isEmpty(type)){
                    return;
                }
                currentType=type;
                projectTypeText.setText(currentType);
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

    private void action_done() {
        initProjectInfo();
    }

    private void initProjectInfo(){
        projectInfo.name=projectName.getText().toString().trim();
        if (!InitProUtils.textValidate(projectInfo.name)){
            showWarningDialog();
            return;
        }
        projectInfo.description=description.getText().toString().trim();
        projectInfo.type="1";
        if (currentType.equals(ProjectTypeActivity.TYPE_PUBLIC)){
            projectInfo.type="2";
        }
        projectInfo.gitEnable="true";
        projectInfo.gitReadmeEnabled="false";
        projectInfo.gitIgnore="no";
        projectInfo.gitLicense="no";
        projectInfo.importFrom="";
        projectInfo.vcsType="git";
        /*projectInfo.icon="";*/
        showProgressBar(true,"正在创建项目...");
        createProject();
    }

    private void createProject(){
        RequestParams params=new RequestParams();
        params.put("name",projectInfo.name);
        params.put("description",projectInfo.description);
        params.put("type",projectInfo.type);
        params.put("gitEnabled",projectInfo.gitEnable);
        params.put("gitReadmeEnabled",projectInfo.gitReadmeEnabled);
        params.put("gitIgnore",projectInfo.gitIgnore);
        params.put("gitLicense",projectInfo.gitLicense);
        params.put("importFrom",projectInfo.importFrom);
        params.put("vcsType",projectInfo.vcsType);
        try {
            if (!TextUtils.isEmpty(projectInfo.icon)){
                Log.d(TAG,"icon="+projectInfo.icon);
                /*File outputFile = new PhotoOperate(getActivity()).scal(Uri.parse(projectInfo.icon));*/
                params.put("icon",new File(projectInfo.icon));
            }
        } catch (Exception e) {
            showMiddleToast("缩放图片失败");
        }
        postNetwork(host, params, host);

/*        params.setHttpEntityIsRepeatable(true);
        params.setUseJsonStreamer(false);

        AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
        client.post(host,params,new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG,"onFailure--"+responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG,"onSuccess-"+responseString);
            }
        });*/
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(host)) {
            if (code == 0) {
                InitProUtils.intentToMain(getActivity());
                showButtomToast("项目创建成功...");
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void showWarningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.setTitle("提示")
                .setMessage("项目名只允许字母、数字或者下划线（_）、中划线（-），必须以字母或者数字开头，且不能以.git结尾")
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        CustomDialog.dialogTitleLineColor(getActivity(), dialog);
    }

    public final class ProjectInfo{
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

    public static class IconRandom{

        public static String[] iconUrls={
                "https://coding.net/static/project_icon/scenery-10.png",
                "https://coding.net/static/project_icon/scenery-23.png",
                "https://coding.net/static/project_icon/scenery-11.png",
                "https://coding.net/static/project_icon/scenery-20.png",
                "https://coding.net/static/project_icon/scenery-19.png"
        };

        public static String getRandomUrl(){
            int index=(int)(Math.random()*iconUrls.length);
            return iconUrls[index];
        }

    }

}
