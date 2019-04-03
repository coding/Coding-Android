package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/3/26.
 */
public class Depot implements Serializable {

    private static final long serialVersionUID = -9111895567170031794L;

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

    public Depot(JSONObject json) {
        this.hasCommits = json.optBoolean("hasCommits");
        this.path = json.optString("path"); // "8206503/AndroidCoding",
        this.created_at = json.optLong("created_at"); // 1407379608000,
        if (json.has("owner")) {
            this.owner = new UserObject(json.optJSONObject("owner"));
        }
        this.id = json.optInt("id"); //: 5164,
        this.root_id = json.optInt("root_id"); // 5164,
        this.name = json.optString("name"); // "AndroidCoding",
        this.origin_url = json.optString("origin_url"); // "",
        this.status = json.optInt("status"); // 0,
        this.depot_path = json.optString("depot_path"); // "/u/8206503/p/AndroidCoding/git",
        this.project_id = json.optInt("project_id"); // 5166,
        this.size = json.optInt("size");//: 0
        this.default_branch = json.optString("default_branch"); // "master",
        this.parent_id = json.optInt("parent_id"); // 0,
        this.language = json.optString("language"); //: "Java",
    }

    public String getDefault_branch() {
        return default_branch;
    }
}

