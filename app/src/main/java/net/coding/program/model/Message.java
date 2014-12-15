package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-27.
 */
public class Message {

    public static class MessageObject implements Serializable {

        public String content = "";
        public int count = 0;
        public long created_at = 0;
        public UserObject friend = new UserObject();
        public String id = "";
        public int read_at;
        public UserObject sender = new UserObject();
        public int status;
        public int unreadCount;

        public MessageObject(JSONObject json) throws JSONException {
            content = json.optString("content");
            count = json.optInt("count");
            created_at = json.optLong("created_at");

            if (json.has("friend")) {
                friend = new UserObject(json.optJSONObject("friend"));
            }

            id = json.optString("id");
            read_at = json.optInt("read_at");

            if (json.has("sender")) {
                sender = new UserObject(json.optJSONObject("sender"));
            }

            status = json.optInt("status");
            unreadCount = json.optInt("unreadCount");
        }

        public MessageObject() {
        }
    }
}
