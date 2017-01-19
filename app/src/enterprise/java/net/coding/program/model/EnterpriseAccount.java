package net.coding.program.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/1/19.
 */

public class EnterpriseAccount implements Serializable {

    private static final long serialVersionUID = -8068995659716038945L;

    public String balance;
    public int remaindays;
    public int createdat;
    public boolean trial;

    public EnterpriseAccount(JSONObject json) {
        balance = json.optString("balance");
        remaindays = json.optInt("remain_days");
        createdat = json.optInt("created_at");
        trial = json.optBoolean("trial");
    }
}
