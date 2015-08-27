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
        private int id = 0;
        public int read_at;
        public UserObject sender = new UserObject();
        public int status;
        public int unreadCount;
        public int duration;
        public String file;
        public int type;
        public int played;
        public String extra;

        public MessageObject(JSONObject json) throws JSONException {
            content = json.optString("content");
            count = json.optInt("count");
            created_at = json.optLong("created_at");

            if (json.has("friend")) {
                friend = new UserObject(json.optJSONObject("friend"));
            }

            id = json.optInt("id");
            read_at = json.optInt("read_at");

            if (json.has("sender")) {
                sender = new UserObject(json.optJSONObject("sender"));
            }

            if(json.has("type")){
                type = json.getInt("type");
            }
            if(json.has("played")){
                played = json.getInt("played");
            }

            if(json.has("file")){
                file = json.getString("file");
            }

            if(json.has("duration")){
                duration = json.optInt("duration");
            }

            status = json.optInt("status");
            unreadCount = json.optInt("unreadCount");
        }

        public MessageObject() {
        }

        public int getId() {
            return id;
        }

    }
}
