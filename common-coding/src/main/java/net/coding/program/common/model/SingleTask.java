package net.coding.program.common.model;

import android.text.Html;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class SingleTask implements Serializable {

    private static final long serialVersionUID = 1792599466384619184L;

    public static final int[] priorityDrawable = new int[]{
            R.drawable.ic_task_priority_0,
            R.drawable.ic_task_priority_1,
            R.drawable.ic_task_priority_2,
            R.drawable.ic_task_priority_3
    };
    public static final int STATUS_PROGRESS = 1;
    public static final int STATUS_FINISH = 2;

    @SerializedName("")
    @Expose
    public String content = "";
    @SerializedName("created_at")
    @Expose
    public long created_at;
    @SerializedName("creator")
    @Expose
    public UserObject creator = new UserObject();
    @SerializedName("creator_id")
    @Expose
    public String creator_id = "";
    @SerializedName("current_user_role_id")
    @Expose
    public String current_user_role_id = "";
    @SerializedName("owner")
    @Expose
    public UserObject owner = new UserObject();
    @SerializedName("owner_id")
    @Expose
    public int owner_id;
    @SerializedName("project")
    @Expose
    public ProjectObject project = new ProjectObject();
    @SerializedName("project_id")
    @Expose
    public int project_id;
    @SerializedName("deadline")
    @Expose
    public String deadline = "";
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("priority")
    @Expose
    public int priority;
    @SerializedName("updated_at")
    @Expose
    public long updated_at;
    @SerializedName("comments")
    @Expose
    public int comments;
    @SerializedName("has_description")
    @Expose
    public boolean has_description;
    @SerializedName("labels")
    @Expose
    public ArrayList<TopicLabelObject> labels = new ArrayList<>();
    @SerializedName("description")
    @Expose
    public String description = "";
    @SerializedName("number")
    @Expose
    private int number;
    @SerializedName("id")
    @Expose
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
        if (priority >= priorityDrawable.length) {
            priority = priorityDrawable.length - 1;
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

    public int getPriorityIcon() {
        int icon = priorityDrawable[0];
        if (0 <= priority && priority < priorityDrawable.length) {
            icon = priorityDrawable[priority];
        }
        return icon;
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

    public static class TaskDescription implements Serializable {

        private static final long serialVersionUID = -5806507607883184719L;

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
}
