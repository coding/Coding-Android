package net.coding.program.common.model;

import net.coding.program.network.model.file.CodingFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by yangzhen on 2014/10/25.
 */
public class AttachmentFolderObject implements Serializable {

    private static final long serialVersionUID = -2909859091872550261L;

    public static final String SHARE_FOLDER_ID = "-1";
    public static final String DEFAULT_FOLDER_ID = "0";
    public static final String ROOT_FOLDER_ID = "-2";

    public long created_at;//创建时间
    public String file_id = "";//文件ID
    public String name = "";//文件名称
    public String owner_id = "";//创建者ID
    public String parent_id = "";//文件夹ID
    public int type;//文件类型
    public long updated_at;//更新时间
    public int count;//文件数量
    public boolean isSelected = false;
    public ArrayList<AttachmentFolderObject> sub_folders = new ArrayList<>();
    public AttachmentFolderObject parent;

    public AttachmentFolderObject() {
        this.name = "默认文件夹";
        this.file_id = "0";
    }

    public static AttachmentFolderObject create(String type) {
        AttachmentFolderObject folder = new AttachmentFolderObject();
        if (ROOT_FOLDER_ID.equals(type)) {
            folder.file_id = type;
            folder.name = "文件";
        }

        return folder;
    }

    public boolean isRoot() {
        return ROOT_FOLDER_ID.equals(file_id);
    }

    public boolean isSharded() {
        return SHARE_FOLDER_ID.equals(file_id);
    }

    public boolean isDefault() {
        return DEFAULT_FOLDER_ID.equals(file_id);
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

    public AttachmentFolderObject(CodingFile file) {
        created_at = file.createdAt;
        file_id = String.valueOf(file.id);
        name = file.name;
        owner_id = String.valueOf(file.ownerId);
        parent_id = String.valueOf(file.parentId);
        type = file.type;
        updated_at = file.updatedAt;
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
