package net.coding.program.network.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.ProjectObject;
import net.coding.program.network.constant.MemberAuthority;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/24.
 */
public class MemberRole implements Serializable {

    private static final long serialVersionUID = -864031372875592743L;

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("project_id")
    @Expose
    public int projectId;
    @SerializedName("user_id")
    @Expose
    public int userId;
    @SerializedName("type")
    @Expose
    public int type;
    @SerializedName("alias")
    @Expose
    public String alias = "";
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("last_visit_at")
    @Expose
    public long lastVisitAt;
    @SerializedName("project")
    @Expose
    public ProjectObject project;

    public MemberAuthority getAuthority() {
        return MemberAuthority.idToEnum(type);
    }
}
