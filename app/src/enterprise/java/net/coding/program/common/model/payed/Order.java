package net.coding.program.common.model.payed;

import net.coding.program.common.CodingColor;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by chenchao on 2017/4/4.
 * 付费订单
 */

public class Order implements Serializable {

    static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    private static final long serialVersionUID = -2621229953327935467L;

    public String action;
    public long createdAt;
    public String creatorGK;
    public String creatorName;
    public String number;
    public String price;
    public String status;

    public String statusString = "";
    public int statusColor = 0xFF000000;

    public Order(JSONObject json) {
        number = json.optString("number");
        price = json.optString("price");
        status = json.optString("status", "");
        action = json.optString("action", "");
        createdAt = json.optLong("created_at");
        creatorName = json.optString("creator_name", "");
        creatorGK = json.optString("creator_gk", "");

        switch (status.toLowerCase()) {
            case "pending":
                statusString = "等待支付";
                statusColor = CodingColor.fontOrange;
                break;
            case "closed":
                statusString = "关闭";
                statusColor = CodingColor.font4;
                break;
            default: // success
                statusString = "成功";
                statusColor = CodingColor.fontGreen;
                break;
        }
    }

    public String getAction() {
        return String.format("充值 %s 元", price);
    }

    public String getTime() {
        return timeFormat.format(createdAt);
    }

}
