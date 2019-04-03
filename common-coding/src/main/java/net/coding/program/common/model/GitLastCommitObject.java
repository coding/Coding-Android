package net.coding.program.common.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


/**
 * Created by zjh on 2017/2/16.
 * Git最后改动model
 */

public class GitLastCommitObject implements Serializable {
    public String commitId;

    public GitLastCommitObject(JSONObject json) throws JSONException {
        commitId = json.optString("lastCommit");
    }
}
