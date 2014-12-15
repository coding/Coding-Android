package net.coding.program.model;

import net.coding.program.Global;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by cc191954 on 14-8-21.
 */
public class Maopao {

    public static class MaopaoObject implements Serializable {

        public String activity_id = "";
        public ArrayList<Comment> comment_list = new ArrayList<Comment>();
        public int comments;
        public String content = "";
        public long created_at;
        public String id = "";
        public ArrayList<Like_user> like_users = new ArrayList<Like_user>();
        public boolean liked;
        public int likes;
        public UserObject owner = new UserObject();
        public String owner_id = "";
        public String path = "";
        public String device = "";

        public MaopaoObject(JSONObject json) throws JSONException {
            activity_id = json.optString("activity_id");

            if (json.has("comment_list")) {
                JSONArray jsonComments = json.optJSONArray("comment_list");
                for (int i = 0; i < jsonComments.length(); ++i) {
                    Comment comment = new Comment(jsonComments.getJSONObject(i));
                    comment_list.add(comment);
                }
            }

            comments = json.optInt("comments");
            content = json.optString("content");
            created_at = json.optLong("created_at");
            id = json.optString("id");

            if (json.has("like_users")) {
                JSONArray jsonLikeUsers = json.optJSONArray("like_users");
                for (int i = 0; i < jsonLikeUsers.length(); ++i) {
                    Like_user user = new Like_user(jsonLikeUsers.getJSONObject(i));
                    like_users.add(user);
                }
            }

            liked = json.optBoolean("liked");
            likes = json.optInt("likes");
            if (json.has("owner")) {
                owner = new UserObject(json.optJSONObject("owner"));
            }
            owner_id = json.optString("owner_id");
            path = json.optString("path");
            device = json.optString("device");
        }
    }

    public static class Comment extends BaseComment implements Serializable {

        public String tweet_id = ""; // 4676

        public Comment(JSONObject json) throws JSONException {
            super(json);
            if (json.has("tweet_id")) {
                tweet_id = json.optString("tweet_id");
            }
        }

        public Comment(MaopaoObject maopao) {
            id = "";
            owner = new DynamicObject.Owner(maopao.owner);
            owner_id = maopao.owner_id;
            tweet_id = maopao.id;
        }

        public String toString() {
            return tweet_id + " ," + owner_id + " ," + id;
        }
    }

    public static class Like_user implements Serializable {
        public String avatar = "";
        public String global_key = "";
        public String name = "";
        public String path = "";

        public Like_user(JSONObject json) throws JSONException {
            if (json.has("avatar")) {
                avatar = Global.replaceAvatar(json);
            }

            global_key = json.optString("global_key");
            name = json.optString("name");
            path = json.optString("path");
        }

        public Like_user(UserObject user) {
            avatar = user.avatar;
            global_key = user.global_key;
            name = user.name;
            path = user.path;
        }
    }

}
