package net.coding.program.common.event;

/**
 * Created by chenchao on 16/9/26.
 */
public class EventNotifyBottomBar {
    private static EventNotifyBottomBar ourInstance = new EventNotifyBottomBar();

    private EventNotifyBottomBar() {
    }

    public static EventNotifyBottomBar getInstance() {
        return ourInstance;
    }
}
