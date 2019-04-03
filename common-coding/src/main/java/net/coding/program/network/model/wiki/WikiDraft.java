package net.coding.program.network.model.wiki;

import net.coding.program.common.model.topic.TopicData;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/5/26.
 * wiki 草稿
 */
public class WikiDraft implements Serializable {

    private static final long serialVersionUID = -3651464885499545651L;

    public String content = "";
    public String title = "";
    public long updateAt; // 保存 wiki 的 updateAt
//    public int version;


    public WikiDraft(TopicData data) {
        content = data.content;
        title = data.title;
    }
}
