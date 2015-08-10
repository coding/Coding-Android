package net.coding.program.model;

import net.coding.program.common.Global;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/8/5.
 * 码币交易历史记录
 */
public class PointObject implements Serializable {

    private double points_left;
    private String remark;
    private int action;
    private long created_at;
    private String usage;
    private double points_change;

    public PointObject(JSONObject json) {
        points_left = json.optDouble("points_left");
        remark = json.optString("remark");
        action = json.optInt("action");
        created_at = json.optLong("created_at");
        usage = json.optString("usage");
        points_change = json.optDouble("points_change");
    }

    public static String getHttpRecord() {
        return Global.HOST_API + "/point/records?";
    }

    public static String getHttpPointsAll() {
        return Global.HOST_API + "/point/points";
    }

    public double getPoints_left() {
        return points_left;
    }

    public void setPoints_left(double points_left) {
        this.points_left = points_left;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getUsage() {
        return usage;
    }

    public boolean isIncome() {
        return action == 1;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public double getPoints_change() {
        return points_change;
    }

    public void setPoints_change(double points_change) {
        this.points_change = points_change;
    }
}
