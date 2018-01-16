package net.coding.program.common.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenchao on 2017/11/28.
 */
public class MarkedMarketingData implements Serializable {
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
