package net.coding.program.model;

import net.coding.program.subject.service.ISubjectRecommendObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-21.
 */
public class Subject {

    public static class SubjectDescObject implements Serializable, ISubjectRecommendObject {

        public int id;
        public String name;
        public Integer speackers;
        public int watchers;
        public Integer count;
        public String image_url;
        public String description;
        public boolean watched;
        public long created_at;
        public HotTweetDescObject hot_tweet;
        public List<UserObject> user_list;

        public SubjectDescObject(JSONObject json) throws JSONException {
            created_at = json.optLong("created_at");
            id = json.optInt("id");
            watched = json.optBoolean("watched");
            speackers = json.optInt("speackers");
            if (speackers == 0)
                speackers = json.optInt("speakers");
            watchers = json.optInt("watchers");
            count = json.optInt("count");
            image_url = json.optString("image_url");
            description = json.optString("description");
            name = json.optString("name");
            if (json.has("hot_tweet"))
                hot_tweet = new HotTweetDescObject(json.optJSONObject("hot_tweet"));
            JSONArray arr = json.optJSONArray("user_list");
            if (arr != null && arr.length() > 0) {
                user_list = new ArrayList<UserObject>();
                for (int i = 0; i < arr.length(); i++) {
                    user_list.add(new UserObject(arr.optJSONObject(i)));
                }
            }
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getType() {
            return 1;
        }

    }

    public static class HotTweetDescObject implements Serializable {
        public int id;
        public int owner_id;
        public UserObject owner;
        public long created_at;
        public int likes;
        public int comments;
        public List<BaseComment> comment_list;
        public String device;
        public String location;
        public String coord;
        public String address;
        public String content;
        public String path;
        public int acitivity_id;
        public boolean liked;
        public List<UserObject> like_users;

        public HotTweetDescObject(JSONObject json) {
            id = json.optInt("id");
            owner_id = json.optInt("owner_id");
            owner = new UserObject(json.optJSONObject("owner"));
            created_at = json.optLong("created_at");
            likes = json.optInt("likes");
            comments = json.optInt("comments");
            device = json.optString("device");
            location = json.optString("location");
            device = json.optString("device");
            coord = json.optString("coord");
            device = json.optString("device");
            address = json.optString("address");
            content = json.optString("content");
            acitivity_id = json.optInt("acitivity_id");
            liked = json.optBoolean("liked");
            JSONArray arr = json.optJSONArray("like_users");
            if (arr != null && arr.length() > 0) {
                like_users = new ArrayList<UserObject>();
                for (int i = 0; i < arr.length(); i++) {
                    like_users.add(new UserObject(arr.optJSONObject(i)));
                }
            }
        }

    }

    public static class SubjectLastUsedObject implements Serializable, ISubjectRecommendObject {

        public String name;

        public SubjectLastUsedObject(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getType() {
            return 0;
        }
    }

}
