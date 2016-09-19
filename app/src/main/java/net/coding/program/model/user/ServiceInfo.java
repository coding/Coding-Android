package net.coding.program.model.user;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/19.
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 7792590334229510318L;

    public int privateProject;
    public int totalmemory;
    public int pointleft;
    public int publicProject;
    public int balance;
    public int usedmemory;
    public int team;

    public ServiceInfo(JSONObject json) {
        privateProject = json.optInt("private");
        totalmemory = json.optInt("total_memory");
        pointleft = json.optInt("point_left");
        publicProject = json.optInt("public");
        balance = json.optInt("balance");
        usedmemory = json.optInt("used_memory");
        team = json.optInt("team");
    }
}
