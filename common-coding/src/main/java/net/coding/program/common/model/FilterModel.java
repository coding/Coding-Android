package net.coding.program.common.model;

import java.util.List;

/**
 * Created by anfs on 20/12/2016.
 * 项目筛选条件
 */
public class FilterModel {
    public int status;//任务状态，进行中的为1，已完成的为2
    public String label;//任务标签
    public String keyword;//根据关键字筛选任务
    public long statusTaskDoing;//进行中任务数
    public long statusTaskDone;//已完成任务数
    public List<TaskLabelModel> labelModels;//任务标签列表

    public FilterModel() {
        status = 0;
        label = null;
        keyword = null;
    }

    public FilterModel(int status, String keyword) {
        this.status = status;
        this.label = null;
        this.keyword = keyword;
    }

    public FilterModel(String label, String keyword) {
        this.label = label;
        this.status = 0;
        this.keyword = keyword;
    }

    public FilterModel(List<TaskLabelModel> labelModels) {
        this.labelModels = labelModels;
    }

    public boolean isFilter() {
        return status != 0 || label != null && keyword != null;
    }
}
