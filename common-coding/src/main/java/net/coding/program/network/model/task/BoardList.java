package net.coding.program.network.model.task;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.TaskObject;
import net.coding.program.network.model.Pager;

import java.io.Serializable;

public class BoardList implements Serializable {

    private static final long serialVersionUID = -2911048418433272470L;

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
    public String title;
    @SerializedName("remark")
    @Expose
    public String remark;
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
    public Pager<TaskObject> tasks;

}
