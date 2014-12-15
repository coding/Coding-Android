package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yangzhen on 2014/11/18.
 */
public class GitFileInfoObject implements Serializable {
    private final static String MODE_TREE = "tree";
    private final static String MODE_FILE = "file";
    public long lastCommitDate;
    public String lastCommitId;
    public String lastCommitMessage;
    public GitCommitterObject lastCommitter;

    public String mode = "";
    public String path = "";
    public String name = "";

    public GitFileInfoObject(JSONObject json) throws JSONException {
        lastCommitDate = json.optLong("lastCommitDate");
        lastCommitId = json.optString("lastCommitId");
        lastCommitMessage = json.optString("lastCommitMessage");
        lastCommitter = new GitCommitterObject(json.optJSONObject("lastCommitter"));
        mode = json.optString("mode");
        path = json.optString("path");
        name = json.optString("name");
    }

    /**
     * @return 是否目录
     */
    public boolean isTree() {
        return mode.equals(MODE_TREE);
    }
}
