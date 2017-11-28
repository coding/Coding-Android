package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by libo on 2015/11/22.
 */
public class MallOrderObject implements Serializable {

    public static final int STATUS_NO_PAY = 3;
    public static final int STATUS_NO_SEND = 0;
    public static final int STATUS_AREALY_SEDN = 1;
    public static final int STATUS_FINISH = 2;

    /**
     * id: 3340,
     * userId: 57423,
     * giftId: 23,
     * pointsCost: 1,
     * receiverName: "廖益平",
     * receiverAddress: "北京朝阳区望京SOHO塔三B座1909",
     * receiverPhone: "18612184288",
     * createdAt: 1448174663000,
     * status: 0,
     * orderNo: "2015112200004",
     * expressNo: "",
     * giftName: "码币换书(每月10日)",
     * giftImage: "https://dn-coding-net-production-static.qbox.me/51ac24b1-ef09-45f2-af20-62b7f7a40b79.jpg",
     * remark: "签名"
     */
    private String orderNo = "";

    private int id;

    private long userId;

    private int giftId;

    private String expressNo = "";

    private String name = "";

    public String pointsCost;

    private String receiverName = "";

    private String receiverPhone = "";

    private String giftImage = "";

    private int status;

    private long createdAt;

    private String receiverAddress = "";

    private String remark = "";

    private String optionName = "";

    public String paymentAmount = "";
    public String pointDiscount = "";

    public MallOrderObject() {
    }

    public MallOrderObject(JSONObject object) {
        id = object.optInt("id");
        orderNo = object.optString("orderNo", "");
        giftId = object.optInt("giftId");
        name = object.optString("giftName", "");
        pointsCost = object.optString("pointsCost", "0.00");
        receiverName = object.optString("receiverName", "");
        receiverPhone = object.optString("receiverPhone", "");
        status = object.optInt("status");
        createdAt = object.optLong("createdAt");
        receiverAddress = object.optString("receiverAddress", "");
        userId = object.optLong("userId");
        giftImage = object.optString("giftImage", "");
        remark = object.optString("remark", "");
        expressNo = object.optString("expressNo", "");
        optionName = object.optString("optionName", "");
        paymentAmount = object.optString("paymentAmount", "0.00");
        pointDiscount = object.optString("pointDiscount", "0.00");
    }

    public String getOptionName() {
        return optionName;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGiftImage() {
        return giftImage;
    }

    public void setGiftImage(String giftImage) {
        this.giftImage = giftImage;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }
}
