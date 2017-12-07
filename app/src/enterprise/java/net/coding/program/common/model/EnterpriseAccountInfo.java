package net.coding.program.common.model;

import android.content.Context;

/**
 * Created by chenchao on 2017/1/18.
 */

public class EnterpriseAccountInfo extends AccountInfo {

    private static final String ENTERPRISE_DETAIL = "ENTERPRISE_DETAIL";

    public static void saveEnterpriseDetail(Context context, EnterpriseDetail data) {
        new DataCache<CustomHost>().saveGlobal(context, data, ENTERPRISE_DETAIL);
    }

    public static EnterpriseDetail loadEnterpriseDetail(Context context) {
        EnterpriseDetail detail = new DataCache<EnterpriseDetail>().loadGlobalObject(context, ENTERPRISE_DETAIL);
        if (detail == null) {
            detail = new EnterpriseDetail();
        }

        return detail;
    }
}
