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

    public TaskJumpParams(String projectPath, int taskId) {
        String userPrefix = "/t/";
        String projectPrefix = "/p/";
        int userStart = projectPath.indexOf(userPrefix);
        if (userStart == -1) {
            userStart = 0;
        }
        int projectStart = projectPath.indexOf(projectPrefix);
        if (projectStart < userStart) {
            projectStart = userStart;
        }

        userKey = projectPath.substring(userStart + userPrefix.length(), projectStart);
        projectName = projectPath.substring(projectStart + projectPrefix.length(), projectPath.length());
        this.taskId = String.valueOf(taskId);
    }

}
