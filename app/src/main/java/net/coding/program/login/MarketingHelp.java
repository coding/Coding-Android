package net.coding.program.login;

import android.content.Context;

import net.coding.program.WebActivity_;
import net.coding.program.common.Global;
import net.coding.program.model.AccountInfo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenchao on 16/1/18.
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

    public static class MarkedMarketingData implements Serializable {
        public String mGlobalKey = "";
        public ArrayList<String> mReadData = new ArrayList<>();

        public MarkedMarketingData(String mGlobalKey) {
            this.mGlobalKey = mGlobalKey;
        }

        public void add(String url) {
            mReadData.add(url);
        }

        public boolean marked(String url) {
            for (String item : mReadData) {
                if (item.equals(url)) {
                    return true;
                }
            }

            return false;
        }
    }

}
