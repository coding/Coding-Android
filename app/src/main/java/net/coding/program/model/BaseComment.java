package net.coding.program.model;

import net.coding.program.MyApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class BaseComment implements Serializable {
    public String content = "";
    public long created_at; // 1408614375604,
    public java.lang.String id = ""; // 9291,
    public net.coding.program.model.DynamicObject.Owner owner = new net.coding.program.model.DynamicObject.Owner();
    public java.lang.String owner_id = ""; // 8205,

    public BaseComment(JSONObject json) throws JSONException {
        content = json.optString("content");
        created_at = json.optLong("created_at");
        id = json.optString("id");

        if (json.has("owner")) {
            owner = new DynamicObject.Owner(json.getJSONObject("owner"));
        }

        owner_id = json.optString("owner_id");
    }

    public BaseComment() {
    }

    public boolean isMy() {
        return MyApp.sUserObject.id.equals(owner_id);
    }
}