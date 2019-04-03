package net.coding.program.common.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/12/13.
 */
public class Share implements Serializable {

    private static final long serialVersionUID = 5469993347566525191L;

    /**
     * resource_type : 0
     * resource_id : 308811
     * user_id : 7074
     * access_type : 0
     * project_id : 126848
     * overdue : 0
     * created_at : 1440992709000
     * hash : 572e7ead-8b42-4e2f-9d18-b74b48d2195a
     * url : https://coding.net/s/572e7ead-8b42-4e2f-9d18-b74b48d2195a
     */

    @SerializedName("resource_type")
    @Expose
    public int resource_type;
    @SerializedName("resource_id")
    @Expose
    public int resource_id;
    @SerializedName("user_id")
    @Expose
    public int user_id;
    @SerializedName("access_type")
    @Expose
    public int access_type;
    @SerializedName("project_id")
    @Expose
    public int project_id;
    @SerializedName("overdue")
    @Expose
    public long overdue;
    @SerializedName("created_at")
    @Expose
    public long created_at;
    @SerializedName("hash")
    @Expose
    public String hash = "";
    @SerializedName("url")
    @Expose
    public String url = "";

    public Share() {
    }

    public Share(JSONObject json) {
        resource_type = json.optInt("resource_type");
        resource_id = json.optInt("resource_id");
        user_id = json.optInt("user_id");
        access_type = json.optInt("access_type");
        project_id = json.optInt("project_id");
        overdue = json.optLong("overdue");
        created_at = json.optLong("created_at");
        hash = json.optString("hash");
        url = json.optString("url");
    }

    public Share(net.coding.program.network.model.file.Share file) {
        resource_type = file.resourceType;
        resource_id = file.resourceId;
        user_id = file.userId;
        access_type = file.accessType;
        project_id = file.projectId;
        overdue = file.overdue;
        created_at = file.createdAt;
        hash = file.hash;
        url = file.url;
    }

    public String getUrl() {
        return url;
    }
}
