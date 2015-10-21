package net.coding.program.project.init.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.common.photopick.CameraPhotoUtil;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.ProjectObject;
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
 * 项目设置页面
 */
@EFragment(R.layout.init_fragment_project_set)
@OptionsMenu(R.menu.menu_fragment_create)
public class ProjectSetFragment extends BaseFragment {

    public static final int RESULT_REQUEST_PHOTO = 3003;
    private static final String TAG = "ProjectSetFragment";
    final String host = Global.HOST_API + "/project";
    private final int RESULT_REQUEST_PHOTO_CROP = 3006;
    ProjectObject mProjectObject;

    String iconPath;

    boolean isBackToRefresh = false;
    MenuItem mMenuSave;
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
    @ViewById
    View itemTransfer;

    TextView advanceText;
    private Uri fileUri;
    private Uri fileCropUri;

    @AfterViews
    protected void init() {
        mProjectObject = (ProjectObject) getArguments().getSerializable("projectObject");

        ((TextView) itemTransfer.findViewById(R.id.title)).setText("项目转让");

        advanceText = (TextView) item.findViewById(R.id.title);
        advanceText.setText("删除项目");
        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
        projectName.setText(mProjectObject.name);
        description.setText(mProjectObject.description);
        if (!mProjectObject.isPublic()) {
            iconPrivate.setVisibility(View.VISIBLE);
        }
        description.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        });
        Global.popSoftkeyboard(getActivity(), description, false);
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


    @Click
    public void item() {
        ProjectAdvanceSetActivity_
                .intent(this)
                .mProjectObject(mProjectObject)
                .start();
    }

    @Click
    public void itemTransfer() {
        ProjectTransferActivity_
                .intent(this)
                .mProjectObject(mProjectObject)
                .start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenuSave = menu.findItem(R.id.action_finish);
        updateSendButton();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateSendButton() {
        String text = description.getText().toString().trim();
        if (text.isEmpty() || text.equals(mProjectObject.description)) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.action_finish) {
            showProgressBar(true, "正在修改...");
            action_done();
            return true;
        }
        return false;
    }

    private void action_done() {
        RequestParams params = new RequestParams();
        params.put("name", mProjectObject.name);
        params.put("description", description.getText().toString().trim());
        params.put("id", mProjectObject.getId());
        params.put("default_branch", "master");
        putNetwork(host, params, host, null);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(host)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "修改项目");
                showButtomToast("修改成功");
                isBackToRefresh = true;
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                InitProUtils.hideSoftInput(getActivity());
                backToRefresh();
            } else {
                isBackToRefresh = false;
                showErrorMsg(code, respanse);
            }
        } else {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "修改项目图片");
                showButtomToast("图片上传成功...");
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                isBackToRefresh = true;
            } else {
                isBackToRefresh = false;
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
                Global.cropImageUri(this, fileUri, fileCropUri, 600, 600, RESULT_REQUEST_PHOTO_CROP);
            }

        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    iconPath = Global.getPath(getActivity(), fileCropUri);
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


    public void backToRefresh() {
        Intent intent = new Intent();
        intent.putExtra("projectObject", mProjectObject);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
