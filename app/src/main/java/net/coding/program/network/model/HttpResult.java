package net.coding.program.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class HttpResult<T> implements Serializable {

    private static final long serialVersionUID = -6843431901532382029L;

    @SerializedName("code")
    @Expose
    public int code;
    @SerializedName("msg")
    @Expose
    public Map<String, String> msg;
    @SerializedName("data")
    @Expose
    public T data;

    public String getErrorMessage() {
        if (msg != null && msg.size() > 0) {
            return msg.values().iterator().next();
        } else {
            return "未知错误";
        }
    }
}
