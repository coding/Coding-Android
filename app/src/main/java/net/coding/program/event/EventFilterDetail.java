package net.coding.program.event;

/**
 * Created by anfs on 15/12/2016.
 */

public class EventFilterDetail {

    public EventFilterDetail(String meAction, String label, String status) {
        this.meAction = meAction;
        this.label = label;
        this.status = status;
    }

    public String meAction;
    public String label;
    public String status;
}
