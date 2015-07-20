package net.coding.program.model;

import net.coding.program.common.Global;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by david on 15-7-21.
 */
public class Subject {

    public static class SubjectDescObject implements Serializable {

        public int id;
        public String name;
        public int speackers;
        public int watchers;
        public int count;
        public String image_url;
        public String description;
        public boolean watched;
        public long created_at;

        public SubjectDescObject(JSONObject json) throws JSONException {
            created_at = json.optLong("created_at");
            id = json.optInt("id");
            watched = json.optBoolean("watched");
            speackers = json.optInt("speackers");
            watchers = json.optInt("watchers");
            count = json.optInt("count");
            image_url = json.optString("image_url");
            description = json.optString("description");
            name = json.optString("name");
        }
    }


}
