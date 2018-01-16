package net.coding.program.compatible;

import android.text.TextUtils;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;

/**
 * Created by chenchao on 2017/1/17.
 * 企业版和 coding 兼容 url
 */

public class UriCompat {

    public static String createProject() {
        final String host;
        String teamGK = GlobalData.getEnterpriseGK();
        if (TextUtils.isEmpty(teamGK)) {
            host = Global.HOST_API + "/project";
        } else {
            host = String.format("%s/team/%s/project", Global.HOST_API, teamGK);
        }
        return host;
    }

}
