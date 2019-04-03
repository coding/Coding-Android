package net.coding.program.project.detail;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.widget.BottomToolBar;
import net.coding.program.pickphoto.detail.ImagePagerFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EActivity(R.layout.activity_attachments_photo_detail)
public class AttachmentsPhotoDetailActivity extends AttachmentsDetailBaseActivity {

    @ViewById
    SubsamplingScaleImageView imageView;

    @ViewById
    View layout_image_prototype, clickImagePrototype;

    @ViewById
    BottomToolBar bottomToolBar;

    @ViewById
    ProgressBar progressBar;

    @ViewById
    View ivDownloadCancel;

    @ViewById
    TextView progressBarText;

    String urlTemplate = Global.HOST_API + "/project/%s/files/%s/view";

    String urlFiles = null;

    private static final String TAG_PhotoDetailActivity = "TAG_PhotoDetailActivity";

    @AfterViews
    protected final void initAttachmentsPhotoDetailActivity() {
        updateDisplay();
        bottomToolBar.setClick(clickBottomBar);

        bindUIDownload(false);
    }

    private void updateDisplay() {
        if (mExtraFile != null) {
            String filePath = "file://" + mExtraFile.getPath();

            imageView.setImage(ImageSource.uri(filePath));
            layout_image_prototype.setVisibility(View.GONE);
            bottomToolBar.setVisibility(View.VISIBLE);
            findViewById(R.id.bottomPanel).setVisibility(View.GONE);

        } else if (mFile.exists()) {
            String filePath = "file://" + mFile.getPath();
            imageView.setImage(ImageSource.uri(filePath));
            layout_image_prototype.setVisibility(View.GONE);
            bottomToolBar.setVisibility(View.VISIBLE);
        } else if (!TextUtils.isEmpty(mAttachmentFileObject.owner_preview)) {
            if (mProjectObjectId != 0 && !TextUtils.isEmpty(mAttachmentFileObject.file_id)) {
                urlFiles = String.format(urlTemplate, mProjectObjectId, mAttachmentFileObject.file_id);
            }
            getImageLoad().imageLoader.loadImage(mAttachmentFileObject.owner_preview, ImagePagerFragment.Companion.getOptionsImage(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    File file = getImageLoad().imageLoader.getDiskCache().get(imageUri);
                    if (file != null) {
                        imageView.setImage(ImageSource.uri(file.getAbsolutePath()));
                        layout_image_prototype.setVisibility(View.VISIBLE);
                        bottomToolBar.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            urlFiles = String.format(urlTemplate, mProjectObjectId, mAttachmentFileObject.file_id);
            onRefresh();
        }

    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.project_attachment_photo;
    }

    @Click
    protected void clickImagePrototype() {
        action_download();
        showMiddleToast("开始下载");

        bindUIDownload(true);
    }

    @Override
    protected void onRefresh() {
        if (TextUtils.isEmpty(urlFiles)) return;
        getNetwork(urlFiles, TAG_PhotoDetailActivity);
    }

    private void bindUIDownload(boolean downloading) {
        if (downloading) {
            progressBar.setVisibility(View.VISIBLE);
            progressBarText.setVisibility(View.VISIBLE);
            ivDownloadCancel.setVisibility(View.VISIBLE);
            clickImagePrototype.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            progressBarText.setVisibility(View.INVISIBLE);
            ivDownloadCancel.setVisibility(View.INVISIBLE);
            clickImagePrototype.setVisibility(View.VISIBLE);
        }
    }

    @Click
    void ivDownloadCancel() {
        bindUIDownload(false);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        super.parseJson(code, respanse, tag, pos, data);
        if (tag.equals(TAG_PhotoDetailActivity)) {
            if (code == 0) {
                JSONObject file = respanse.getJSONObject("data").getJSONObject("file");
                mAttachmentFileObject = new AttachmentFileObject(file);
                updateDisplay();
                hideProgressDialog();
                invalidateOptionsMenu();
            } else {
//                if (code == ImagePagerFragment.HTTP_CODE_FILE_NOT_EXIST) {
//                    BlankViewDisplay.setBlank(0, this, true, blankLayout, null);
//                } else {
//                    BlankViewDisplay.setBlank(0, this, false, blankLayout, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            getFileUrlFromNetwork();
//                        }
//                    });
//                }

                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    protected void onDownloadProgress(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    protected void onDownloadFinish(boolean success) {
        showProgressBar(false);
        if (success) {
            mAttachmentFileObject.isDownload = true;
            setResult(RESULT_OK);
            updateDisplay();
        } else {
            bindUIDownload(false);
        }
    }
}
