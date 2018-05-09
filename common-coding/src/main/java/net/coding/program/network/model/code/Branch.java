package net.coding.program.network.model.code;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Branch implements Serializable {

    private static final long serialVersionUID = 4743145549593769483L;

    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("last_commit")
    @Expose
    public LastCommit lastCommit;
    @SerializedName("is_default_branch")
    @Expose
    public boolean isDefaultBranch;
    @SerializedName("is_protected")
    @Expose
    public boolean isProtected;
    @SerializedName("deny_force_push")
    @Expose
    public boolean denyForcePush;
    @SerializedName("force_squash")
    @Expose
    public boolean forceSquash;

    public BranchMetrics metrics;

}