package net.coding.program.network.constant;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/31.
 */
public enum VIP implements Serializable {

    normal(1),
    silver(2),
    gold(3),
    diamond(4);

    public int id;

    VIP(int id) {
        this.id = id;
    }

    public static VIP id2Enum(int id) {
        for (VIP item : VIP.values()) {
            if (item.id == id) {
                return item;
            }
        }

        return normal;
    }
}
