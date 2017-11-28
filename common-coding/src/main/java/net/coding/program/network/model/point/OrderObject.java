package net.coding.program.network.model.point;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/9/22.
 */

public class OrderObject implements Serializable {

    private static final long serialVersionUID = -3976404646002488093L;

    @SerializedName("orderId")
    @Expose
    public String orderId = "";
    @SerializedName("paymentId")
    @Expose
    public String paymentId = "";
    @SerializedName("payMethod")
    @Expose
    public String payMethod = "Alipay";
    @SerializedName("url")
    @Expose
    public String url = "";
}
