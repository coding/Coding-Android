package net.coding.program.param;

import net.coding.program.model.TopicLabelObject;
import net.coding.program.model.TopicObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenchao on 2017/4/13.
 * markdown 数据
 */
public class TopicData implements Serializable {
    public ArrayList<TopicLabelObject> labels = new ArrayList<>();
    public String title = "";
    public String content = "";

    public TopicData(TopicObject topicObject) {
        this.title = topicObject.title;
        this.content = topicObject.content;
        this.labels = topicObject.labels;
    }

    public TopicData(String title, String content, ArrayList<TopicLabelObject> labels) {
        this.title = title;
        this.content = content;
        this.labels = labels;
    }

    public TopicData() {
    }
}
