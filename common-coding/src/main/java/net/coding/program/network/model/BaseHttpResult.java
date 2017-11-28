package net.coding.program.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by chenchao on 2017/5/17.
 * 不关心返回值
 */
public class BaseHttpResult implements Serializable {

    private static final long serialVersionUID = 6211341232240386579L;

    @SerializedName("code")
    @Expose
    public int code;
    @SerializedName("msg")
    @Expose
    public Map<String, String> msg;

    public String getErrorMessage() {
        if (msg != null && msg.size() > 0) {
            return msg.values().iterator().next();
        } else {
            return "未知错误";
        }
    }

}
