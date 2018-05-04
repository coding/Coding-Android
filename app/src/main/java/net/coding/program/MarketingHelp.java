package net.coding.program;

import android.content.Context;

import net.coding.program.common.Global;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.MarkedMarketingData;

/**
 * Created by chenchao on 16/1/18.
 * 打开市场活动的链接
 * todo delete
 */
public class MarketingHelp {

    private static String url = "";

    public static void setUrl(String url) {
        MarketingHelp.url = url;
    }

    public static void showMarketing(Context context) {
        if (url == null || !url.startsWith(Global.DEFAULT_HOST)) {
            return;
        }

        MarkedMarketingData data = AccountInfo.loadGlobalMarkedMarketing(context);
        if (!data.marked(url)) {
            WebActivity_.intent(context).url(url).start();
            data.add(url);
            AccountInfo.saveGlobalMarkedMarketing(context, data);
        } else {
            url = "";
        }
    }

}
