package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by libo on 2015/11/20.
 */
public class MallBannerObject implements Serializable {

    /**
     * "id": 2,
     * "title": "Coding 抱枕 ￥99/<small>个</small>",
     * "description": "忙碌一天，脑里已对温热的晚餐产生无限遐想。<br>,
     * "image": "https://dn-coding-net-production-static.qbox.me/8140ca62-d614-4ab6-9515-37b69a9cd9c7.png",
     * "createdAt": 1435314602000,
     * "updatedAt": 1444186433000
     */
    private int id;

    private String title = "";

    private String description = "";

    private String image = "";

    private long createdAt;

    private long updatedAt;

    public MallBannerObject() {
    }

    public MallBannerObject(JSONObject json) {
        id = json.optInt("id");
        title = json.optString("title");
        description = json.optString("description");
        image = json.optString("image");
        createdAt = json.optLong("createdAt");
        updatedAt = json.optLong("updatedAt");
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
