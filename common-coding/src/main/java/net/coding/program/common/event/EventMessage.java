package net.coding.program.common.event;

/**
 * Created by chenchao on 2016/12/21.
 */

public class EventMessage {

    public final Type type;

    public EventMessage(Type type) {
        this.type = type;
    }

    public enum Type {
        loginOut
    }

}
