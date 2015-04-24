package net.coding.program.common;

/**
 * Created by chenchao on 15/4/24.
 */
public class GlobalSetting {

    private GlobalSetting() {
    }

    private static GlobalSetting sGlobalSetting;

    public static GlobalSetting getInstance() {
        if (sGlobalSetting == null) {
            sGlobalSetting = new GlobalSetting();
        }

        return sGlobalSetting;
    }

    private String mNoNotifyGlobalKey = "";

    public void setMessageNoNotify(String globalKey) {
        mNoNotifyGlobalKey = globalKey;
    }

    public void removeMessageNoNotify() {
        mNoNotifyGlobalKey = "";
    }

    public String getMessageNotify() {
        return mNoNotifyGlobalKey;
    }

}
