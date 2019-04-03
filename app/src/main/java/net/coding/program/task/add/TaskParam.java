package net.coding.program.task.add;

import java.io.Serializable;

/**
 * Created by chenchao on 2018/1/8.
 */
public class TaskParam implements Serializable {
    String projectPath;
    int taskId;

    public TaskParam(String projectPath, int taskId) {
        this.projectPath = projectPath;
        this.taskId = taskId;
    }
}
