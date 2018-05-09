package net.coding.program.network.model.task;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.SingleTask;
import net.coding.program.network.model.Pager;

import java.io.Serializable;

public class BoardList implements Serializable {

    private static final long serialVersionUID = -2911048418433272470L;

    public static final int PENDING = 1;
    public static final int FINISHED = 2;
    public static final int WELCOME = 100;     // 自定义属性
    public static final int ADD = 101;     // 自定义属性

    public static BoardList obtainWelcomeBoard() {
        BoardList b = new BoardList();
        b.type = WELCOME;
        return b;
    }

    public static BoardList obtainAddBoard() {
        BoardList b = new BoardList();
        b.type = ADD;
        return b;
    }

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("owner_id")
    @Expose
    public int ownerId;
    @SerializedName("project_id")
    @Expose
    public int projectId;
    @SerializedName("board_id")
    @Expose
    public int boardId;
    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("remark")
    @Expose
    public String remark = "";
    @SerializedName("order")
    @Expose
    public int order;
    @SerializedName("type")
    @Expose
    public int type;
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("updated_at")
    @Expose
    public long updatedAt;
    @SerializedName("tasks")
    @Expose
    public Pager<SingleTask> tasks;

    public boolean isPending() {
        return type == PENDING;
    }

    public boolean isFinished() {
        return type == FINISHED;
    }

    public boolean isWelcome() {
        return type == WELCOME;
    }

    public boolean isAdd() {
        return type == ADD;
    }
}
