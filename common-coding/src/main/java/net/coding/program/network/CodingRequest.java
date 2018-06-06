package net.coding.program.network;

import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.model.user.IntKeyMapHttpResult;
import net.coding.program.common.model.user.ServiceInfo;
import net.coding.program.network.model.BaseHttpResult;
import net.coding.program.network.model.HttpPageResult;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.code.Branch;
import net.coding.program.network.model.code.BranchMetrics;
import net.coding.program.network.model.code.Release;
import net.coding.program.network.model.common.AppVersion;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.network.model.file.CodingFileView;
import net.coding.program.network.model.file.UploadToken;
import net.coding.program.network.model.task.Board;
import net.coding.program.network.model.task.BoardList;
import net.coding.program.network.model.task.TaskCreating;
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

    @GET("project/{id}")
    Observable<HttpResult<ProjectObject>> getProject(@Path("id") int projectId);

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
    Observable<HttpPageResult<CodingFile>> getFileList(@Path("user") String user,
                                                       @Path("project") String project,
                                                       @Path("folder") int folder);


    @GET("upload_token")
    Observable<HttpResult<UploadToken>> uploadFileToken(@Query("projectId") int projectId,
                                                        @Query("fileName") String fileName,
                                                        @Query("fileSize") long fileSize);

    @GET("upload_token/public/images")
    Observable<HttpResult<UploadToken>> uploadPublicFileToken(@Query("fileName") String fileName,
                                                              @Query("fileSize") long fileSize);


    @FormUrlEncoded
    @POST("user/{user}/project/{project}/folder")
    Observable<HttpResult<CodingFile>> createFolder(@Path("user") String user,
                                                    @Path("project") String project,
                                                    @Field("name") String name,
                                                    @Field("parentId") int parent);

    // https://coding.net/api/user/ease/project/CodingTest/files/3769487/view
    // 文件详情
    @GET("user/{user}/project/{project}/files/{fileId}/view")
    Observable<HttpResult<CodingFileView>> getFileDetail(@Path("user") String user,
                                                         @Path("project") String project,
                                                         @Path("fileId") int fileId);

    // https://coding.net/api/user/ease/project/CodingTest/folder/3813418/fullpath
    // 文件夹详情
    @GET("user/{user}/project/{project}/folder/{folder}/fullpath")
    Observable<HttpResult<ArrayList<CodingFile>>> getDirDetail(@Path("user") String user,
                                                               @Path("project") String project,
                                                               @Path("folder") int folder);

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
    Observable<HttpResult<Boolean>> renameFold(@Path("user") String user,
                                               @Path("project") String project,
                                               @Path("folder") int folder,
                                               @Field("name") String name);

    @FormUrlEncoded
    @PUT("user/{user}/project/{project}/files/{file}/rename")
    Observable<BaseHttpResult> renameFile(@Path("user") String user,
                                          @Path("project") String project,
                                          @Path("file") int folder,
                                          @Field("name") String name);

    @GET("user/2fa/method")
    Observable<HttpResult<String>> need2FA();

    @FormUrlEncoded
    @POST("{projectPath}/delete")
    Observable<BaseHttpResult> deleteProject(@Path("projectPath") String projectPath,
                                             @Field("two_factor_code") String twoFA);

    @POST("user/{user}/project/{project}/quit")
    Observable<BaseHttpResult> quitProject(@Path("user") String user,
                                           @Path("project") String project);

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

    // 检查升级
    @GET("update/app")
    Observable<AppVersion> getAppVersion();

    //    @GET("user/updateInfo")
    @GET("options/skills")
    Observable<HttpResult<HashMap<Integer, String>>> getAllSkills();

    @GET("user/key/{gk}")
    Observable<HttpResult<UserObject>> getUserInfo(@Path("gk") String gk);

    @GET("current_user")
    Observable<HttpResult<UserObject>> getCurrentUser();

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

    // 获取账户详情
    @GET("user/service_info")
    Observable<HttpResult<ServiceInfo>> serviceInfo();

    // 获取默认分支
    @GET("user/{user}/project/{project}/git/branches/default")
    Observable<HttpResult<Branch>> getDefaultBranch(@Path("user") String user,
                                                    @Path("project") String project);

    // 删除分支
    @FormUrlEncoded
    @POST("user/{user}/project/{project}/git/branches/delete")
    Observable<BaseHttpResult> deleteBranch(@Path("user") String user,
                                            @Path("project") String project,
                                            @Field("branch_name") String branchName);

    // 获取分支列表
    @GET("user/{user}/project/{project}/git/branches/filter")
    Observable<HttpPageResult<Branch>> getBranches(@Path("user") String user,
                                                   @Path("project") String project,
                                                   @Query("page") int page,
                                                   @Query("q") String key);

    // 获取分支信息
    @GET("user/{user}/project/{project}/git/branch_metrics?")
    Observable<HttpResult<HashMap<String, BranchMetrics>>> getBranchMetrics(@Path("user") String user,
                                                                            @Path("project") String project,
                                                                            @Query("base") String base,
                                                                            @Query("targets") String targets);


    // 获取发布列表
    @GET("user/{user}/project/{project}/git/releases")
    Observable<HttpPageResult<Release>> getReleases(@Path("user") String user,
                                                    @Path("project") String project,
                                                    @Query("page") int page);


    // 删除发布
    @POST("user/{user}/project/{project}/git/releases/delete/{release}")
    Observable<BaseHttpResult> deleteRelease(@Path("user") String user,
                                             @Path("project") String project,
                                             @Path("release") String releaseName);


    // 修改发布
    @FormUrlEncoded
    @POST("user/{user}/project/{project}/git/releases/update/{tagName}")
    Observable<BaseHttpResult> modifyRelease(@Path("user") String user,
                                             @Path("project") String project,
                                             @Path("tagName") String tagName,
                                             @Field("resource_references") ArrayList<Integer> refs,
                                             @FieldMap Map<String, String> map);


    // 获取单个 release
    @GET("user/{user}/project/{project}/git/releases/tag/{tagName}")
    Observable<HttpResult<Release>> getRelease(@Path("user") String user,
                                               @Path("project") String project,
                                               @Path("tagName") String tagName);

    // 获取 board
    @GET("user/{user}/project/{project}/tasks/board")
    Observable<HttpResult<Board>> getTaskBoard(@Path("user") String user,
                                               @Path("project") String project);


    // 创建 board
    @FormUrlEncoded
    @POST("user/{user}/project/{project}/tasks/board/{boardId}/list")
    Observable<HttpResult<BoardList>> addTaskBoardList(@Path("user") String user,
                                                       @Path("project") String project,
                                                       @Path("boardId") int boardId,
                                                       @Field("title") String title);

    // 修改任务完成状态
    @FormUrlEncoded
    @PUT("task/{id}/status")
    Observable<BaseHttpResult> modifyTaskStatus(@Path("id") int id,
                                                @Field("status") int status);


    // 获取 board
    @GET("user/{user}/project/{project}/tasks/board/{boardId}/list/{listId}/tasks")
    Observable<HttpPageResult<SingleTask>> getTaskBoardList(@Path("user") String user,
                                                            @Path("project") String project,
                                                            @Path("boardId") int boardId,
                                                            @Path("listId") int listId,
                                                            @Query("page") int page,
                                                            @Query("pageSize") int pageSize);

    // 删除 boardlist
    @DELETE("user/{user}/project/{project}/tasks/board/{boardId}/list/{listId}")
    Observable<BaseHttpResult> deleteTaskBoardList(@Path("user") String user,
                                                   @Path("project") String project,
                                                   @Path("boardId") int boardId,
                                                   @Path("listId") int listId);

    // 重命名 boardlist
    @FormUrlEncoded
    @PUT("user/{user}/project/{project}/tasks/board/{boardId}/list/{listId}")
    Observable<BaseHttpResult> renameTaskBoardList(@Path("user") String user,
                                                   @Path("project") String project,
                                                   @Path("boardId") int boardId,
                                                   @Path("listId") int listId,
                                                   @Field("title") String title);

    @GET("task/creating")
    Observable<HttpResult<TaskCreating>> taskCreateParam();

    // 修改任务到 boardlist
    @PUT("user/{user}/project/{project}/tasks/board/{boardId}/list/{listId}/task/{taskId}")
    Observable<BaseHttpResult> moveTaskToBoard(@Path("user") String user,
                                               @Path("project") String project,
                                               @Path("boardId") int boardId,
                                               @Path("listId") int listId,
                                               @Path("taskId") int taskId);
}


