package net.coding.program.common.widget;

import android.content.Context;
import android.support.annotation.NonNull;

import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.ProgressRequestBody;
import net.coding.program.network.UpQboxRequest;
import net.coding.program.network.model.HttpResult;
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
public class UploadPublicHelp {

    Context context;
    File uploadFile;
    private Subscription subscription;
    private NetworkRequest networkRequest;
    long updateTime; // 标记

    public UploadPublicHelp(Context context, File uploadFile, NetworkRequest networkRequest, long updateTime) {
        this.context = context;
        this.uploadFile = uploadFile;
        this.networkRequest = networkRequest;
        this.updateTime = updateTime;
    }

    public void upload() {
        Network.getRetrofit(context)
                .uploadPublicFileToken(uploadFile.getName(), uploadFile.length())
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
                        networkRequest.postImageFail(updateTime);
                    }
                });
    }


    private void uploadFileToQbox(UploadToken data) {
        File file = uploadFile;
        ProgressRequestBody body = new ProgressRequestBody(context, file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                networkRequest.onProgress(updateTime, percentage);
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
        RequestBody token = RequestBody.create(MultipartBody.FORM, data.uptoken);
        RequestBody time = RequestBody.create(MultipartBody.FORM, data.time);
        RequestBody authToken = RequestBody.create(MultipartBody.FORM, data.authToken);
        RequestBody userId = RequestBody.create(MultipartBody.FORM, String.valueOf(data.userId));

        UpQboxRequest retrofitLoad = Network.getRetrofitLoad(context);
        Observable<HttpResult<String>> httpResultObservable;

        httpResultObservable = retrofitLoad
                .uploadPublicFile(part, key, token, time, authToken, userId);

        subscription = httpResultObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<String>(context) {
                    @Override
                    public void onSuccess(String data) {
                        super.onSuccess(data);

                        subscription = null;

                        networkRequest.postImageSuccess(updateTime, data);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        subscription = null;
                        networkRequest.postImageFail(updateTime);
                    }

                });

    }

    public void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public interface NetworkRequest {
        void onProgress(long time, int progress);

        void postImageSuccess(long time, String url);

        void postImageFail(long time);
    }

}
