package net.coding.program.network.model.code;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BranchMetrics implements Serializable {

    private static final long serialVersionUID = -7032853125318221927L;

    @SerializedName("base")
    @Expose
    public String base = "";
    @SerializedName("ahead")
    @Expose
    public int ahead;
    @SerializedName("behind")
    @Expose
    public int behind;
}
