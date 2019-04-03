package net.coding.program.common.widget;

import android.content.Context;
import android.support.annotation.NonNull;

import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.ProgressRequestBody;
import net.coding.program.network.UpQboxRequest;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.network.model.file.UploadToken;

import java.io.File;
import java.util.UUID;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chenchao on 2018/3/13.
 */
public class UploadHelp {

    Context context;
    int projectId;
    File uploadFile;
    int folderId;
    private Subscription subscription;
    private boolean isProjectFile = true;

    private NetworkRequest networkRequest;

    public UploadHelp(Context context, int projectId, int folderId, File uploadFile, NetworkRequest networkRequest) {
        this.context = context;
        this.projectId = projectId;
        this.uploadFile = uploadFile;
        this.folderId = folderId;
        this.networkRequest = networkRequest;
    }


    public UploadHelp(Context context, int projectId, int folderId, File uploadFile, NetworkRequest networkRequest, boolean isProjectFile) {
        this.context = context;
        this.projectId = projectId;
        this.uploadFile = uploadFile;
        this.folderId = folderId;
        this.networkRequest = networkRequest;
        this.isProjectFile = isProjectFile;
    }

    public void upload() {
        Network.getRetrofit(context)
                .uploadFileToken(projectId, uploadFile.getName(), uploadFile.length())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<UploadToken>(context) {
                    @Override
                    public void onSuccess(UploadToken data) {
                        super.onSuccess(data);

                        uploadFileToQbox(data);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        networkRequest.onFail();
                    }
                });
    }


    private void uploadFileToQbox(UploadToken data) {
        File file = uploadFile;
        ProgressRequestBody body = new ProgressRequestBody(context, file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                networkRequest.onProgress(percentage);
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
        int pos;
        if ((pos = fileName.lastIndexOf('.')) != -1) {
            suffix = fileName.substring(pos, fileName.length());
        }
        RequestBody key = RequestBody.create(MultipartBody.FORM, UUID.randomUUID().toString() + suffix);
        RequestBody dir = RequestBody.create(MultipartBody.FORM, String.valueOf(folderId));
        RequestBody projectIdBody = RequestBody.create(MultipartBody.FORM, String.valueOf(projectId));
        RequestBody token = RequestBody.create(MultipartBody.FORM, data.uptoken);
        RequestBody time = RequestBody.create(MultipartBody.FORM, data.time);
        RequestBody authToken = RequestBody.create(MultipartBody.FORM, data.authToken);
        RequestBody userId = RequestBody.create(MultipartBody.FORM, String.valueOf(data.userId));

        UpQboxRequest retrofitLoad = Network.getRetrofitLoad(context);
        Observable<HttpResult<CodingFile>> httpResultObservable;

        if (isProjectFile) {
            httpResultObservable = retrofitLoad
                    .uploadFile(part, key, dir, projectIdBody, token, time, authToken, userId);
        } else {
            RequestBody xfoldType = RequestBody.create(MultipartBody.FORM, "0");
            RequestBody foldType = RequestBody.create(MultipartBody.FORM, "0");
            RequestBody xdir = RequestBody.create(MultipartBody.FORM, "0");
            httpResultObservable = retrofitLoad
                    .uploadFile(part, key, dir, projectIdBody, token, time, authToken, userId, xfoldType, foldType, xdir);
        }

        subscription = httpResultObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<CodingFile>(context) {
                    @Override
                    public void onSuccess(CodingFile data) {
                        super.onSuccess(data);

                        subscription = null;

                        networkRequest.onSuccess(data);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        subscription = null;
                        networkRequest.onFail();
                    }

                });

    }

    public void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public interface NetworkRequest {
        void onProgress(int progress);

        void onSuccess(CodingFile codingFile);

        void onFail();
    }

}
