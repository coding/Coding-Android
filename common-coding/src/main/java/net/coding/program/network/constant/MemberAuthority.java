package net.coding.program.network.constant;

import net.coding.program.R;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/24.
 * 项目成员在项目中的权限
 */
public enum MemberAuthority implements Serializable {

    ower(100, "所有者"),
    member(80, "项目成员"),
    manager(90, "管理员"),
    limited(75, "受限成员"),
    noJoin(-1, "");

    public int type;
    public String projectName; // 项目中的名称

    MemberAuthority(int type, String projectName) {
        this.type = type;
        this.projectName = projectName;
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
