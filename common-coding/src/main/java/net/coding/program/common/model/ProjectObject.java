package net.coding.program.common.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.network.constant.MemberAuthority;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-8.
 */

public class ProjectObject implements Serializable {

    private static final long serialVersionUID = 7015515666165386726L;

    @SerializedName("groupId")
    @Expose
    public int groupId;
    @SerializedName("plan")
    @Expose
    public int plan;
    @SerializedName("isTeam")
    @Expose
    public boolean isTeam;
    @SerializedName("max_member")
    @Expose
    public int maxMember;
    @SerializedName("recommended")
    @Expose
    public int recommended;
    @SerializedName("backend_project_path")
    @Expose
    private String backend_project_path = ""; // "/user/cc/project/hell"
    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("owner_id")
    @Expose
    public int owner_id;
    @SerializedName("owner_user_home")
    @Expose
    public String ownerUserHome = "";
    @SerializedName("owner_user_name")
    @Expose
    public String owner_user_name = "";
    @SerializedName("owner_user_picture")
    @Expose
    public String owner_user_picture = "";
    // 企业版里面去掉了 /u 感觉很多地方都要修改了
    @SerializedName("project_path")
    @Expose
    public String project_path = ""; // "/u/cc/p/hell"
    public String ssh_url = "";
    public String current_user_role = "";
    @SerializedName("current_user_role_id")
    @Expose
    public int current_user_role_id; // 0 表示不是项目成员
    public String depot_path = "";
    @SerializedName("description")
    @Expose
    public String description = "";
    public String git_url = "";
    public String https_url = "";
    @SerializedName("icon")
    @Expose
    public String icon = "";
    @SerializedName("fork_count")
    @Expose
    public int fork_count;
    @SerializedName("forked")
    @Expose
    public boolean forked;
    @SerializedName("created_at")
    @Expose
    public long created_at;
    @SerializedName("star_count")
    @Expose
    public int star_count;
    @SerializedName("stared")
    @Expose
    public boolean stared;
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("un_read_activities_count")
    @Expose
    public int unReadActivitiesCount;
    @SerializedName("updated_at")
    @Expose
    public long updateAt;
    @SerializedName("watch_count")
    @Expose
    public int watch_count;
    @SerializedName("watched")
    @Expose
    public boolean watched;
    @SerializedName("is_public")
    @Expose
    public boolean isPublic;
    @SerializedName("member_num")
    @Expose
    public int memberNum; // 这个属性很坑的，getManagerProjects 才能取到，
    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("pin")
    @Expose
    public boolean pin;
    @SerializedName("type")
    @Expose
    public int type;
    @SerializedName("shared")
    @Expose
    public boolean shared;

    private String fork_path = "";
    private DynamicObject.Owner owner;

    public ProjectObject(JSONObject json) {
        backend_project_path = json.optString("backend_project_path", "").replace("/team/", "/user/");
        name = json.optString("name", "");
        owner_id = json.optInt("owner_id");
        ownerUserHome = json.optString("owner_user_home", "");
        owner_user_name = json.optString("owner_user_name", "");
        owner_user_picture = json.optString("owner_user_picture", "");
        project_path = json.optString("project_path", "").replace("/t/", "/u/");
        ssh_url = json.optString("ssh_url", "");
        current_user_role = json.optString("current_user_role", "");
        current_user_role_id = json.optInt("current_user_role_id", 0);
        depot_path = json.optString("depot_path", "");
        description = json.optString("description", "");
        git_url = json.optString("git_url", "");
        https_url = json.optString("https_url", "");
        icon = Global.replaceHeadUrl(json, "icon");
        id = json.optInt("id");
        created_at = json.optLong("created_at");
        updateAt = json.optLong("update_at");
        fork_count = json.optInt("fork_count");
        star_count = json.optInt("star_count");
        status = json.optInt("status");
        type = json.optInt("type");
        unReadActivitiesCount = json.optInt("un_read_activities_count");
        watch_count = json.optInt("watch_count");
        watched = json.optBoolean("watched");
        forked = json.optBoolean("forked");
        isPublic = json.optBoolean("is_public");
        shared = json.optBoolean("shared");
        stared = json.optBoolean("stared");
        pin = json.optBoolean("pin");
        fork_path = json.optString("path", "");
        if (json.has("owner")) {
            owner = new DynamicObject.Owner(json.optJSONObject("owner"));
        }
        memberNum = json.optInt("member_num");
    }

    public String getBackendProjectPath() {
        return backend_project_path.replace("/team/", "/user/");
    }

    public ProjectObject() {
    }

    public String getV2PathByName() {
        return String.format("team/%s/project/%s", owner_user_name, name);
    }

    public String getV2PathById() {
        return String.format("team/%s/project/%s", owner_user_name, id);
    }

    public static String translatePath(String path) {
        return GlobalData.transformEnterpriseUri(path)
                .replace("/u/", "/user/")
                .replace("/t/", "/user/")
                .replace("/p/", "/project/");
    }

    public static String translatePathToOld(String path) {
        return path.replace("/user/", "/u/").replace("/project/", "/p/");
    }

    public static String getTitle(boolean isPull) {
        return isPull ? "Pull Request" : "Merge Request";
    }

    public static String getMdPreview(String projectPath) {
        final String HOST_PREVIEW = Global.HOST_API + "%s/markdownNoAt";
        return String.format(HOST_PREVIEW, projectPath);
    }

    public static String teamPath2User(String path) {
        if (path == null) {
            return "";
        }

        return path.replace("/team/", "/user/").replace("/t/", "/u/");
    }

    public static String getHttpProject(String user, String project) {
        return String.format("%s/user/%s/project/%s", Global.HOST_API, user, project);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean canReadCode() {
        return MemberAuthority.canReadCode(current_user_role_id);
    }

    public boolean canManagerMember() {
        return MemberAuthority.canManagerMember(current_user_role_id);
    }

    public boolean isJoined() {
        return current_user_role_id > 0;
    }

    public void setJoin() {
        current_user_role_id = 10;
    }

    public void setReadActivities() {
        unReadActivitiesCount = 0;
    }

    public boolean isPin() {
        return pin;
    }

    public void setPin(boolean pin) {
        this.pin = pin;
    }

    public boolean isEmpty() {
        return id == 0;
    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return Global.HOST + project_path;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isShared() {
        return shared;
    }

    public String getHttpGitTree(String version) {
        return Global.HOST_API + getBackendProjectPath() + "/git/tree/" + version;
    }

    public String getHttpReadme(String version, String readmeName) {
        return Global.HOST_API + getBackendProjectPath() + "/git/edit/" + version + "%252F" + readmeName;
    }

    public String getHttpReadmePreview(String version, String readmeName) {
//        "https://coding.net/api/user/gggg/project/aa66/git/blob-preview/master%252FREADME.md"
        return Global.HOST_API + getBackendProjectPath() + "/git/blob-preview/" + version + "%252F" + readmeName;
    }

    public String getProjectGit() {
        return Global.HOST_API + getBackendProjectPath() + "/git";
    }

    public String getHttpStar(boolean star) {
        return getHttpUrl(star ? "/star" : "/unstar");
    }

    private String getHttpUrl(String param) {
        return Global.HOST_API + getBackendProjectPath() + param;
    }

    public int getStar_count() {
        return star_count;
    }

    public void setStar_count(int star_count) {
        this.star_count = star_count;
    }

    public String getStarString() {
        return String.valueOf(star_count);
    }

    public int getWatch_count() {
        return watch_count;
    }

    public void setWatch_count(int watch_count) {
        this.watch_count = watch_count;
    }

    public String getWatchCountString() {
        return String.valueOf(watch_count);
    }

    public int getFork_count() {
        return fork_count;
    }

    public void setFork_count(int fork_count) {
        this.fork_count = fork_count;
    }

    public String getForkCountString() {
        return String.valueOf(fork_count);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHttpProjectApi() {
        return Global.HOST_API + getBackendProjectPath();
    }

    public String getHttpWatch(boolean watch) {
        return getHttpUrl(watch ? "/watch" : "/unwatch");
    }

    public String getHttpProjectObject() {
        return Global.HOST_API + getBackendProjectPath();
    }

    public String getProjectPath() { //     /user/cc/project/test
        return translatePath(getBackendProjectPath());
    }

    public boolean isMy() {
        return GlobalData.sUserObject.id == owner_id;
    }

    /*
     * 上传图片的链接，公开项目和私有项目的链接是不同的
     */
    public String getHttpUploadPhoto() {
        if (isPublic) {
            return Global.HOST_API + "/project/" + id + "/upload_public_image";
        } else {
            return Global.HOST_API + "/project/" + id + "/file/upload";
        }
    }

    public static String getPublicTopicUploadPhoto(int projectId) {
        return Global.HOST_API + "/project/" + projectId + "/upload_public_image";
    }

    public String getHttpMerge(boolean open) {
        String type = open ? "open" : "closed";
        String pull = isPublic() ? "/git/pulls/" : "/git/merges/";
        return Global.HOST_API + getBackendProjectPath() + pull + type + "?";
    }

    public String getHttpMergeExamine(boolean open, MergeExamine mineType) {
        String type = open ? "open" : "closed";
        return Global.HOST_API + getBackendProjectPath() + "/git/merges/list/" + mineType + "?&status=" + type;
    }

    public String getHttpDeleteProject2fa(String code) {
        String params = String.format("?name=%s&two_factor_code=%s", name, code);
        return Global.HOST_API + getBackendProjectPath() + params;
    }

    public String getHttpArchiveProject2fa(String code) {
        return String.format("%s/project/%s/archive?two_factor_code=%s", Global.HOST_API, id, code);
    }

    public String getHttpTransferProject(String globalKey) {
        return Global.HOST_API + getBackendProjectPath() + "/transfer_to/" + globalKey;
    }

    public String getHttptStargazers() {
        return Global.HOST_API + getBackendProjectPath() + "/stargazers/paging?pageSize=20";
    }

    public String getHttptwatchers() {
        return Global.HOST_API + getBackendProjectPath() + "/watchers/paging?pageSize=20";
    }

    public String getForkPath() {
        return fork_path;
    }

    public String getMergesFilterAll() {
        String pull = isPublic() ? "/git/pulls/" : "/git/merges/";
        return Global.HOST_API + getBackendProjectPath() + pull + "all?";
    }

    public String getMergesFilter() {
        String pull = "/git/merges/";
        return Global.HOST_API + getBackendProjectPath() + pull + "filter?";
    }

    public String getMergesFilterStatus(String status) {
        String pull = isPublic() ? "/git/pulls/" : "/git/merges/";
        String params = String.format("status=%s", status);
        return Global.HOST_API + getBackendProjectPath() + pull + "filter?" + params;
    }

    public DynamicObject.Owner getOwner() {
        if (owner == null) {
            owner = new DynamicObject.Owner();
        }
        return owner;
    }

    public enum MergeExamine {
        review, mine, other
    }

    public boolean isManagerLevel() {
        return current_user_role_id >= MemberAuthority.manager.type;
    }

}
