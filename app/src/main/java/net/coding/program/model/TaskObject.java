package net.coding.program.model;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by cc191954 on 14-8-12.
 * 任务的数据结构
 */
public class TaskObject {

    public static int STATUS_PRECESS = 1;
    public static int STATUS_FINISH = 2;

    public static class Members implements Serializable {
        public static final int MEMBER_TYPE_OWNER = 100;
        public static final int MEMBER_TYPE_MEMBER = 80;
        public long created_at;
        public int id;
        public long last_visit_at;
        public int project_id;
        public int type;
        public int user_id;
        public UserObject user = new UserObject();

        public Members(JSONObject json) throws JSONException {
            created_at = json.optLong("created_at");
            id = json.optInt("id");
            last_visit_at = json.optLong("last_visit_at");
            project_id = json.optInt("project_id");
            type = json.optInt("type");
            user_id = json.optInt("user_id");

            if (json.has("user")) {
                user = new UserObject(json.optJSONObject("user"));
            }
        }

        public Members(UserObject data) {
            created_at = data.created_at;
            id = data.id;
            last_visit_at = data.last_activity_at;
            project_id = 0;
            type = 0;
            user_id = data.id;
            user = data;
        }

        public Members() {
        }
    }

    public static class TaskDescription implements Serializable {
        public String description = "";
        public String markdown = "";

        public TaskDescription(JSONObject json) throws JSONException {
            description = json.optString("description");
            markdown = json.optString("markdown");
        }

        public TaskDescription(TaskDescription t) {
            description = t.description;
            markdown = t.markdown;
        }

        public TaskDescription() {
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
        public UserObject owner = new UserObject();
        public int owner_id;
        public ProjectObject project = new ProjectObject();
        public int project_id;
        public String deadline = "";
        public int status;
        public int priority;
        public long updated_at;
        public int comments;
        public boolean has_description;
        public ArrayList<TopicLabelObject> labels = new ArrayList<>();
        private int number;
        private int id;
        public SingleTask(JSONObject json) throws JSONException {
            comments = json.optInt("comments");
            content = Html.fromHtml(json.optString("content")).toString();
            created_at = json.optLong("created_at");

            if (json.has("creator")) {
                creator = new UserObject(json.optJSONObject("creator"));
            }

            creator_id = json.optString("creator_id");
            current_user_role_id = json.optString("current_user_role_id");
            id = json.optInt("id");
            priority = json.optInt("priority");

            if (json.has("owner")) {
                owner = new UserObject(json.optJSONObject("owner"));
            }

            owner_id = json.optInt("owner_id");

            if (json.has("project")) {
                project = new ProjectObject(json.optJSONObject("project"));
            }

            project_id = json.optInt("project_id");
            status = json.optInt("status");
            updated_at = json.optLong("updated_at");
            deadline = json.optString("deadline");
            has_description = json.optBoolean("has_description", false);
            number = json.optInt("number");

            if (json.has("labels")) {
                JSONArray jsonLabals = json.optJSONArray("labels");
                for (int i = 0; i < jsonLabals.length(); ++i) {
                    TopicLabelObject label = new TopicLabelObject(jsonLabals.getJSONObject(i));
                    if (!label.isEmpty()) {
                        labels.add(label);
                    }
                }
            }
        }
        public SingleTask() {
        }

        public boolean isDone() {
            return status == 2;
        }

        public boolean isEmpty() {
            return id == 0;
        }

        public int getId() {
            return id;
        }

        public void removeLabel(int labelId) {
            for (int i = 0; i < labels.size(); ++i) {
                if (labels.get(i).id == labelId) {
                    labels.remove(i);
                    return;
                }
            }
        }

        public String getHttpRemoveLabal(int labelId) {
            return String.format("%s/task/%d/label/%d", project.getHttpProjectApi(), id, labelId);
        }

        public String getNumber() {
            return "#" + number;
        }
    }

}
