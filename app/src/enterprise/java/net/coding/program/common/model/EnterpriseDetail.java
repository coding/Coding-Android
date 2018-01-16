package net.coding.program.common.model;

import net.coding.program.network.constant.MemberAuthority;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/1/5.
 */

public class EnterpriseDetail implements Serializable {

    private static final long serialVersionUID = 5353429366955903361L;

    public int createdat;
    public int updatedat;
    public String name = "";
    public String namepinyin = "";
    public String introduction = "";
    public String avatar = "";
    public int currentuserroleid;
    public String path = "";
    public String htmllink = "";
    public String globalkey = "";
    public int membercount;
    public int projectcount;
    public boolean locked;
    public int id;
    public UserObject owner;

    // 企业所有者，管理员，普通成员, 这个字段来自另一个 api，感觉专门写个类没必要
    private MemberAuthority identity = MemberAuthority.member;

    public EnterpriseDetail(JSONObject json) {
        id = json.optInt("id");
        createdat = json.optInt("created_at");
        updatedat = json.optInt("updated_at");
        name = json.optString("name", "");
        namepinyin = json.optString("name_pinyin", "");
        introduction = json.optString("introduction", "");
        avatar = json.optString("avatar", "");
        currentuserroleid = json.optInt("current_user_role_id");
        path = json.optString("path", "");
        htmllink = json.optString("html_link", "");
        globalkey = json.optString("global_key", "");
        membercount = json.optInt("member_count");
        projectcount = json.optInt("project_count");
        locked = json.optBoolean("locked");

        if (json.has("owner")) {
            owner = new UserObject(json.optJSONObject("owner"));

            if (owner.isMe()) {
                identity = MemberAuthority.ower;
            }
        }
    }

    public void setIdentity(MemberAuthority identity) {
        this.identity = identity;
    }

    public MemberAuthority getIdentity() {
        return identity;
    }

    public EnterpriseDetail() {
    }

    public String getAvatar() {
        return avatar;
    }

    public int getCreatedat() {
        return createdat;
    }

    public int getCurrentuserroleid() {
        return currentuserroleid;
    }

    public String getGlobalkey() {
        return globalkey;
    }

    public String getHtmllink() {
        return htmllink;
    }

    public int getId() {
        return id;
    }

    public String getIntroduction() {
        return introduction;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getMembercount() {
        return membercount;
    }

    public String getName() {
        return name;
    }

    public String getNamepinyin() {
        return namepinyin;
    }

    public UserObject getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }

    public int getProjectcount() {
        return projectcount;
    }

    public int getUpdatedat() {
        return updatedat;
    }
}
