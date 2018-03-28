package net.coding.program.network;

import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.file.CodingFile;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by chenchao on 2017/5/16.
 */

public interface UpQboxRequest {

    @Multipart
    @POST("?dir=0")
    Observable<HttpResult<CodingFile>> uploadFile(@Part MultipartBody.Part body,
                                                  @Part("key") RequestBody key,
                                                  @Part("x:dir") RequestBody dir,
                                                  @Part("x:projectId") RequestBody projectId,
                                                  @Part("token") RequestBody token,
                                                  @Part("x:time") RequestBody time,
                                                  @Part("x:authToken") RequestBody authToken,
                                                  @Part("x:userId") RequestBody userId);

    @Multipart
    @POST("?")
    Observable<HttpResult<CodingFile>> uploadFile(@Part MultipartBody.Part body,
                                                  @Part("key") RequestBody key,
                                                  @Part("x:dir") RequestBody xdir,
                                                  @Part("x:projectId") RequestBody projectId,
                                                  @Part("token") RequestBody token,
                                                  @Part("x:time") RequestBody time,
                                                  @Part("x:authToken") RequestBody authToken,
                                                  @Part("x:userId") RequestBody userId,

                                                  @Part("x:folderType") RequestBody xfolderType,
                                                  @Part("folderType") RequestBody folderType,
                                                  @Part("dir") RequestBody dir);

    @Multipart
    @POST("?")
    Observable<HttpResult<String>> uploadPublicFile(@Part MultipartBody.Part body,
                                                  @Part("key") RequestBody key,
                                                  @Part("token") RequestBody token,
                                                  @Part("x:time") RequestBody time,
                                                  @Part("x:authToken") RequestBody authToken,
                                                  @Part("x:userId") RequestBody userId);


}
