package net.coding.program.model;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc191954 on 14-8-12.
 */
public class TaskObject {

    public static class Members implements Serializable {
        public static final int MEMBER_TYPE_OWNER = 100;
        public static final int MEMBER_TYPE_MEMBER = 80;
        public long created_at;
        public String id = "";
        public long last_visit_at;
        public String project_id = "";
        public int type;
        public String user_id = "";
        public UserObject user = new UserObject();

        public Members(JSONObject json) throws JSONException {
            created_at = json.optLong("created_at");
            id = json.optString("id");
            last_visit_at = json.optLong("last_visit_at");
            project_id = json.optString("project_id");
            type = json.optInt("type");
            user_id = json.optString("user_id");

            if (json.has("user")) {
                user = new UserObject(json.optJSONObject("user"));
            }
        }

        public Members(UserObject data) {
            created_at = data.created_at;
            id = data.id;
            last_visit_at = data.last_activity_at;
            project_id = "";
            type = 0;
            user_id = data.id;
            user = data;
        }

        public Members() {
        }
    }

    public static class TaskComment extends BaseComment implements Serializable {

        public int taskId;

        public TaskComment(JSONObject json) throws JSONException {
            super(json);

            taskId = json.optInt("taskId");

        }
    }

    public static class UserTaskCount {
        public String done;
        public String processing;
        public String user;

        public UserTaskCount(JSONObject json) throws JSONException {
            done = json.optString("done");
            processing = json.optString("processing");
            user = json.optString("user");
        }
    }

    public static class SingleTask implements Serializable {
        public String content = "";
        public long created_at;
        public UserObject creator = new UserObject();
        public String creator_id = "";
        public String current_user_role_id = "";
        public String id = "";
        public UserObject owner = new UserObject();
        public String owner_id = "";
        public ProjectObject project = new ProjectObject();
        public String project_id = "";
        public int status;
        public int priority;
        public long updated_at;
        public int comments;

        public SingleTask(JSONObject json) throws JSONException {
            comments = json.optInt("comments");
            content = Html.fromHtml(json.optString("content")).toString();
            created_at = json.optLong("created_at");

            if (json.has("creator")) {
                creator = new UserObject(json.optJSONObject("creator"));
            }

            creator_id = json.optString("creator_id");
            current_user_role_id = json.optString("current_user_role_id");
            id = json.optString("id");
            priority = json.optInt("priority");

            if (json.has("owner")) {
                owner = new UserObject(json.optJSONObject("owner"));
            }

            owner_id = json.optString("owner_id");

            if (json.has("project")) {
                project = new ProjectObject(json.optJSONObject("project"));
            }

            project_id = json.optString("project_id");
            status = json.optInt("status");
            updated_at = json.optLong("updated_at");
        }

        public SingleTask() {
        }
    }

}
