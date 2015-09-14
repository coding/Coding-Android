package net.coding.program.model;

import net.coding.program.common.Global;

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
        public ArrayList<Comment> comment_list = new ArrayList<>();
        public int comments;
        public String content = "";
        public long created_at;
        public int id;
        public ArrayList<Like_user> like_users = new ArrayList<>();
        public boolean liked;
        public int likes;
        public UserObject owner = new UserObject();
        public int owner_id;
        public String path = "";
        public String device = "";
        public String location = "";
        public String coord = "";
        public String address = "";

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
            id = json.optInt("id");

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
            owner_id = json.optInt("owner_id");
            path = json.optString("path");
            device = json.optString("device");
            location = json.optString("location");
            coord = json.optString("coord");
            address = json.optString("address");
        }

        public String getLink() {
            return Global.HOST + "/u/" + owner.global_key + "/pp/" + id;
        }

        public String getMobileLink() {
            return Global.HOST_MOBILE + "/u/" + owner.global_key + "/pp/" + id;
        }

        @Override
        public boolean equals(Object o) {
            if( o instanceof MaopaoObject){
                if(this.id == ((MaopaoObject) o).id){
                    return true;
                }
            }
            return super.equals(o);
        }
    }

    public static class Comment extends BaseComment implements Serializable {

        public int tweet_id;

        public Comment(JSONObject json) throws JSONException {
            super(json);
            tweet_id = json.optInt("tweet_id");
        }

        public Comment(MaopaoObject maopao) {
            id = 0;
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
