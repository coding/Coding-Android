package net.coding.program.network;

import net.coding.program.network.model.PageInfo;
import net.coding.program.network.model.Pager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 16/6/24.
 */
public class PagerData<T> {

    public int page = 0;
    public int totalPage = 1;
    public List<T> data = new ArrayList<>();

    public PagerData() {
    }

    public void addData(Pager<T> result) {
        if (result.page <= 1) {
            data.clear();
        }

        data.addAll(result.list);
        page = result.page;
        totalPage = result.totalPage;
    }

    public void addData(List<T> dataList, PageInfo pageInfo) {
        if (pageInfo.page <= 1) {
            data.clear();
        }

        data.addAll(dataList);
        page = pageInfo.page;
        totalPage = pageInfo.totalPage;
    }

    public void clear() {
        page = 0;
        totalPage = 1;
        data.clear();
    }

    public boolean isLoadAll() {
        return page >= totalPage;
    }

    public void setPageFirst() {
        page = 0;
        totalPage = 1;
    }

}
