package net.coding.program.model;

import android.text.Html;

import net.coding.program.task.add.TaskAddActivity;

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

    public static int STATUS_PROGRESS = 1;
    public static int STATUS_FINISH = 2;

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
        public String description = "";
        private int number;
        private int id;

        public SingleTask(JSONObject json) throws JSONException {
            this(json, false);
        }

        public SingleTask(JSONObject json, boolean useRaw) throws JSONException {
            comments = json.optInt("comments");
            this.content = json.optString("content", "");
            if (!useRaw) {
                this.content = Html.fromHtml(content).toString();
            }

            created_at = json.optLong("created_at");

            if (json.has("creator")) {
                creator = new UserObject(json.optJSONObject("creator"));
            }
            if (json.has("description")) {
                JSONArray jsonArray = json.optJSONArray("description");
                if (jsonArray.length() > 0) {
                    description = jsonArray.getString(0).toString();
                }
            }
            creator_id = json.optString("creator_id");
            current_user_role_id = json.optString("current_user_role_id");
            id = json.optInt("id");
            priority = json.optInt("priority");
            if (priority >= TaskAddActivity.priorityDrawable.length) {
                priority = TaskAddActivity.priorityDrawable.length - 1;
            }

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
            return String.format("%s/task/%s/label/%s", project.getHttpProjectApi(), id, labelId);
        }

        public int getNumberValue() {
            return number;
        }

        public String getNumber() {
            return "#" + number;
        }
    }

}
