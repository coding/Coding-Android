package net.coding.program.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/2/26.
 */
public class GitFileBlobObject implements Serializable {

    GitFileObject gitFileObject;
    HeadCommitObject headCommitObject;
    String ref;

    public GitFileBlobObject(JSONObject json) {
        ref = json.optString("ref", "");
        gitFileObject = new GitFileObject(json.optJSONObject("file"));
        headCommitObject = new HeadCommitObject(json.optJSONObject("headCommit"));
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

    static class HeadCommitObject implements Serializable {
        String commitId;

        public HeadCommitObject(JSONObject json) {
            commitId = json.optString("commitId", "");
        }
    }
}
