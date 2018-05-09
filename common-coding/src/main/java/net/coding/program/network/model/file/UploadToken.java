package net.coding.program.network.model.file;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/16.
 */
public class UploadToken implements Serializable {

    private static final long serialVersionUID = -6810952851933394651L;

    @SerializedName("uptoken")
    @Expose
    public String uptoken = "";
    @SerializedName("authToken")
    @Expose
    public String authToken = "";
    @SerializedName("time")
    @Expose
    public String time = "";
    @SerializedName("userId")
    @Expose
    public int userId;
}
