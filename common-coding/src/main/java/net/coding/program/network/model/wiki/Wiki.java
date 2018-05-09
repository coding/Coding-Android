package net.coding.program.network.model.wiki;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.loopj.android.http.RequestParams;

import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.model.Share;
import net.coding.program.common.model.ShareParam;
import net.coding.program.common.model.UserObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by chenchao on 2017/4/11.
 */

public class Wiki implements Serializable, ShareParam {

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
    public String title = "";
    @SerializedName("content")
    @Expose
    public String content = "";
    @SerializedName("msg")
    @Expose
    public String msg = "";
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
    public String html = "";
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
    public String path = "";
    @SerializedName("order")
    @Expose
    public float order;
    @SerializedName("share")
    @Expose
    public Share share = new Share();

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

    @Override
    public RequestData getHttpShareLinkOn(ProjectObject projectObject) {
        String url = Global.HOST_API + "/share/create";
        RequestParams params = new RequestParams();
        params.put("resourceId", iid);
        params.put("resourceType", 2);
        params.put("projectId", projectObject.getId());
        params.put("accessType", 0);
        return new RequestData(url, params);
    }

    @Override
    public String getHttpShareLinkOff() {
        String shareUrl = share.url;
        int pos = shareUrl.lastIndexOf("/");
        if (pos == -1) {
            return "";
        }

        String hash = shareUrl.substring(pos + 1);
        return Global.HOST_API + "/share/" + hash;
    }

    @Override
    public boolean isShared() {
        return !TextUtils.isEmpty(share.url);
    }

    @Override
    public String getShareLink() {
        return share.url;
    }

    @Override
    public void setShereLink(String link) {
        share.url = link;
    }
}
