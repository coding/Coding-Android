package net.coding.program.common.model;

import android.content.Context;

import net.coding.program.network.constant.MemberAuthority;

/**
 * Created by chenchao on 2017/1/5.
 */

public class EnterpriseInfo {

    private static EnterpriseInfo enterpriseInfo;

    private EnterpriseDetail detail;

    private EnterpriseInfo() {
    }

    public static EnterpriseInfo instance() {
        if (enterpriseInfo == null) {
            enterpriseInfo = new EnterpriseInfo();
        }

        return enterpriseInfo;
    }

    public void init(Context context) {
        detail = EnterpriseAccountInfo.loadEnterpriseDetail(context);
    }

    public void update(Context context, EnterpriseDetail detail) {
        this.detail = detail;
        EnterpriseAccountInfo.saveEnterpriseDetail(context, detail);
    }

    public MemberAuthority getIdentity() {
        return detail.getIdentity();
    }

    public boolean canManagerEnterprise() {
        MemberAuthority identity = detail.getIdentity();
        return identity == MemberAuthority.ower || identity == MemberAuthority.manager;
    }

    // 是否企业所有者
    public boolean isOwner() {
        return detail.getIdentity() == MemberAuthority.ower;
    }

    public String getAvatar() {
        return detail.getAvatar();
    }

    public UserObject getOwner() {
        return detail.getOwner();
    }

    public int getCurrentuserroleid() {
        return detail.getCurrentuserroleid();
    }

    public String getIntroduction() {
        return detail.getIntroduction();
    }

    public boolean isLocked() {
        return detail.isLocked();
    }

    public int getProjectcount() {
        return detail.getProjectcount();
    }

    public String getName() {
        return detail.getName();
    }

    public int getUpdatedat() {
        return detail.getUpdatedat();
    }

    public String getNamepinyin() {
        return detail.getNamepinyin();
    }

    public String getHtmllink() {
        return detail.getHtmllink();
    }

    public String getGlobalkey() {
        return detail.getGlobalkey();
    }

    public int getCreatedat() {
        return detail.getCreatedat();
    }

    public int getMembercount() {
        return detail.getMembercount();
    }

    public String getPath() {
        return detail.getPath();
    }

    public int getId() {
        return detail.getId();
    }
}
