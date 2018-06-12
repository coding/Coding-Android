package net.coding.program.common.model;

import android.graphics.PorterDuff;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.R;
import net.coding.program.network.model.task.BoardList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SingleTask implements Serializable {

    public static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final long serialVersionUID = 1792599466384619184L;

    public static final int[] priorityDrawable = new int[]{
            R.drawable.ic_task_priority_0,
            R.drawable.ic_task_priority_1,
            R.drawable.ic_task_priority_2,
            R.drawable.ic_task_priority_3
    };
    public static final int STATUS_PROGRESS = 1;
    public static final int STATUS_FINISH = 2;

    private static String mToday = "";
    private static String mTomorrow = "";

    // 日期要显示是否今天，在这里初始化一次
    public static void initDate() {
        Calendar calendar = Calendar.getInstance();
        SingleTask.mToday = SingleTask.mDateFormat.format(calendar.getTimeInMillis());
        SingleTask.mTomorrow = SingleTask.mDateFormat.format(calendar.getTimeInMillis() + 1000 * 60 * 60 * 24);
    }

    @SerializedName("content")
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
    @SerializedName("task_board_list")
    @Expose
    public BoardList taskBoardList;

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

        if (json.has("task_board_list")) {
            taskBoardList = new Gson().fromJson(json.optString("task_board_list", ""), BoardList.class);
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

    public static void setDeadline(TextView textView, SingleTask data) {
        boolean displayBG = true;
        if (data.deadline.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);

            final int[] taskColors = new int[]{
                    0xFFF68435,
                    0xFFA1CF64,
                    0xFFF56061,
                    0xFF59A2FF,
                    0xFFA9B3BE
            };

            if (data.deadline.equals(SingleTask.mToday)) {
                textView.setText("今天");
                setDeadlineColor(textView, taskColors[0], displayBG);
            } else if (data.deadline.equals(SingleTask.mTomorrow)) {
                textView.setText("明天");
                setDeadlineColor(textView, taskColors[1], displayBG);
            } else {
                if (data.deadline.compareTo(SingleTask.mToday) < 0) {
                    setDeadlineColor(textView, taskColors[2], displayBG);
                } else {
                    setDeadlineColor(textView, taskColors[3], displayBG);
                }
                String num[] = data.deadline.split("-");
                textView.setText(String.format("%s/%s", num[1], num[2]));
            }

            if (data.isDone()) {
                setDeadlineColor(textView, taskColors[4], displayBG);
            }
        }
    }

    // 应该和 setDeadline 一样，然而设计又不统一了
    public static void setBoardDeadline(TextView textView, SingleTask data) {
        boolean displayBG = false;

        if (data.deadline.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);

            final int[] taskColors = new int[]{
                    0xFFF78636,
                    0xFF93A7BF,
                    0xFFFF0000,
                    0xFF93A7BF,
                    0xFFA9B3BE
            };

            if (data.deadline.equals(SingleTask.mToday)) {
                textView.setText("今天");
                setDeadlineColor(textView, taskColors[0], displayBG);
            } else if (data.deadline.equals(SingleTask.mTomorrow)) {
                textView.setText("明天");
                setDeadlineColor(textView, taskColors[1], displayBG);
            } else {
                if (data.deadline.compareTo(SingleTask.mToday) < 0) {
                    setDeadlineColor(textView, taskColors[2], displayBG);
                } else {
                    setDeadlineColor(textView, taskColors[3], displayBG);
                }
                String num[] = data.deadline.split("-");
                textView.setText(String.format("%s/%s", num[1], num[2]));
            }

            if (data.isDone()) {
                setDeadlineColor(textView, taskColors[4], displayBG);
            }
        }
    }

    private static void setDeadlineColor(TextView mDeadline, int color, boolean desplayBG) {
        if (desplayBG) {
            mDeadline.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        mDeadline.setTextColor(color);
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
