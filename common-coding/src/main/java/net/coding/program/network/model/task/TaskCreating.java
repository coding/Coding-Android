package net.coding.program.network.model.task;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskCreating implements Serializable {

    private static final long serialVersionUID = 3085154988465949693L;

    @SerializedName("task_board_list")
    @Expose
    public ArrayList<BoardList> taskBoardList;
}
