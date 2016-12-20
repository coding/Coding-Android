package net.coding.program.model;

/**
 * Created by anfs on 20/12/2016.
 * 项目外 「我的任务、我关注的、我创建的」数量
 */
public class TaskCountModel {

    public long processing;//我的任务」中「进行中的」任务数
    public long done;//「我的任务」中「已完成的」任务数
    public long watchAll;//「我关注的」任务数
    public long watchAllProcessing;//「我关注的」中「进行中的」任务数
    public long create;//「我创建的」任务数
    public long createProcessing;//「我创建的」中「进行中的」任务数

    public int all;
    public int allProcessing;

    //注： - 「我关注的」中「已完成的」数量 = watchAll - watchAllProcessing - 「
    public long getWatcherDoneCount() {
        return watchAll - watchAllProcessing;
    }

    // 我创建的」中「已完成的」数量 = create - createProcessing
    public long getCreatorDoneCount() {
        return create - createProcessing;
    }
}
