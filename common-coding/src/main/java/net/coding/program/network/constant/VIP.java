package net.coding.program.network.constant;

import net.coding.program.R;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/31.
 */
public enum VIP implements Serializable {

    normal(1, "普通会员", 0),
    silver(2, "银牌会员", 0),
    gold(3, "金牌会员", R.drawable.member_gold),
    diamond(4, "钻石会员", R.drawable.member_diamond);

    public int id;
    public String alias;
    public int icon;

    VIP(int id, String alias, int icon) {
        this.id = id;
        this.alias = alias;
        this.icon = icon;
    }

    public static VIP id2Enum(int id) {
        for (VIP item : VIP.values()) {
            if (item.id == id) {
                return item;
            }
        }

        return normal;
    }

    public boolean isPayed() {
        return id >= 3;
    }
}
