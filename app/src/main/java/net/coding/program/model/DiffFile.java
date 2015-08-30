package net.coding.program.model;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.url.UrlCreate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenchao on 15/6/2.
 */
public class DiffFile implements Serializable {
    String commitId; // "4d2dade52151288a42b7534f9bc6ea6895bf221b",
    int insertions; // 3,
    int deletions; // 1
    ArrayList<DiffSingleFile> paths = new ArrayList<>();

    public DiffFile(JSONObject json) {
        commitId = json.optString("commitId");
        insertions = json.optInt("insertions");
        deletions = json.optInt("deletions");
        JSONArray jsonPaths = json.optJSONArray("paths");
        if (jsonPaths != null) {
            for (int i = 0; i < jsonPaths.length(); ++i) {
                paths.add(new DiffSingleFile(jsonPaths.optJSONObject(i)));
            }
        }
    }

    public int getInsertions() {
        return insertions;
    }

    public int getDeletions() {
        return deletions;
    }

    public int getFileCount() {
        return paths.size();
    }

    public ArrayList<DiffSingleFile> getFiles() {
        return paths;
    }

    public static class DiffSingleFile implements Serializable {
        String changeType; // "MODIFY","ADD", "DELETE"
        int insertions; // 3,
        int deletions; // 1,
        String name; // "README.md",
        String path; // "README.md",
        int size; // 0,
        int mode; // 33188,
        String objectId; // "f48b3649a6bc4887a13dc507886ed5cd5db06faa",
        String commitId; // "4d2dade52151288a42b7534f9bc6ea6895bf221b"

        public DiffSingleFile(JSONObject json) {
            changeType = json.optString("changeType");
            insertions = json.optInt("insertions");
            deletions = json.optInt("deletions");
            name = json.optString("name");
            path = json.optString("path");
            size = json.optInt("size");
            mode = json.optInt("mode");
            objectId = json.optString("objectId");
            commitId = json.optString("commitId");
        }

        public String getInsertions() {
            return "+" + insertions;
        }

        public String getDeletions() {
            return "-" + deletions;
        }

        public String getName() {
            int index = path.lastIndexOf("/");
            if (index != -1) {
                return path.substring(index + 1);
            }

            return path;
        }

        // encode 2 次，path 前不加 /
        public String getHttpFileDiffDetail(String projectPath) {
            String realPath = ProjectObject.translatePath(projectPath);
            return Global.HOST_API + realPath + "/git/commitDiffContent/" + commitId + "/" + UrlCreate.pathEncode2NoSplite(path);
        }

        // encode 2 次，path 前不加 /
        public String getHttpFileDiffComment(String projectPath) {
            String realPath = ProjectObject.translatePath(projectPath);
            return Global.HOST_API + realPath + "/git/commitDiffComment/" + commitId + "/" + UrlCreate.pathEncode2NoSplite(path);
        }

        // 都是文件名，只需要 encode 一次
        public String getHttpFileDiffDetail(String projectPath, int mergeIid, Merge merge) {
            return getHttpFileDiff(projectPath, mergeIid, merge, "commitDiffContent");

        }

        // 都是文件名，只需要 encode 一次
        public String getHttpFileDiffComment(String projectPath, int mergeIid, Merge merge) {
            return getHttpFileDiff(projectPath, mergeIid, merge, "commitDiffComment");
        }

        private String getHttpFileDiff(String projectPath, int mergeIid, Merge merge, String type) {
            String realPath = ProjectObject.translatePath(projectPath);
            String mergePath = "/git/merge/";
            if (merge != null && merge.isPull()) {
                mergePath = "/git/pull/";
            }

            return Global.HOST_API + realPath + mergePath + mergeIid + "/" +
                    type +
                    "?path="
                    + Global.encodeUtf8(path);
        }

        // encode 2 次，path 前要加上 /
        public String getHttpSourceFile(String projectPath, String refId) {
            String realPath = ProjectObject.translatePath(projectPath);
            return Global.HOST_API + realPath + "/git/blob/" + refId + UrlCreate.pathEncode2(path);
        }

        public int getIconId() {
            if (changeType.equals("MODIFY")) {
                return R.drawable.ic_mergefile_modify;
            } else if (changeType.equals("ADD")) {
                return R.drawable.ic_mergefile_add;
            } else { // DELETE
                return R.drawable.ic_mergefile_delete;
            }
        }
    }
}
