package net.coding.program.model;

import net.coding.program.MyApp;
import net.coding.program.common.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-8.
 *
 */

public class ProjectObject implements Serializable {
    public String backend_project_path = ""; // "/user/cc/project/hell"
    public String name = "";
    public int owner_id;
    public String owner_user_home = "";
    public String owner_user_name = "";
    public String owner_user_picture = "";
    public String project_path = ""; // "/u/cc/p/hell"
    public String ssh_url = "";
    public String current_user_role = "";
    public String current_user_role_id = "";
    public String depot_path = "";
    public String description = "";
    public String git_url = "";
    public String https_url = "";
    public String icon = "";
    public int fork_count;
    public boolean forked;
    public long created_at;
    public int star_count;
    public boolean stared;
    public int status;
    public int un_read_activities_count;
    public long update_at;
    public int watch_count;
    public boolean watched;
    private int id;
    private boolean is_public;
    private boolean pin;
    private int type;
    private String fork_path = "";
    private DynamicObject.Owner owner;

    public ProjectObject(JSONObject json) throws JSONException {
        backend_project_path = json.optString("backend_project_path");
        name = json.optString("name");
        owner_id = json.optInt("owner_id");
        owner_user_home = json.optString("owner_user_home");
        owner_user_name = json.optString("owner_user_name");
        owner_user_picture = json.optString("owner_user_picture");
        project_path = json.optString("project_path");
        ssh_url = json.optString("ssh_url");
        current_user_role = json.optString("current_user_role");
        current_user_role_id = json.optString("current_user_role_id");
        depot_path = json.optString("depot_path");
        description = json.optString("description");
        git_url = json.optString("git_url");
        https_url = json.optString("https_url");
        icon = Global.replaceHeadUrl(json, "icon");
        id = json.optInt("id");
        created_at = json.optLong("created_at");
        update_at = json.optLong("update_at");
        fork_count = json.optInt("fork_count");
        star_count = json.optInt("star_count");
        status = json.optInt("status");
        type = json.optInt("type");
        un_read_activities_count = json.optInt("un_read_activities_count");
        watch_count = json.optInt("watch_count");
        watched = json.optBoolean("watched");
        forked = json.optBoolean("forked");
        is_public = json.optBoolean("is_public");
        stared = json.optBoolean("stared");
        pin = json.optBoolean("pin");
        fork_path = json.optString("path");
        if (json.has("owner")) {
            owner = new DynamicObject.Owner(json.optJSONObject("owner"));
        }
    }

    public ProjectObject() {
    }

    public static String translatePath(String path) {
        return path.replace("/u/", "/user/").replace("/p/", "/project/");
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

    public void setReadActivities() {
        un_read_activities_count = 0;
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
        return is_public;
    }

    public String getHttpGitTree(String version) {
        return Global.HOST_API + backend_project_path + "/git/tree/" + version;
    }

    public String getProjectGit() {
        return Global.HOST_API + backend_project_path + "/git";
    }

    public String getHttpStar(boolean star) {
        return getHttpUrl(star ? "/star" : "/unstar");
    }

    private String getHttpUrl(String param) {
        return Global.HOST_API + backend_project_path + param;
    }

    public String getHttpProjectApi() {
        return Global.HOST_API + backend_project_path;
    }

    public String getHttpWatch(boolean watch) {
        return getHttpUrl(watch ? "/watch" : "/unwatch");
    }

    public String getHttpProjectObject() {
        return Global.HOST_API + backend_project_path;
    }

    public String getProjectPath() {
        return translatePath(backend_project_path);
    }

    public boolean isMy() {
        return MyApp.sUserObject.id == owner_id;
    }

    /*
     * 上传图片的链接，公开项目和私有项目的链接是不同的
     */
    public String getHttpUploadPhoto() {
        if (is_public) {
            return Global.HOST_API + "/project/" + id + "/upload_public_image";
        } else {
            return Global.HOST_API + "/project/" + id + "/file/upload";
        }
    }

    public String getHttpMerge(boolean open) {
        String type = open ? "open" : "closed";
        String pull = isPublic() ? "/git/pulls/" : "/git/merges/";
        return Global.HOST_API + backend_project_path + pull + type + "?";
    }

    public enum MergeExamine {
        review, mine, other
    }

    public String getHttpMergeExamine(boolean open, MergeExamine mineType) {
        String type = open ? "open" : "closed";
        return Global.HOST_API + backend_project_path + "/git/merges/list/" + mineType + "?&status=" + type;
    }



    public String getHttpDeleteProject2fa(String code) {
        String params = String.format("?name=%s&two_factor_code=%s", name, code);
        return Global.HOST_API + backend_project_path + params;
    }


    public String getHttpTransferProject(String globalKey) {
        return Global.HOST_API + backend_project_path + "/transfer_to/" + globalKey;
    }


    public String getHttptStargazers() {
        return Global.HOST_API + backend_project_path + "/stargazers";
    }

    public String getHttptwatchers() {
        return Global.HOST_API + backend_project_path + "/watchers";
    }

    public String getForkPath() {
        return fork_path;
    }

    public DynamicObject.Owner getOwner() {
        if (owner == null) {
            owner = new DynamicObject.Owner();
        }
        return owner;
    }
}
