package net.coding.program.common.model.project;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/23.
 */

public class ProjectServiceInfo implements Serializable {

    private static final long serialVersionUID = 4677715412515174885L;

    public int maxmember;
    public int gitmemory;
    public int usedday;
    public int member;
    public int maxgitmemory;

    public ProjectServiceInfo(JSONObject json) {
        maxmember = json.optInt("max_member");
        gitmemory = json.optInt("git_memory");
        usedday = json.optInt("used_day");
        member = json.optInt("member");
        maxgitmemory = json.optInt("max_git_memory");
    }
}
