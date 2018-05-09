package net.coding.program.network.model.code;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.Global;

import java.io.Serializable;

public class Attachment implements Serializable {

    private static final long serialVersionUID = 5487083579902598676L;

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("size")
    @Expose
    public long size;

    public String getSizeString() {
        return Global.HumanReadableFilesize(size);
    }
}
