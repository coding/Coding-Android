package net.coding.program.common.model;

/**
 * Created by anfs on 20/12/2016.
 * 项目内 「我的任务、我关注的、我创建的」中「进行中的、已完成的」的数量
 */
public class TaskProjectCountModel {

    public long owner;
    public long ownerDone;
    public long ownerProcessing;

    public long creator;
    public long creatorDone;
    public long creatorProcessing;

    public long watcher;
    public long watcherDone;
    public long watcherProcessing;
}
