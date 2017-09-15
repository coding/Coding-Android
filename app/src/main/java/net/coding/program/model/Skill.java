package net.coding.program.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/9/14.
 */

public class Skill implements Serializable {

    private static final long serialVersionUID = -521263171727383003L;

    public String skillName = "";
    public int skillId;
    public int level;

    public Skill(JSONObject json) {
        if (json == null) {
            return;
        }

        skillName = json.optString("skillName", "");
        skillId = json.optInt("skillId");
        level = json.optInt("level");
    }

    @Override
    public String toString() {
        String[] levelString = new String[]{
                "",
                "入门",
                "一般",
                "熟练",
                "精通"
        };

        String s = "";
        if (0 <= level && level < levelString.length) {
            s = levelString[level];
        }

        return String.format("%s · %s", skillName, s);
    }
}
