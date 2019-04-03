package net.coding.program.network.model.file;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Share implements Serializable {

    private static final long serialVersionUID = 1350215870059841873L;

    @SerializedName("resource_type")
    @Expose
    public int resourceType;
    @SerializedName("resource_id")
    @Expose
    public int resourceId;
    @SerializedName("user_id")
    @Expose
    public int userId;
    @SerializedName("access_type")
    @Expose
    public int accessType;
    @SerializedName("project_id")
    @Expose
    public int projectId;
    @SerializedName("overdue")
    @Expose
    public long overdue;
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("hash")
    @Expose
    public String hash = "";
    @SerializedName("url")
    @Expose
    public String url = "";

}
