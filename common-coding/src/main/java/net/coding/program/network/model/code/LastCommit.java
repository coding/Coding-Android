package net.coding.program.network.model.code;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.Committer;

import java.io.Serializable;

public class LastCommit implements Serializable {

    private static final long serialVersionUID = 4678248256893573491L;

    @SerializedName("shortMessage")
    @Expose
    public String shortMessage = "";
    @SerializedName("commitId")
    @Expose
    public String commitId = "";
    @SerializedName("commitTime")
    @Expose
    public long commitTime;
    @SerializedName("committer")
    @Expose
    public Committer committer;

}
