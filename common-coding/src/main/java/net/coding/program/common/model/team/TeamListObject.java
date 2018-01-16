package net.coding.program.common.model.team;

import net.coding.program.common.model.DynamicObject;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/12.
 */
public class TeamListObject implements Serializable {

    private static final long serialVersionUID = 3062367363127218437L;

    public int id;
    public DynamicObject.Owner owner;
    public int createdat;
    public int updatedat;
    public String name = "";
    public String namepinyin = "";
    public String introduction = "";
    public String avatar = "";
    public String path = "";
    public String htmllink = "";
    public String globalkey = "";
    public int membercount;
    public int projectcount;

    public TeamListObject(JSONObject json) {
        id = json.optInt("id");
        owner = new DynamicObject.Owner(json.optJSONObject("owner"));
        createdat = json.optInt("created_at");
        updatedat = json.optInt("updated_at");
        name = json.optString("name", "");
        namepinyin = json.optString("name_pinyin", "");
        introduction = json.optString("introduction", "");
        avatar = json.optString("avatar", "");
        path = json.optString("path", "");
        htmllink = json.optString("html_link", "");
        globalkey = json.optString("global_key", "");
        membercount = json.optInt("member_count");
        projectcount = json.optInt("project_count");
    }

}
