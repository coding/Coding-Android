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
        public boolean rewarded;
        public int rewards;
        public ArrayList<Like_user> reward_users = new ArrayList<>();

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
                    user.setType(Like_user.Type.Like);
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

            if (json.has("reward_users")) {
                JSONArray jsonUsers = json.optJSONArray("reward_users");
                for (int i = 0; i < jsonUsers.length(); ++i) {
                    Like_user user = new Like_user(jsonUsers.getJSONObject(i));
                    user.setType(Like_user.Type.Reward);
                    reward_users.add(user);
                }
            }

            rewarded = json.optBoolean("rewarded");
            rewards = json.optInt("rewards");
        }

        public String getLink() {
            return Global.HOST + "/u/" + owner.global_key + "/pp/" + id;
        }

        public String getMobileLink() {
            return Global.HOST_MOBILE + "/u/" + owner.global_key + "/pp/" + id;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MaopaoObject) {
                if (this.id == ((MaopaoObject) o).id) {
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

    public static class Like_user extends DynamicObject.User implements Serializable {
        public Type type = Type.Like; // 0表示点赞，1表示打赏

        public enum Type {
            Like, Reward
        }

        public Like_user(JSONObject json) throws JSONException {
            super(json);
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Like_user(UserObject user) {
            avatar = user.avatar;
            global_key = user.global_key;
            name = user.name;
            path = user.path;
        }
    }

    public static String getHttpProjectMaopao(int projectId, int maopaoId) {
//        https://coding.net/api/project/205646/tweet/2417
        return String.format("%s/project/%d/tweet/%d", Global.HOST_API, projectId, maopaoId);
    }
}
