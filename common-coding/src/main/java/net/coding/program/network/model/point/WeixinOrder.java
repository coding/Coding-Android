package net.coding.program.network.model.point;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/11/30.
 */

public class WeixinOrder implements Serializable {

    private static final long serialVersionUID = 5550273792303313792L;

    @SerializedName("package")
    @Expose
    public String _package = "";
    @SerializedName("orderId")
    @Expose
    public String orderId = "";
    @SerializedName("paymentId")
    @Expose
    public String paymentId = "";
    @SerializedName("payMethod")
    @Expose
    public String payMethod = "";
    @SerializedName("appId")
    @Expose
    public String appId = "";
    @SerializedName("sign")
    @Expose
    public String sign = "";
    @SerializedName("partnerId")
    @Expose
    public String partnerId = "";
    @SerializedName("prepayId")
    @Expose
    public String prepayId = "";
    @SerializedName("nonceStr")
    @Expose
    public String nonceStr = "";
    @SerializedName("timestamp")
    @Expose
    public String timestamp = "";
}
