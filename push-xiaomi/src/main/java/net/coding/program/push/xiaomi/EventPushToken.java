package net.coding.program.push.xiaomi;

/**
 * Created by chenchao on 2017/11/13.
 */

public class EventPushToken {

    final public String token;
    final public String type;

    public EventPushToken(String type, String token) {
        this.token = token;
        this.type = type;
    }
}
