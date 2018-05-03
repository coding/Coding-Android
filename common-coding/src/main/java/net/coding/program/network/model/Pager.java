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

    public void add(Pager<T> result) {
        if (result.page <= 1) {
            list.clear();
        }

        list.addAll(result.list);
        page = result.page;
        totalPage = result.totalPage;
        totalRow = result.totalRow;
        pageSize = result.pageSize;
    }

    public void add(List<T> dataList, PageInfo pageInfo) {
        if (pageInfo.page <= 1) {
            list.clear();
        }

        list.addAll(dataList);
        page = pageInfo.page;
        totalPage = pageInfo.totalPage;
        totalRow = pageInfo.totalRow;
        pageSize = pageInfo.pageSize;
    }

    public void clear() {
        page = 0;
        totalPage = 1;
        list.clear();
    }

    public boolean isLoadAll() {
        return page >= totalPage;
    }

    public void setPageFirst() {
        page = 0;
        totalPage = 1;
    }
}
