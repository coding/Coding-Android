package net.coding.program.common.widget;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.project.detail.file.v2.UploadCallback;

import java.io.File;

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

    UploadHelp uploadHelp;

    private void upload() {
        retryUpload.setVisibility(GONE);

        uploadHelp = new UploadHelp(getContext(), postParam.projectId, postParam.folderId, postParam.file,
                new UploadHelp.NetworkRequest() {
                    @Override
                    public void onProgress(int progress) {
                        setProgress(progress);
                    }

                    @Override
                    public void onSuccess(CodingFile codingFile) {
                        uploadHelp = null;
                        ((ViewGroup) getParent()).removeView(FileListHeadItem2.this);
                        callback.onSuccess(codingFile);
                    }

                    @Override
                    public void onFail() {
                        uploadHelp = null;
                        ((ViewGroup) getParent()).removeView(FileListHeadItem2.this);
                    }
                });

        uploadHelp.upload();
    }


    private void stopUpload() {
        if (uploadHelp != null) {
            uploadHelp.unsubscribe();
            ((ViewGroup) getParent()).removeView(this);
        }
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
