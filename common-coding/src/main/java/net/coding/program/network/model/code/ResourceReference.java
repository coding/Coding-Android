package net.coding.program.network.model.code;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.R;

import java.io.Serializable;

public class ResourceReference implements Serializable {

    private static final long serialVersionUID = 252949013114615996L;

    @SerializedName("code")
    @Expose
    public int code;
    @SerializedName("target_type")
    @Expose
    public String targetType = "";
    @SerializedName("target_id")
    @Expose
    public int targetId;
    @SerializedName("title")
    @Expose
    public String title = "";
    @SerializedName("link")
    @Expose
    public String link = "";
    @SerializedName("img")
    @Expose
    public String img = "";
    @SerializedName("status")
    @Expose
    public int status;

    public int getTypeIcon() {
        switch (targetType) {
            case "Release":
                return R.drawable.release_file;
            case "Task":
                return R.drawable.release_task;
            case "ProjectFile":
                return R.drawable.release_file;
            case "MergeRequestBean":
                return R.drawable.release_mr;
            case "Wiki":
                return R.drawable.release_file;
            default:
                return R.drawable.release_file;
        }
    }

}
