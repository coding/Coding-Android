package net.coding.program.network.model.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.network.model.BaseHttpResult;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/11/20.
 * 本地 apk 的版本号
 */

public class AppVersion extends BaseHttpResult implements Serializable {

    private static final long serialVersionUID = -5029578406976812785L;
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("updated_at")
    @Expose
    public long updatedAt;
    @SerializedName("required")
    @Expose
    public int required;
    @SerializedName("target")
    @Expose
    public int target;
    @SerializedName("build")
    @Expose
    public int build;
    @SerializedName("version")
    @Expose
    public String version = "";
    @SerializedName("url")
    @Expose
    public String url = "";
    @SerializedName("message")
    @Expose
    public String message = "";
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("domain")
    @Expose
    public int domain;
    @SerializedName("id")
    @Expose
    public int id;

}
