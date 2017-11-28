package net.coding.program.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 16/6/24.
 */
public class Pager<T> extends PageInfo implements Serializable {

    private static final long serialVersionUID = 3963364344858443637L;

    @SerializedName("list")
    @Expose
    public List<T> list = new ArrayList<>();
}
