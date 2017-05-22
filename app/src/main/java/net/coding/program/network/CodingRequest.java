package net.coding.program.network;

import net.coding.program.model.ProjectObject;
import net.coding.program.network.model.BaseHttpResult;
import net.coding.program.network.model.HttpPageResult;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.network.model.file.UploadToken;
import net.coding.program.network.model.user.ManagerUser;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.network.model.wiki.WikiHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface CodingRequest {

    @GET("user/{user}/project/{project}/wikis")
    Observable<HttpResult<List<Wiki>>> getWikis(@Path("user") String user, @Path("project") String project);

    @DELETE("user/{user}/project/{project}/wiki/{iid}")
    Observable<HttpResult<Boolean>> deleteWiki(@Path("user") String user, @Path("project") String project, @Path("iid") int iid);

    @FormUrlEncoded
    @POST("user/{user}/project/{project}/wiki")
    Observable<HttpResult<Wiki>> postWiki(@Path("user") String user, @Path("project") String project,
                                          @FieldMap Map<String, String> map);

    @GET("user/{user}/project/{project}/wiki/{id}")
    Observable<HttpResult<Wiki>> getWikiDetail(@Path("user") String user, @Path("project") String project,
                                               @Path("id") int id, @Query("version") int version);

    @GET("user/{user}/project/{project}/wiki/{id}/histories")
    Observable<HttpResult<List<WikiHistory>>> getWikiHistory(@Path("user") String user, @Path("project") String project,
                                                             @Path("id") int id);

    @POST("user/{user}/project/{project}/wiki/{id}/history")
    Observable<HttpResult<WikiHistory>> rollbackWiki(@Path("user") String user, @Path("project") String project,
                                                     @Path("id") int id, @Query("version") int version);

    @GET("user/{user}/project/{project}/folder/{folder}?height=90&width=90&page=1&pageSize=500")
    Observable<HttpPageResult<CodingFile>> getFileList(@Path("user") String user, @Path("project") String project,
                                                       @Path("folder") String folder);


    @GET("upload_token")
    Observable<HttpResult<UploadToken>> uploadFileToken(@Query("projectId") int projectId,
                                                        @Query("fileName") String fileName,
                                                        @Query("fileSize") long fileSize);

    @FormUrlEncoded
    @POST("user/{user}/project/{project}/folder")
    Observable<HttpResult<CodingFile>> createFolder(@Path("user") String user,
                                                    @Path("project") String project,
                                                    @Field("name") String name,
                                                    @Field("parentId") int parent);

    @DELETE("project/{projectId}/file/delete")
    Observable<HttpResult<Integer>> deleteFiles(@Path("projectId") int projectId,
                                           @Query("fileIds") ArrayList<Integer> files);

    @FormUrlEncoded
    @POST("user/{user}/project/{project}/folder/{folder}/move-files")
    Observable<BaseHttpResult> moveFolder(@Path("user") String user,
                                          @Path("project") String project,
                                          @Path("folder") int folder,
                                          @Field("fileId") ArrayList<Integer> files);

    @FormUrlEncoded
    @PUT("user/{user}/project/{project}/folder/{folder}")
    Observable<HttpResult<Boolean>> renameFile(@Path("user") String user,
                                          @Path("project") String project,
                                          @Path("folder") int folder,
                                          @Field("name") String name);

    @GET("user/2fa/method")
    Observable<HttpResult<String>> need2FA();

    @FormUrlEncoded
    @POST("{projectPath}/delete")
    Observable<BaseHttpResult> deleteProject(@Path("projectPath") String projectPath,
                                             @Field("two_factor_code") String twoFA);


    // 我参与的项目
    @GET("projects")
    Observable<HttpPageResult<ProjectObject>> getProjects();

//     企业所有者获取全部项目
    @GET("team/{user}/projects")
    Observable<HttpResult<List<ProjectObject>>> getManagerProjects(@Path("user") String user);

    //     企业所有者获取参与中的项目中自己的个人信息
    @GET("team/{user}/members")
    Observable<HttpResult<List<ManagerUser>>> getManagerProjectsMember(@Path("user") String user);
}
