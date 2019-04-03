package net.coding.program.common.model.team;

import net.coding.program.network.constant.MemberAuthority;
import net.coding.program.network.model.user.Member;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/13.
 * 团队成员
 */
public class TeamMember extends Member implements Serializable {

    private static final long serialVersionUID = 8647703879552415625L;

    public long updatedat;
    public int teamid;
    public int role;
    public String default2faMethod = "";

    public TeamMember(JSONObject json) {
        super(json);
        updatedat = json.optLong("updated_at", 0);
        teamid = json.optInt("team_id");
        role = json.optInt("role");
        default2faMethod = json.optString("default2faMethod", "");
    }

    @Override
    public MemberAuthority getType() {
        for (MemberAuthority item : MemberAuthority.values()) {
            if (item.getType() == role) {
                return item;
            }
        }

        return MemberAuthority.member;
    }

}
