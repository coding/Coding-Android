package net.coding.program.model;

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
        commitId = json.optString("");
        insertions = json.optInt("");
        deletions = json.optInt("");
        JSONArray jsonPaths = json.optJSONArray("paths");
        if (jsonPaths != null) {
            for (int i = 0; i < jsonPaths.length(); ++i) {
                paths.add(new DiffSingleFile(jsonPaths.optJSONObject(i)));
            }
        }
    }

    public ArrayList<DiffSingleFile> getFiles() {
        return paths;
    }

    public static class DiffSingleFile implements Serializable {
        String changeType; // "MODIFY",
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
    }
}
