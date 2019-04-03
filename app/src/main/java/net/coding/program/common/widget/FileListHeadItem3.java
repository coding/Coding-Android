package net.coding.program.common.widget;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.project.detail.file.v2.UploadCallback;

import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chenchao on 16/2/27.
 */
@Deprecated
public class FileListHeadItem3 extends FrameLayout {

    View retryUpload;
    View stopUpload;
    ProgressBar progressBar;

    UploadCallback callback;
    Param postParam;
    private RequestHandle requestHandle;

    public FileListHeadItem3(Context context) {
        super(context);

        inflate(context, R.layout.project_attachment_file_list_item_upload, this);
        retryUpload = findViewById(R.id.retryUpload);
        stopUpload = findViewById(R.id.stopUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        retryUpload.setVisibility(GONE);
        retryUpload.setOnClickListener(v -> upload());
        stopUpload.setOnClickListener(v -> stopUpload());
    }

    public void setData(Param param, UploadCallback uploadStyle, ImageLoadTool imageLoadTool) {
        postParam = param;
        this.callback = uploadStyle;

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

    public void setProgress(int progress) { // max = 100
        progressBar.setProgress(progress);
    }

    private void upload() {
        retryUpload.setVisibility(GONE);

        AsyncHttpClient client = MyAsyncHttpClient.createClient(getContext().getApplicationContext());
        RequestParams params = new RequestParams();
        params.put("dir", postParam.dirId);
        try {
            params.put("file", postParam.file);
        } catch (Exception e) {
            Global.errorLog(e);
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                int code = response.optInt("code");
                if (code == 0) {
                    CodingFile codingFile = new Gson().fromJson(response.optString("data"), CodingFile.class);
                    callback.onSuccess(codingFile);
                }
                ((ViewGroup) getParent()).removeView(FileListHeadItem3.this);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                ((ViewGroup) getParent()).removeView(FileListHeadItem3.this);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                setProgress((int) (bytesWritten * 1.0 / totalSize * 100));
            }
        };
        requestHandle = client.post(getContext().getApplicationContext(), postParam.url, params, jsonHttpResponseHandler);


    }

    private void stopUpload() {
        if (requestHandle != null) {
            AsyncTask.execute(() -> requestHandle.cancel(true));
            ((ViewGroup) getParent()).removeView(FileListHeadItem3.this);
        }
    }

    public static class Param {
        String url;
        File file;
        String dirId;

        public Param(String url, String dirId, File file) {
            this.url = url;
            this.dirId = dirId;
            this.file = file;
        }

        public Param(int projectId, int dirId, File file) {
            String urlUpload = Global.HOST_API + "/project/%s/file/upload";
            this.url = String.format(urlUpload, projectId);
            this.dirId = String.valueOf(dirId);
            this.file = file;
        }
    }
}
