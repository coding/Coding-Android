package net.coding.program.network.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.UserObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/22.
 * 管理用户
 */

public class ManagerUser implements Serializable {

    private static final long serialVersionUID = -6493280218667587516L;

    @SerializedName("created_at")
    @Expose
    public int createdAt;
    @SerializedName("updated_at")
    @Expose
    public int updatedAt;
    @SerializedName("team_id")
    @Expose
    public int teamId;
    @SerializedName("user_id")
    @Expose
    public int userId;
    @SerializedName("user")
    @Expose
    public UserObject user;
    @SerializedName("role")
    @Expose
    public int role;
    @SerializedName("alias")
    @Expose
    public String alias = "";
    @SerializedName("default2faMethod")
    @Expose
    public String default2faMethod = "";
}
