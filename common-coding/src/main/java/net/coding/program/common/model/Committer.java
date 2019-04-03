package net.coding.program.common.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.Global;

import org.json.JSONObject;

import java.io.Serializable;

public class Committer implements Serializable {

    private static final long serialVersionUID = -5890576229953980226L;

    @SerializedName("name")
    @Expose
    public String name; // "1984nn",
    @SerializedName("email")
    @Expose
    public String email; // "chenchao@coding.net",
    @SerializedName("avatar")
    @Expose
    public String avatar; // "https; ////dn-coding-net-production-static.qbox.me/8ea73108-5ead-49f2-9153-000de9b7318e.jpg",
    @SerializedName("link")
    @Expose
    public String link; // "/u/1984"

    public Committer(JSONObject json) {
        name = json.optString("name");
        email = json.optString("email");
        avatar = Global.replaceAvatar(json);
        link = json.optString("link");
    }

    public Committer() {
    }
}
