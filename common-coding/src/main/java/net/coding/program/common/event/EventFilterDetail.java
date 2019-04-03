package net.coding.program.common.event;

import net.coding.program.common.model.FilterModel;

/**
 * Created by anfs on 15/12/2016.
 */

public class EventFilterDetail {

    public String meAction;
    public String label;
    public String status;
    public String keyword;

    public EventFilterDetail(String meAction, String label, int status, String keyword) {
        this.meAction = meAction;
        this.label = label;
        this.status = String.valueOf(status);
        this.keyword = keyword;
    }

    public EventFilterDetail(String meAction) {
        this.meAction = meAction;
    }

    public EventFilterDetail(String label, String status, String keyword) {
        this.label = label;
        this.status = status;
        this.keyword = keyword;
    }

    public EventFilterDetail(String meAction, FilterModel filterModel) {
        this.meAction = meAction;
        if (filterModel == null) {
            return;
        }
        this.label = filterModel.label;
        this.status = filterModel.status == 0 ? null : String.valueOf(filterModel.status);
        this.keyword = filterModel.keyword;
    }
}
