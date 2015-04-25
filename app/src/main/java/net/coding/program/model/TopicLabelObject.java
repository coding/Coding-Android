package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Neutra on 2015/4/23.
 */
public class TopicLabelObject implements Serializable {
    public int id;
    public String name;
    public int count;

    public TopicLabelObject(JSONObject json) throws JSONException {
        id = json.optInt("id");
        name = json.optString("name", "");
        count = json.optInt("count", 0);
    }

    public TopicLabelObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public TopicLabelObject(TopicLabelObject src) {
        id = src.id;
        name = src.name;
        count = src.count;
    }
}
