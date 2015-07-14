package net.coding.program.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by yangzhen on 2014/10/25.
 */
public class AttachmentFolderObject implements Serializable {
    public long created_at;
    public String file_id = "";
    public String name = "";
    public String owner_id = "";
    public String parent_id = "";
    public int type;
    public long updated_at;
    public int count;
    public boolean isSelected = false;

    public ArrayList<AttachmentFolderObject> sub_folders = new ArrayList<>();
    public AttachmentFolderObject parent;

    public AttachmentFolderObject() {
        this.name = "默认文件夹";
        this.file_id = "0";
    }

    public AttachmentFolderObject(JSONObject json) throws JSONException {
        created_at = json.optLong("created_at");
        file_id = json.optString("file_id");
        name = json.optString("name");
        owner_id = json.optString("owner_id");
        parent_id = json.optString("parent_id");
        type = json.optInt("type");
        updated_at = json.optLong("updated_at");

        JSONArray subFolders = json.optJSONArray("sub_folders");
        for (int i = 0; i < subFolders.length(); i++) {
            JSONObject subFolderObject = subFolders.getJSONObject(i);
            AttachmentFolderObject subFolder = new AttachmentFolderObject(subFolderObject);
            subFolder.parent = this;
            sub_folders.add(subFolder);
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(Integer count) {
        if (count != null) {
            this.count = count;
        }
    }

    /**
     * @return 当前目录文件总数，包括子目录中的文件数
     */
    public int getTotelCount() {
        int subFolderCount = 0;
        for (AttachmentFolderObject subFolder : this.sub_folders) {
            subFolderCount += subFolder.getTotelCount();
        }
        return this.count + subFolderCount;
        //return this.count;
    }

    public String getNameCount() {
        return String.format("%s (%s)", this.name, getTotelCount());
    }

    /**
     * @return 文件夹是否能被删除
     */
    public boolean isDeleteable() {
        return (!file_id.equals("0") && count == 0);
    }
}
