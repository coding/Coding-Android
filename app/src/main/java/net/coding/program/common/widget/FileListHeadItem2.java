package net.coding.program.common.widget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.ProgressRequestBody;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.network.model.file.UploadToken;
import net.coding.program.project.detail.file.v2.UploadCallback;

import java.io.File;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chenchao on 16/2/27.
 * 文件上传 list
 */
public class FileListHeadItem2 extends FrameLayout {

    View retryUpload;
    View stopUpload;
    ProgressBar progressBar;

    UploadCallback callback;
    Param postParam;

    public FileListHeadItem2(Context context) {
        super(context);

        inflate(context, R.layout.project_attachment_file_list_item_upload, this);
        retryUpload = findViewById(R.id.retryUpload);
        stopUpload = findViewById(R.id.stopUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        retryUpload.setVisibility(GONE);
        retryUpload.setOnClickListener(v -> upload());
        stopUpload.setOnClickListener(v -> stopUpload());
    }

    public void setData(Param param, UploadCallback callback, ImageLoadTool imageLoadTool) {
        postParam = param;
        this.callback = callback;

        String fileName = param.file.getName();

        String[] splitName = fileName.split("\\.");

        int iconId = R.drawable.ic_file_unknown;
        String suffix = splitName[splitName.length - 1];

        if (splitName.length > 1) {
            if (AttachmentFileObject.isImage(suffix)) {
                imageLoadTool.loadImage(((ImageView) findViewById(R.id.icon)), Uri.fromFile(postParam.file).toString());
            } else {
                iconId = AttachmentFileObject.getIconResourceId(suffix);
                ((ImageView) findViewById(R.id.icon)).setImageResource(iconId);
            }
        } else {
            ((ImageView) findViewById(R.id.icon)).setImageResource(iconId);
        }

        ((TextView) findViewById(R.id.file_name)).setText(fileName);
        upload();
    }

    public void setError() {
        retryUpload.setVisibility(VISIBLE);
    }

    public void setProgress(int progress) { // max = 100
        progressBar.setProgress(progress);
    }

    private void upload() {
        retryUpload.setVisibility(GONE);

        Network.getRetrofit(getContext())
                .uploadFileToken(postParam.projectId, postParam.file.getName(), postParam.file.length())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<UploadToken>(getContext()) {
                    @Override
                    public void onSuccess(UploadToken data) {
                        super.onSuccess(data);

                        uploadFileToQbox(data);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
        }

    private void uploadFileToQbox(UploadToken data) {
        File file = postParam.file;
        MediaType type = MediaType.parse("image");
        ProgressRequestBody body = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                setProgress(percentage);
            }

            @Override
            public void onError() {

            }

            @Override
            public void onFinish() {

            }
        });

        String fileName = file.getName();
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, body);

        String suffix = "";
        int pos = 0;
        if ((pos = fileName.lastIndexOf('.')) != -1) {
            suffix = fileName.substring(pos + 1, fileName.length());
        }

        RequestBody key = RequestBody.create(MultipartBody.FORM, UUID.randomUUID().toString() + suffix);
        RequestBody dir = RequestBody.create(MultipartBody.FORM, String.valueOf(postParam.folderId));
        RequestBody projectId = RequestBody.create(MultipartBody.FORM, String.valueOf(postParam.projectId));
        RequestBody token = RequestBody.create(MultipartBody.FORM, data.uptoken);
        RequestBody time = RequestBody.create(MultipartBody.FORM, data.time);
        RequestBody authToken = RequestBody.create(MultipartBody.FORM, data.authToken);
        RequestBody userId = RequestBody.create(MultipartBody.FORM, String.valueOf(data.userId));

        Network.getRetrofitLoad(getContext())
                .uploadFile(part, key, dir, projectId, token, time, authToken, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<CodingFile>(getContext()) {
                    @Override
                    public void onSuccess(CodingFile data) {
                        super.onSuccess(data);

                        ((ViewGroup) getParent()).removeView(FileListHeadItem2.this);
                        callback.onSuccess(data);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }

                });
    }


    private void stopUpload() {
        // TODO: 2017/5/17 停止上传 未实现
    }

    public static class Param {
        int projectId;
        int folderId;
        File file;

        public Param(int projectId, int folderId, File file) {
            this.projectId = projectId;
            this.folderId = folderId;
            this.file = file;
        }
    }
}
