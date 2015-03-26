package net.coding.program.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/3/26.
 */
public class Depot implements Serializable {

    private int id; //: 5164,
    private int parent_id; // 0,
    private int project_id; // 5166,
    private int root_id; // 5164,
    private String path; // "8206503/AndroidCoding",
    private String origin_url; // "",
    private long created_at; // 1407379608000,
    private String default_branch; // "master",
    private String depot_path; // "/u/8206503/p/AndroidCoding/git",
    private String name; // "AndroidCoding",
    private int status; // 0,
    private UserObject owner;
    private String language; //: "Java",
    private int size;//: 0
    private boolean hasCommits;
    private Parent parent;

    public Depot(JSONObject json) {
        id = json.optInt("id"); //: 5164,
        parent_id = json.optInt("parent_id"); // 0,
        project_id = json.optInt("project_id"); // 5166,
        root_id = json.optInt("root_id"); // 5164,
        path = json.optString("path"); // "8206503/AndroidCoding",
        origin_url = json.optString("origin_url"); // "",
        created_at = json.optLong("created_at"); // 1407379608000,
        default_branch = json.optString("default_branch"); // "master",
        depot_path = json.optString("depot_path"); // "/u/8206503/p/AndroidCoding/git",
        name = json.optString("name"); // "AndroidCoding",
        status = json.optInt("status"); // 0,
        owner = new UserObject(json.optJSONObject("owner"));
        language = json.optString("language"); //: "Java",
        size = json.optInt("size");//: 0
        hasCommits = json.optBoolean("hasCommits");
        parent = new Parent(json.optJSONObject("parent"));
    }

    public String getDefault_branch() {
        return default_branch;
    }

    private static class Parent implements Serializable {

        private boolean hasCommits;//: false,
        private int size; //: 0

        public Parent(JSONObject json) {
            hasCommits = json.optBoolean("hasCommits");
            size = json.optInt("size");
        }
    }

//    private     private     languages: {
//    private     private Java: 100
//    private     },
}

