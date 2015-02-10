package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chaochen on 14-9-11.
 */
public class NotifyObject {

    public String content = "";
    public long created_at;
    public int id;
    public int owner_id;
    private int status; // 0表示未读，1表示已读
    public int target_id;
    public String target_type = "";
    public int type;

    public NotifyObject(JSONObject json) throws JSONException {
        content = json.optString("content"); //<a href=Global.HOST + "/u/8206503">chenchao</a> 在 <a href=Global.HOST + "/u/wangfeiping/pp/6852">收到月饼了，可是还想要抱枕，coding再办...</a> 中提到了你 :@chenchao : vvvvv"
        created_at = json.optLong("created_at");
        id = json.optInt("id"); //40338"
        owner_id = json.optInt("owner_id"); //7074"
        status = json.optInt("status"); //0"
        target_id = json.optInt("target_id"); //14270"
        target_type = json.optString("target_type"); //TweetComment"
        type = json.optInt("type"); //0"
    }

    public boolean isUnRead() {
        return status == 0;
    }

    public void setRead() {
        status = 1;
    }
}
