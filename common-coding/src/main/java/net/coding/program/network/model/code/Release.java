package net.coding.program.network.model.code;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.UserObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Release implements Serializable {

    private static final long serialVersionUID = -2548704514564834594L;

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("creator_id")
    @Expose
    public int creatorId;
    @SerializedName("project_id")
    @Expose
    public int projectId;
    @SerializedName("iid")
    @Expose
    public int iid;
    @SerializedName("tag_name")
    @Expose
    public String tagName = "";
    @SerializedName("commit_sha")
    @Expose
    public String commitSha = "";
    @SerializedName("target_commitish")
    @Expose
    public String targetCommitish = "";
    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("body")
    @Expose
    public String body = "";
    @SerializedName("markdownBody")
    @Expose
    public String markdownBody = "";
    @SerializedName("compare_tag_name")
    @Expose
    public String compareTagName = "";
    @SerializedName("author")
    @Expose
    public UserObject author;
    @SerializedName("last_commit")
    @Expose
    public LastCommit lastCommit;
    @SerializedName("resource_references")
    @Expose
    public java.util.List<ResourceReference> resourceReferences = new ArrayList<>();
    @SerializedName("attachments")
    @Expose
    public java.util.List<Attachment> attachments = new ArrayList<>();
    @SerializedName("pre")
    @Expose
    public boolean pre;
    @SerializedName("draft")
    @Expose
    public boolean draft;

}
