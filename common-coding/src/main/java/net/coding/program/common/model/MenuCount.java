package net.coding.program.common.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vernon on 15/11/16.
 */
public class MenuCount implements Serializable {

    private int deploy;
    private int joined;
    private int stared;
    private int created;
    private int all;
    private int watched;


    public MenuCount(JSONObject jsonObject) throws JSONException {
        deploy = jsonObject.optInt("deploy");
        joined = jsonObject.optInt("joined");
        stared = jsonObject.optInt("stared");
        created = jsonObject.optInt("created");
        all = jsonObject.optInt("all");
        watched = jsonObject.optInt("watched");
    }

    public int getDeploy() {
        return deploy;
    }

    public void setDeploy(int deploy) {
        this.deploy = deploy;
    }

    public int getJoined() {
        return joined;
    }

    public void setJoined(int joined) {
        this.joined = joined;
    }

    public int getStared() {
        return stared;
    }

    public void setStared(int stared) {
        this.stared = stared;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public int getWatched() {
        return watched;
    }

    public void setWatched(int watched) {
        this.watched = watched;
    }
}
