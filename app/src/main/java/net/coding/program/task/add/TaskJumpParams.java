package net.coding.program.task.add;

import java.io.Serializable;

/**
 * Created by chenchao on 15/7/8.
 * 由 id 跳转到任务编辑界面的参数
 */
public class TaskJumpParams implements Serializable {
    public String userKey;
    public String projectName;
    public String taskId;

    public TaskJumpParams(String user, String project, String task) {
        userKey = user;
        projectName = project;
        taskId = task;
    }
}
