package net.coding.program.event;

/**
 * Created by chenchao on 2016/12/21.
 */

public class EventMessage {

    public enum Type {
        loginOut
    }

    public final Type type;

    public EventMessage(Type type) {
        this.type = type;
    }

}
