package net.coding.program.network.model.user;

import net.coding.program.common.model.UserObject;
import net.coding.program.network.constant.MemberAuthority;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/24.
 * 项目成员
 */
public class Member implements Serializable {

    private static final long serialVersionUID = -3866010293329249701L;

    public long created_at;
    public int id;
    public long last_visit_at;
    public int project_id;
    public int user_id;
    public String alias = "";
    public UserObject user = new UserObject();
    private int type;

    public Member(JSONObject json) {
        created_at = json.optLong("created_at");
        id = json.optInt("id");
        last_visit_at = json.optLong("last_visit_at");
        project_id = json.optInt("project_id");
        type = json.optInt("type");
        user_id = json.optInt("user_id");
        alias = json.optString("alias");

        if (json.has("user")) {
            user = new UserObject(json.optJSONObject("user"));
        }
    }

    public Member(UserObject data) {
        created_at = data.created_at;
        id = data.id;
        last_visit_at = data.last_activity_at;
        project_id = 0;
        type = 0;
        user_id = data.id;
        user = data;
    }

    public Member() {
    }

    public MemberAuthority getType() {
        for (MemberAuthority item : MemberAuthority.values()) {
            if (item.type == type) {
                return item;
            }
        }

        return MemberAuthority.member;
    }

    public boolean isOwner() {
        return getType() == MemberAuthority.ower;
    }

    public boolean isMe() {
        return user.isMe();
    }

}
