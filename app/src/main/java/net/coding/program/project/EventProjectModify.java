package net.coding.program.project;

/**
 * Created by chenchao on 2017/12/8.
 * 项目有了改变
 */

public class EventProjectModify {

    public boolean exitProject = false;

    // 修改项目名会导致项目路径变化
    public String projectUrl = null;

    public EventProjectModify() {
    }

    public EventProjectModify setExit() {
        exitProject = true;
        return this;
    }

    public EventProjectModify setProjectUrl(String url) {
        projectUrl = url;
        return this;
    }
}
