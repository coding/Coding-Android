package net.coding.program.common.model.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/9/14.
 */

public class UserTag implements Serializable {

    private static final long serialVersionUID = 3841582070918606949L;

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("name")
    @Expose
    public String name = "";
}
