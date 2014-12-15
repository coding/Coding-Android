package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-18.
 */
public class TopicObject implements Serializable {
    public int child_count;
    public String content = "";
    public long created_at;
    public String current_user_role_id = "";
    public String id = "";
    public DynamicObject.Owner owner;
    public String owner_id = "";
    public String parent_id = "";
    public ProjectObject project;
    public String project_id = "";
    public String title = "";
    public long updated_at;

    public TopicObject(JSONObject json) throws JSONException {
        child_count = json.optInt("child_count");
        content = json.optString("content");
        created_at = json.optLong("created_at");
        current_user_role_id = json.optString("current_user_role_id");
        id = json.optString("id");

        if (json.has("owner")) {
            owner = new DynamicObject.Owner(json.optJSONObject("owner"));
        }

        owner_id = json.optString("owner_id");
        parent_id = json.optString("parent_id");

        if (json.has("project")) {
            project = new ProjectObject(json.optJSONObject("project"));
        }

        project_id = json.optString("project_id");
        title = json.optString("title");
        updated_at = json.optLong("updated_at");
    }
}