package net.coding.program.model;

import net.coding.program.MyApp;
import net.coding.program.common.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-8.
 */

public class ProjectObject implements Serializable {
    public String backend_project_path = "";
    public String name = "";
    public int owner_id;
    public String owner_user_home = "";
    public String owner_user_name = "";
    public String owner_user_picture = "";
    public String project_path = "";
    public String ssh_url = "";
    public String current_user_role = "";
    public String current_user_role_id = "";
    public String depot_path = "";
    public String description = "";
    public String git_url = "";
    public String https_url = "";
    public String icon = "";
    private int id;
    public int fork_count;
    public boolean forked;
    public long created_at;
    private boolean is_public;
    public int star_count;
    public boolean stared;
    public int status;
    private int type;
    public int un_read_activities_count;
    public long update_at;
    public int watch_count;
    public boolean watched;

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
        icon = Global.replaceUrl(json, "icon");
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
    }

    public boolean isEmpty() {
        return id == 0;
    }

    public int getId() {
        return id;
    }

    public ProjectObject() {
    }

    public String getPath() {
        return Global.HOST + project_path;
    }

    public boolean isPublic() {
        return is_public;
    }

    public String getHttpGitTree(String version) {
        return Global.HOST + "/api" + backend_project_path + "/git/tree/" + version;
    }

    public String getHttpStar(boolean star) {
        return getHttpUrl(star ? "/star" : "/unstar");
    }

    private String getHttpUrl(String param) {
        return Global.HOST + "/api" + backend_project_path + param;
    }

    public String getHttpWatch(boolean watch) {
        return getHttpUrl(watch ? "/watch" : "/unwatch");
    }

    public String getHttpProjectObject() {
        return Global.HOST + "/api" + backend_project_path;
    }

    public boolean isMy() {
        return MyApp.sUserObject.id == owner_id;
    }
}
