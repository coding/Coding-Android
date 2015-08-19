package net.coding.program.model;

import org.json.JSONObject;

/**
 * Created by chenchao on 15/8/18.
 * 文件的历史版本数据
 */
public class AttachmentFileHistoryObject extends AttachmentFileObject {

    private String remark = "";
    private int action;
    private int version;
    private String action_msg = "";
    private int history_id;

    public AttachmentFileHistoryObject(JSONObject json) {
        super(json);

        remark = json.optString("remark");
        action = json.optInt("action");
        version = json.optInt("version");
        action_msg = json.optString("action_msg");
        history_id = json.optInt("history_id");
    }

    public String getVersionString() {
        return "V" + version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getVersion() {
        return version;
    }

    public int getHistory_id() {
        return history_id;
    }
}
