package net.coding.program.common.model.topic;

import net.coding.program.common.Global;
import net.coding.program.common.model.BaseComment;
import net.coding.program.common.model.DynamicObject;
import net.coding.program.common.model.UserObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenchao on 16/9/7.
 */
public class TopicComment extends BaseComment implements Serializable {

    private static final long serialVersionUID = 6723053788002028749L;

    public int topicid;
    public int parentid;
    public int childcount;
    public ArrayList<TopicCommentChild> childcomments = new ArrayList();
    public int type;
    public int isRecommended;
    public int recommendedBy;
    public UserObject recommendedUser;
    public int recommendedat;
    public int upvotecounts;
    public ArrayList<DynamicObject.Owner> upVoteUsers = new ArrayList<>();
    public int updatedat;
    public String anchor = "";

    public TopicComment(JSONObject json) {
        super(json);
        if (json == null) {
            return;
        }
        id = json.optInt("id");
        owner = new DynamicObject.Owner(json.optJSONObject("owner"));
        topicid = json.optInt("topic_id");
        parentid = json.optInt("parent_id");
        childcount = json.optInt("child_count");
        JSONArray array = json.optJSONArray("child_comments");
        if (array != null) {
            for (int i = 0, n = array.length(); i < n; i++) {
                TopicCommentChild item = new TopicCommentChild(array.optJSONObject(i));
                childcomments.add(item);
            }
        }


        type = json.optInt("type");
        content = json.optString("content", "");
        isRecommended = json.optInt("is_recommended");
        recommendedBy = json.optInt("recommended_by");
        recommendedUser = new UserObject(json.optJSONObject("recommended_user"));
        recommendedat = json.optInt("recommended_at");
        upvotecounts = json.optInt("up_vote_counts");

        JSONArray array1 = json.optJSONArray("up_vote_users");
        if (array1 != null) {
            for (int i = 0, n = array1.length(); i < n; i++) {
                DynamicObject.Owner item = new DynamicObject.Owner(array1.optJSONObject(i));
                upVoteUsers.add(item);
            }
        }

        updatedat = json.optInt("updated_at");
        anchor = json.optString("anchor", "");
    }

    public String getUrlAllComment(int projectId) {
        return String.format("%s/project/%s/topic/%s/comment/%s/comments?page=1&pageSize=99999",
                Global.HOST_API, projectId, topicid, id);
    }

    public boolean isRecommend() {
        return isRecommended == 1;
    }
}
