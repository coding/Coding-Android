package net.coding.program.common.model.topic;

import net.coding.program.common.model.BaseComment;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/9/7.
 */
public class TopicCommentChild extends BaseComment implements Serializable {

    private static final long serialVersionUID = -3465685236748756133L;

    public int topicid;
    public int parentid;
    public int childcount;
    public int type;
    public int isrecommended;
    public int upvotecounts;
    public int updatedat;
    public String anchor = "";

    public TopicCommentChild(JSONObject json) {
        super(json);
        topicid = json.optInt("topic_id");
        parentid = json.optInt("parent_id");
        childcount = json.optInt("child_count");
        type = json.optInt("type");
        isrecommended = json.optInt("is_recommended");
        upvotecounts = json.optInt("up_vote_counts");
        updatedat = json.optInt("updated_at");
        anchor = json.optString("anchor", "");
    }

}
