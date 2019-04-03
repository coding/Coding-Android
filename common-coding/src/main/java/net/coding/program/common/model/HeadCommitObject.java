package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 2018/3/6.
 */
public class HeadCommitObject implements Serializable {

    private static final long serialVersionUID = 5619679447106969383L;

    public String commitId = "";

    public HeadCommitObject(JSONObject json) {
        if (json == null) return;

        commitId = json.optString("commitId", "");
    }
}
