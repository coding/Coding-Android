package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/2/26.
 */
public class GitFileBlobObject implements Serializable {

    GitFileObject gitFileObject;
    HeadCommitObject headCommitObject;
    String ref;
    public boolean canEdit;

    public GitFileBlobObject(JSONObject json) {
        ref = json.optString("ref", "");
        gitFileObject = new GitFileObject(json.optJSONObject("file"));
        headCommitObject = new HeadCommitObject(json.optJSONObject("headCommit"));
        canEdit = json.optBoolean("can_edit", false);
    }

    public GitFileObject getGitFileObject() {
        return gitFileObject;
    }

    public String getRef() {
        return ref;
    }

    // 返回的 CommitId 用于提交代码
    public String getCommitId() {
        return headCommitObject.commitId;
    }

}
