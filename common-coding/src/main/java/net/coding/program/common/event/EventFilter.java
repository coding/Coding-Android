package net.coding.program.common.event;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 16/9/18.
 */
public class EventFilter {
    public int index;//0 我的项目，1我的任务

    public EventFilter(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            this.index = Integer.parseInt(obj.toString());
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }
}
