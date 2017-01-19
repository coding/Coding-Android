package net.coding.program.model.team;

import net.coding.program.model.TaskObject;
import net.coding.program.model.UserObject;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/13.
 */
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 8647703879552415625L;

    public int createdat;
    public int updatedat;
    public int teamid;
    public int userid;
    public UserObject user;
    public int role;
    public String alias = "";
    public String default2faMethod = "";

    public TeamMember(JSONObject json) {
        createdat = json.optInt("created_at");
        updatedat = json.optInt("updated_at");
        teamid = json.optInt("team_id");
        userid = json.optInt("user_id");
        user = new UserObject(json.optJSONObject("user"));
        role = json.optInt("role");
        alias = json.optString("alias", "");
        default2faMethod = json.optString("default2faMethod", "");
    }


    public TaskObject.Members.Type getType() {
        for (TaskObject.Members.Type item : TaskObject.Members.Type.values()) {
            if (item.getType() == role) {
                return item;
            }
        }

        return TaskObject.Members.Type.member;
    }

}
