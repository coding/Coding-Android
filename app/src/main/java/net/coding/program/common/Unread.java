package net.coding.program.common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chaochen on 14-9-27.
 */
public class Unread {
    public int messages;
    public int notifications;
    public int project_update_count;

    public Unread(JSONObject json) throws JSONException {
        messages = json.getInt("messages");
        notifications = json.getInt("notifications");
        project_update_count = json.getInt("project_update_count");
    }

    public Unread() {
    }

    public String getProject() {
        return countToString(project_update_count);
    }

    public String getNotify() {
        return countToString(notifications + messages);
    }

    public static String countToString(int count) {
        if (count == 0) {
            return "";
        } else if (count > 99) {
            return "99+";
        } else {
            return String.valueOf(count);
        }
    }
}
