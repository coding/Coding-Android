package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yangzhen on 14-11-18.
 */
public class GitCommitterObject implements Serializable {
    public String avatar = "";
    public String email = "";
    public String link = "";
    public String name = "";

    public GitCommitterObject(JSONObject json) throws JSONException {
        if (json != null) {
            avatar = json.optString("avatar");
            email = json.optString("email");
            link = json.optString("link");
            name = json.optString("name");
        }
    }

    public GitCommitterObject() {
    }

}
