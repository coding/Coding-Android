package net.coding.program.common.model.payed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by chenchao on 2017/4/4.
 * 付费订单
 */

public class Billing implements Serializable {

    static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    private static final long serialVersionUID = 7066544112808570842L;
    public int id;
    public String price;
    public long billingDate;
    public long createdAt;
    public int userCount;

    public Billing(JSONObject json) {
        id = json.optInt("id");
        price = json.optString("price", "");
        billingDate = json.optLong("billing_date");
        createdAt = json.optLong("created_at");
        JSONArray userArray = json.optJSONArray("details");
        if (userArray != null) {
            userCount = userArray.length();
        }
    }

    public String getTitle() {
        return String.format("结算日：%s", df.format(billingDate));
    }

    public String getTime() {
        return timeFormat.format(createdAt);
    }
}
