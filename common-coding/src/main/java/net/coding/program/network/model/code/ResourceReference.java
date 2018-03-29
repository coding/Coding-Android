package net.coding.program.network.model.code;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ResourceReference implements Serializable {

    private static final long serialVersionUID = 252949013114615996L;

    @SerializedName("code")
    @Expose
    public int code;
    @SerializedName("target_type")
    @Expose
    public String targetType;
    @SerializedName("target_id")
    @Expose
    public int targetId;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("link")
    @Expose
    public String link;
    @SerializedName("img")
    @Expose
    public String img;
    @SerializedName("status")
    @Expose
    public int status;

}
