package net.coding.program.project;

/**
 * Created by chenchao on 2017/12/8.
 * 项目有了改变
 */

public class EventProjectModify {

    public boolean exitProject = false;

    public EventProjectModify() {}

    public EventProjectModify setExit() {
        exitProject = true;
        return this;
    }
}
