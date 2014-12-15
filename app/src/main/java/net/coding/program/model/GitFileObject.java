package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yangzhen on 2014/11/18.
 */
public class GitFileObject implements Serializable {

    public String data = "";
    public String lang = "";
    public long lastCommitDate;
    public String lastCommitId = "";
    public String lastCommitMessage = "";
    public GitCommitterObject lastCommitter;
    public String mode = "";
    public String path = "";
    public String name = "";
    public String preview = "";
    public boolean previewed = false;
    public long size;


    public GitFileObject(JSONObject json) throws JSONException {
        data = json.optString("data");
        lang = json.optString("lang");
        lastCommitDate = json.optLong("lastCommitDate");
        lastCommitId = json.optString("lastCommitId");
        lastCommitMessage = json.optString("lastCommitMessage");
        lastCommitter = new GitCommitterObject(json.optJSONObject("lastCommitter"));

        mode = json.optString("mode");
        path = json.optString("path");
        name = json.optString("name");
        preview = json.optString("preview");
        previewed = json.optBoolean("previewed");
        size = json.optInt("size");
    }
}
