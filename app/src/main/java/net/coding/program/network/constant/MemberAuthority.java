package net.coding.program.network.constant;

import net.coding.program.R;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/24.
 * 项目成员在项目中的权限
 */
public enum MemberAuthority implements Serializable {

    ower(100),
    member(80),
    manager(90),
    limited(75),
    noJoin(-1);

    public int type;

    MemberAuthority(int type) {
        this.type = type;
    }

    public static boolean canReadCode(int type) {
        return type >= member.type;
    }

    public static boolean canManagerMember(int type) {
        return type >= manager.type;
    }

    public static MemberAuthority idToEnum(int id) {
        for (MemberAuthority item : MemberAuthority.values()) {
            if (item.type == id) {
                return item;
            }
        }
        return limited;
    }

    public int getIcon() {
        switch (this) {
            case ower:
                return R.drawable.ic_project_member_create;
            case manager:
                return R.drawable.ic_project_member_manager;
            case limited:
                return R.drawable.ic_project_member_limited;
            default: // member
                return 0;
        }
    }

    public int getType() {
        return type;
    }
}
