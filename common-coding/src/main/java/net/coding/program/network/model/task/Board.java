package net.coding.program.network.model.task;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {

    private static final long serialVersionUID = -3793344647774719204L;
    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("owner_id")
    @Expose
    public int ownerId;
    @SerializedName("project_id")
    @Expose
    public int projectId;
    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("remark")
    @Expose
    public String remark = "";
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("updated_at")
    @Expose
    public long updatedAt;
    @SerializedName("board_lists")
    @Expose
    public List<BoardList> boardLists = new ArrayList<>(0);

}
