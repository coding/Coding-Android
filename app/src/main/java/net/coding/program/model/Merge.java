package net.coding.program.model;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.Global;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/5/25.
 */
public class Merge implements Serializable {
    private int id;
    private String srcBranch = "";
    private String desBranch = "";
    private String title = "";
    private int iid;
    private String merge_status = "";
    private String path = "";
    private String src_owner_name = "";
    private String src_project_name = "";
    private long created_at;
    private UserObject author;
    private ActionAuthor action_author;
    private int action_at;
    private SourceDepot source_depot;
    private String merged_sha = "";
    private boolean srcExist;

    public Merge(JSONObject json) {
        id = json.optInt("id");
        srcBranch = json.optString("srcBranch");
        desBranch = json.optString("desBranch");
        title = json.optString("title");
        iid = json.optInt("iid");
        merge_status = json.optString("merge_status");
        path = json.optString("path", "").replace("/u/", "/user/").replace("/p/", "/project/");
        src_owner_name = json.optString("src_owner_name");
        src_project_name = json.optString("src_project_name");
        created_at = json.optLong("created_at");
        author = new UserObject(json.optJSONObject("author"));
        action_author = new ActionAuthor(json.optJSONObject("action_author"));
        action_at = json.optInt("action_at");
        source_depot = new SourceDepot(json.optJSONObject("source_depot"));
        merged_sha = json.optString("merged_sha");
        srcExist = json.optBoolean("srcExist");
    }

    public UserObject getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public int getIid() {
        return iid;
    }

    public long getCreatedAt() {
        return created_at;
    }

    static class ActionAuthor implements Serializable {

        public ActionAuthor(JSONObject json) {
            status = json.optInt("status");
            is_member = json.optInt("is_member");
            id = json.optInt("id");
            follows_count = json.optInt("follows_count");
            fans_count = json.optInt("fans_count");
            tweets_count = json.optInt("tweets_count");
            followed = json.optBoolean("followed");
            follow = json.optBoolean("follow");
        }

        private int status;
        private int is_member;
        private int id;
        private int follows_count;
        private int fans_count;
        private int tweets_count;
        private boolean followed;
        private boolean follow;
    }

    private String getHostPublicHead(String end) {
        return Global.HOST_API + path + end;
    }

    public String getHttpComments() {
        return getHostPublicHead("/comments");
    }


    public String getHttpCommits() {
        return getHostPublicHead("?diff=");
    }

    public PostRequest getHttpSendComment() {
        String gitHead = getHostPublicHead("");
        int index = gitHead.indexOf("/git/");
        String head = gitHead.substring(0, index);
        String url = head + "/git/line_notes";
        RequestParams params = new RequestParams();
        params.put("noteable_type", "PullRequestBean");
        params.put("noteable_id", id);

        return new PostRequest(url, params);
    }

    public static class PostRequest {
        public String url;
        public RequestParams params;

        public PostRequest(String url, RequestParams params) {
            this.url = url;
            this.params = params;
        }

        public void setContent(String input) {
            params.put("content", input);
        }
    }
}
