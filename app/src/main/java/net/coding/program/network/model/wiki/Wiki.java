package net.coding.program.network.model.wiki;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.model.UserObject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenchao on 2017/4/11.
 */

public class Wiki implements Serializable {

    private static final long serialVersionUID = 3620166483694974192L;

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("creator")
    @Expose
    public UserObject creator;
    @SerializedName("editor")
    @Expose
    public UserObject editor;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("content")
    @Expose
    public String content;
    @SerializedName("msg")
    @Expose
    public String msg;
    @SerializedName("historyId")
    @Expose
    public int historyId;
    @SerializedName("currentUserRoleId")
    @Expose
    public int currentUserRoleId;
    @SerializedName("iid")
    @Expose
    public int iid;
    @SerializedName("html")
    @Expose
    public String html;
    @SerializedName("createdAt")
    @Expose
    public long createdAt;
    @SerializedName("updatedAt")
    @Expose
    public long updatedAt;
    @SerializedName("historiesCount")
    @Expose
    public int historiesCount;
    @SerializedName("lastVersion")
    @Expose
    public int lastVersion;
    @SerializedName("currentVersion")
    @Expose
    public int currentVersion;
    @SerializedName("children")
    @Expose
    public List<Wiki> children = null;
    @SerializedName("parentIid")
    @Expose
    public int parentIid;
    @SerializedName("path")
    @Expose
    public String path;
    @SerializedName("order")
    @Expose
    public float order;

}
