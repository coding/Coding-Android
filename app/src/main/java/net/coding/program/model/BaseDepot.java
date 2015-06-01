package net.coding.program.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/5/25.
 */
public class BaseDepot implements Serializable {
    protected int id; //: 5164,
    protected int parent_id; // 0,
    protected int project_id; // 5166,
    protected int root_id; // 5164,
    protected String path; // "8206503/AndroidCoding",
    protected String origin_url; // "",
    protected long created_at; // 1407379608000,
    protected String default_branch; // "master",
    protected String depot_path; // "/u/8206503/p/AndroidCoding/git",
    protected String name; // "AndroidCoding",
    protected int status; // 0,
    protected UserObject owner;
    protected String language; //: "Java",
    protected int size;//: 0
    protected boolean hasCommits;

    public BaseDepot(JSONObject json) {
        hasCommits = json.optBoolean("hasCommits");
        path = json.optString("path"); // "8206503/AndroidCoding",
        created_at = json.optLong("created_at"); // 1407379608000,
        if (json.has("owner")) {
            owner = new UserObject(json.optJSONObject("owner"));
        }
        id = json.optInt("id"); //: 5164,
        root_id = json.optInt("root_id"); // 5164,
        name = json.optString("name"); // "AndroidCoding",
        origin_url = json.optString("origin_url"); // "",
        status = json.optInt("status"); // 0,
        depot_path = json.optString("depot_path"); // "/u/8206503/p/AndroidCoding/git",
        project_id = json.optInt("project_id"); // 5166,
        size = json.optInt("size");//: 0
        default_branch = json.optString("default_branch"); // "master",
        parent_id = json.optInt("parent_id"); // 0,
        language = json.optString("language"); //: "Java",
    }

    public String getDefault_branch() {
        return default_branch;
    }
}
