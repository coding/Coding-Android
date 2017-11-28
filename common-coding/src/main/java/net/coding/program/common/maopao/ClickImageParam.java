package net.coding.program.common.maopao;

import java.util.ArrayList;

/**
 * Created by chenchao on 2017/11/27.
 * 冒泡的图片点击事件
 */
public class ClickImageParam {
    public ArrayList<String> urls;
    public int pos;
    public boolean needEdit;

    public ClickImageParam(ArrayList<String> urlsParam, int posParam, boolean needEditParam) {
        urls = urlsParam;
        pos = posParam;
        needEdit = needEditParam;
    }

    public ClickImageParam(String url) {
        urls = new ArrayList<>();
        urls.add(url);
        pos = 0;
        needEdit = false;
    }
}
