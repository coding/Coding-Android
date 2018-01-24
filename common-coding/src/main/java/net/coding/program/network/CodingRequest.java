package net.coding.program.network;

import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.model.user.IntKeyMapHttpResult;
import net.coding.program.network.model.BaseHttpResult;
import net.coding.program.network.model.HttpPageResult;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.common.AppVersion;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.network.model.file.UploadToken;
import net.coding.program.network.model.user.ManagerUser;
import net.coding.program.network.model.user.MemberRole;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.network.model.wiki.WikiHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface CodingRequest {

    @GET("user/{user}/project/{project}/wikis")
    Observable<HttpResult<List<Wiki>>> getWikis(@Path("user") String user, @Path("project") String project);

    @GET("user/{user}/project/{project}")
    Observable<HttpResult<ProjectObject>> getProject(@Path("user") String user, @Path("project") String project);

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

    @GET("user/{user}/project/{project}/folder/shared_files?height=90&width=90&page=1&pageSize=500")
    Observable<HttpPageResult<CodingFile>> getShareFileList(@Path("user") String user, @Path("project") String project);

    @GET("user/{user}/project/{project}/folder/{folder}/all?height=90&width=90&page=1&pageSize=500")
    Observable<HttpPageResult<CodingFile>> getFileList(@Path("user") String user, @Path("project") String project,
                                                       @Path("folder") int folder);


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

    // 我参与的项目，取不到项目的成员人数
    @GET("projects?pageSize=1000")
    Observable<HttpPageResult<ProjectObject>> getProjects();

    // 获取企业全部项目, 但获取的项目数据是不全的，需要与 getProjects 才能取到项目数据，
    @GET("team/{enterprise}/projects")
    Observable<HttpResult<List<ProjectObject>>> getManagerProjects(@Path("enterprise") String enterprise);

    // 获取某个成员在全部项目中的权限
    @GET("team/{enterprise}/member/{user}/projects/role")
    Observable<HttpResult<List<MemberRole>>> getUserJoinedProjects(@Path("enterprise") String enterprise,
                                                                   @Path("user") String user);

    //  修改成员在项目中的权限
    @FormUrlEncoded
    @POST("team/{enterprise}/member/{user}/projects/role")
    Observable<HttpResult<BaseHttpResult>> setUserJoinedProjects(@Path("enterprise") String enterprise,
                                                                 @Path("user") String user,
                                                                 @Field("projects") String projects,
                                                                 @Field("roles") String roles);

    //     企业所有者获取参与中的项目中自己的个人信息
    @GET("team/{user}/members")
    Observable<HttpResult<List<ManagerUser>>> getManagerProjectsMember(@Path("user") String user);

    @PUT("project")
    Observable<HttpResult<ProjectObject>> setProjectInfo(@Query("id") int id,
                                                         @Query("name") String name,
                                                         @Query("description") String desc);

    @POST("project/{id}/quit")
    Observable<BaseHttpResult> quitProject(@Path("id") int projectId);

    @POST("team/{enterprise}/member/{user}/role/{role}")
    Observable<BaseHttpResult> setEnterpriseRole(@Path("enterprise") String enterprise,
                                                 @Path("user") String user,
                                                 @Path("role") int role);

    @DELETE("team/{enterprise}/member/{user}")
    Observable<BaseHttpResult> removeEnterpriseMember(@Path("enterprise") String enterprise,
                                                      @Path("user") String user,
                                                      @Query("two_factor_code") String code);

    // 修改项目图片
    @Multipart
    @POST("project/{id}/project_icon")
    Observable<HttpResult<ProjectObject>> setProjectIcon(@Path("id") int projectId,
                                                         @Part MultipartBody.Part body);

    @GET("options/skills")
    Observable<IntKeyMapHttpResult> getSkills();

    @GET("options/degrees")
    Observable<IntKeyMapHttpResult> getDegrees();

    @DELETE("gifts/orders/{orderId}")
    Observable<BaseHttpResult> cancelOrder(@Path("orderId") String orderId);

    // 获取公开项目
    @GET("public/all")
    Observable<HttpPageResult<ProjectObject>> getAllPublic(@Query("page") int page);

    @FormUrlEncoded
    @POST("mobile/device/register")
    Observable<BaseHttpResult> registerPush(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("mobile/device/unregister")
    Observable<BaseHttpResult> unRegisterPush(@FieldMap Map<String, String> map);

    @GET("update/app")
    Observable<AppVersion> getAppVersion();

    //    @GET("user/updateInfo")
    @GET("options/skills")
    Observable<HttpResult<HashMap<Integer, String>>> getAllSkills();

    @GET("user/key/{gk}")
    Observable<HttpResult<UserObject>> getUserInfo(@Path("gk") String gk);

    @GET("user/check")
    Observable<HttpResult<Boolean>> checkGKRegistered(@Query("key") String gk);

    // 注册检查短信验证码
    @FormUrlEncoded
    @POST("account/register/check-verify-code")
    Observable<HttpResult<Boolean>> checkRegisterMessageCode(@Field("phoneCountryCode") String country,
                                                             @Field("phone") String phone,
                                                             @Field("verifyCode") String code);

    // 修改密码检查短信验证码
    @FormUrlEncoded
    @POST("account/phone/code/check")
    Observable<HttpResult<Boolean>> checkMessageCode(@Field("phoneCountryCode") String country,
                                                     @Field("phone") String phone,
                                                     @Field("code") String code,
                                                     @Field("type") String type);
}
