package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/1/6.
 */

public class EnterpriseUserObject implements Serializable {

    private static final long serialVersionUID = 722208774167508837L;

    public int createdat;
    public int updatedat;
    public int teamid;
    public int userid;
    public UserObject user;
    public int role;
    public String alias = "";
    public String default2faMethod = "";

    public EnterpriseUserObject(JSONObject json) {
        createdat = json.optInt("created_at");
        updatedat = json.optInt("updated_at");
        teamid = json.optInt("team_id");
        userid = json.optInt("user_id");
        user = new UserObject(json.optJSONObject("user"));
        role = json.optInt("role");
        alias = json.optString("alias", "");
        default2faMethod = json.optString("default2faMethod", "");
    }

}
