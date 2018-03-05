package net.coding.program.common.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/19.
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 7792590334229510318L;

    @SerializedName("private")
    @Expose
    public int privateProject;
    @SerializedName("public_project_quota")
    @Expose
    public String publicProjectMax;
    @SerializedName("point_left")
    @Expose
    public double pointLeft;
    @SerializedName("private_project_quota")
    @Expose
    public String privateProjectMax;
    @SerializedName("public")
    @Expose
    public int publicProject;
    @SerializedName("balance")
    @Expose
    public double balance;
    @SerializedName("team")
    @Expose
    public int team;
}
