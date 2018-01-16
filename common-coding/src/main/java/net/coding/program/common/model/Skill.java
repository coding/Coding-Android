package net.coding.program.common.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 2017/9/14.
 */

public class Skill implements Serializable {

    private static final long serialVersionUID = -521263171727383003L;

    @SerializedName("skillName")
    @Expose
    public String skillName = "";
    @SerializedName("skillId")
    @Expose
    public int skillId;
    @SerializedName("level")
    @Expose
    public int level;

    public Skill(JSONObject json) {
        if (json == null) {
            return;
        }

        skillName = json.optString("skillName", "");
        skillId = json.optInt("skillId");
        level = json.optInt("level");
    }

    public Skill() {
    }

    @Override
    public String toString() {
        return String.format("%s · %s", skillName, Grade.idToEnum(level).alics);
    }

    public static String[] generateParam(List<Skill> skills) {
        List<String> list = new ArrayList<>();
        for (Skill item : skills) {
            list.add(String.format("%s:%s", item.skillId, item.level));
        }
        return list.toArray(new String[0]);
    }

    public enum Grade {
        first("入门", 1),
        second("一般", 2),
        third("熟练", 3),
        fourth("精通", 4);

        public String alics;
        public int id;

        Grade(String alics, int id) {
            this.alics = alics;
            this.id = id;
        }

        public static Grade idToEnum(int id) {
            for (Grade item : Grade.values()) {
                if (item.id == id) {
                    return item;
                }
            }

            return first;
        }

        public static Grade aliceToEnum(String alics) {
            for (Grade item : Grade.values()) {
                if (item.alics.equals(alics)) {
                    return item;
                }
            }

            return first;
        }
    }
}
