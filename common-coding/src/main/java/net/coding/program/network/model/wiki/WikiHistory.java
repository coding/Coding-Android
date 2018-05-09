package net.coding.program.network.model.wiki;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.model.UserObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by chenchao on 2017/4/14.
 * wiki 历史数据
 */
public class WikiHistory implements Serializable {

    static final SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd hh:mm");

    @SerializedName("historyId")
    @Expose
    public long historyId;
    @SerializedName("content")
    @Expose
    public String content = "";
    @SerializedName("html")
    @Expose
    public String html = "";
    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("msg")
    @Expose
    public String msg = "";
    @SerializedName("type")
    @Expose
    public String type = "";
    @SerializedName("wikiId")
    @Expose
    public int wikiId;
    @SerializedName("version")
    @Expose
    public int version;
    @SerializedName("createdAt")
    @Expose
    public long createdAt;
    @SerializedName("editor")
    @Expose
    public UserObject editor;

    public String getVersion() {
        return String.format("版本 %s", version);
    }

    public String getTime() {
        return timeFormat.format(createdAt);
    }


    public String getEditorName() {
        return editor.name;
    }

    public String getMsg() {
        return msg;
    }


}
