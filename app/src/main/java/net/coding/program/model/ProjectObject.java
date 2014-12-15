package net.coding.program.model;

import net.coding.program.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-8.
 */

public class ProjectObject implements Serializable {
    public String backend_project_path = "";
    public String name = "";
    public String owner_id = "";
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
    public String id = "";
    public int fork_count;
    public boolean forked;
    public long created_at;
    public boolean is_public;
    public int star_count;
    public boolean stared;
    public int status;
    public int type;
    public int un_read_activities_count;
    public long update_at;
    public int watch_count;
    public boolean watched;

    public ProjectObject(JSONObject json) throws JSONException {
        backend_project_path = json.optString("backend_project_path");
        name = json.optString("name");
        owner_id = json.optString("owner_id");
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
        id = json.optString("id");
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

    public ProjectObject() {
    }
}
