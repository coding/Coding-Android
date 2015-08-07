package net.coding.program.model;

import net.coding.program.common.Global;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/7/30.
 */
public class BannerObject implements Serializable {

    /**
     * id : 14
     * title : 1
     * updated_at : 1438137122000
     * status : 1
     * link : https://coding.net/pp
     * name : app-1
     * image : https://dn-coding-net-production-static.qbox.me/b79d79bb-78ce-41dd-bb79-6c44693a40e1.png
     * created_at : 1438137122000
     * code : app
     */
    private int id;
    private String title = "";
    private long updated_at;
    private int status;
    private String link = "";
    private String name = "";
    private String image = "";
    private long created_at;
    private String code = "";

    public BannerObject(JSONObject json) {
        id = json.optInt("id");
        title = json.optString("title", "");
        updated_at = json.optLong("updated_at");
        status = json.optInt("status");
        link = json.optString("link", "");
        name = json.optString("name", "");
        image = json.optString("image", "");
        created_at = json.optLong("created_at");
        code = json.optString("code", "");
    }

    public BannerObject() {
    }

    public static String getHttpBanners() {
        return Global.HOST_API + "/banner/type/app";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
