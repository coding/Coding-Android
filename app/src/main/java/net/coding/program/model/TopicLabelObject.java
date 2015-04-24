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

    /*
    id: 579191,
    name: "Bug",
    color: "#d95c5c",
    owner_id: 289,
    count: 0,
    type: 1
    */
    public TopicLabelObject(JSONObject json) throws JSONException {
        id = json.optInt("id");
        name = json.optString("name", "");
        count = json.optInt("count", 0);
    }
}
