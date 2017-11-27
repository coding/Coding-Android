package net.coding.program.common.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by zjh on 2017/2/16.
 * 文件上传前需要拿到lastCommit
 */

public class GitUploadPrepareObject implements Serializable {
    public String lastCommit;

    public GitUploadPrepareObject(JSONObject json) throws JSONException {
        lastCommit = json.optString("lastCommit");
    }

}
