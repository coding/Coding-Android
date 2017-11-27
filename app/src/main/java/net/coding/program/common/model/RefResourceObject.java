package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/2/24.
 */
public class RefResourceObject implements Serializable {

    public int code;
    public String target_type = "";
    public int target_id;
    public String title = "";
    public String link = "";

    public RefResourceObject(JSONObject json) {
        code = json.optInt("code", 0);
        target_type = json.optString("target_type", "");
        target_id = json.optInt("target_id", 0);
        title = json.optString("title", "");
        link = json.optString("link", "");
    }
}
