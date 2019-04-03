package net.coding.program.network.model.file;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CodingFileView implements Serializable {

    private static final long serialVersionUID = 6149256798198120055L;

    @SerializedName("file")
    @Expose
    public CodingFile file;
    @SerializedName("activity_count")
    @Expose
    public int activity_count;
    @SerializedName("history_count")
    @Expose
    public int history_count;
    @SerializedName("share")
    @Expose
    public boolean share;
    @SerializedName("content")
    @Expose
    public String content = "";
}
