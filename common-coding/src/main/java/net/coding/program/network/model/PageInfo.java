package net.coding.program.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by chenchao on 16/6/24.
 * 新的 pagerinfo
 */
public class PageInfo implements Serializable {

    private static final long serialVersionUID = -3116088987613227969L;

    @SerializedName("page")
    @Expose
    public int page;
    @SerializedName("pageSize")
    @Expose
    public int pageSize;
    @SerializedName("totalPage")
    @Expose
    public int totalPage;
    @SerializedName("totalRow")
    @Expose
    public int totalRow;

}
