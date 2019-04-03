package net.coding.program.common.model;

import net.coding.program.common.Global;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/5/29.
 */
public class Commit implements Serializable {

    Committer committer;
    private String fullMessage; // "update README.md",
    private String shortMessage; // "update README.md ",
    private String allMessage; // "",
    private String commitId; // "4d2dade52151288a42b7534f9bc6ea6895bf221b",
    private long commitTime; // 1432806003000,
    private int notesCount; // 0

    public Commit(JSONObject json) {
        fullMessage = json.optString("fullMessage");
        shortMessage = json.optString("shortMessage");
        allMessage = json.optString("allMessage");
        commitId = json.optString("commitId");
        commitTime = json.optLong("commitTime");
        notesCount = json.optInt("notesCount");
        committer = new Committer(json.optJSONObject("committer"));
    }

    public static String getHttpSendComment(String path) {
        String realPath = ProjectObject.translatePath(path);
        return Global.HOST_API + realPath + "/git/line_notes";
    }

    public static String getHttpDeleteComment(String path, int id) {
        String realPath = ProjectObject.translatePath(path);
        return Global.HOST_API + realPath + "/git/line_notes/" + id;
    }

    public String getHttpFiles(String path) {
        String realPath = ProjectObject.translatePath(path);
        return Global.HOST_API + realPath + "/git/commitDiffStat/" + commitId;
    }

    public String getHttpComments(String path) {
        String realPath = ProjectObject.translatePath(path);
        return Global.HOST_API + realPath + "/git/commit/" + commitId;
    }

    public String getCommitIdPrefix() {
        int prefixLength = 10;
        if (commitId.length() < prefixLength) {
            return commitId;
        }

        return commitId.substring(0, prefixLength);
    }

    public String getCommitId() {
        return commitId;
    }

    public String getTitle() {
        return fullMessage;
    }

    public String getName() {
        return committer.name;
    }

    public long getCommitTime() {
        return commitTime;
    }

    public String getIcon() {
        return committer.avatar;
    }

    public String getGlobalKey() {
        return committer.link.replace("/u/", "");
    }

}
