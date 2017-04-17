package net.coding.program.network.model.wiki;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.model.UserObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by chenchao on 2017/4/11.
 */

public class Wiki implements Serializable {

    static final SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd hh:mm");

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

    public void update(Wiki wiki) {
        this.id = wiki.id;
        this.creator = wiki.creator;
        this.editor = wiki.editor;
        this.title = wiki.title;
        this.content = wiki.content;
        this.msg = wiki.msg;
        this.historyId = wiki.historyId;
        this.currentUserRoleId = wiki.currentUserRoleId;
        this.iid = wiki.iid;
        this.html = wiki.html;
        this.createdAt = wiki.createdAt;
        this.updatedAt = wiki.updatedAt;
        this.historiesCount = wiki.historiesCount;
        this.lastVersion = wiki.lastVersion;
        this.currentVersion = wiki.currentVersion;
        this.children = wiki.children;
        this.parentIid = wiki.parentIid;
        this.path = wiki.path;
        this.order = wiki.order;
    }

    public String getTitleTip() {
        String time = timeFormat.format(updatedAt);
        return String.format("%s   更新于 %s   当前版本 %s", editor.name, time, currentVersion);
    }
}
