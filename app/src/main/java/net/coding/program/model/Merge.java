package net.coding.program.model;

import android.text.Spannable;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/5/25.
 * Merge对象，由json生成
 */
public class Merge implements Serializable {
    private static final String STYLE_ACCEPT = "ACCEPTED";
    private static final String STYLE_REFUSE = "REFUSED";
    private static final String STYLE_CANNEL = "CANCEL";
    private static final String STYLE_CANMERGE = "CANMERGE";
    private static final String STYLE_CANNOTMERGE = "CANNOTMERGE";
    public static final String[] STYLES = new String[]{
            STYLE_ACCEPT,
            STYLE_REFUSE,
            STYLE_CANMERGE,
            STYLE_CANNOTMERGE,
            STYLE_CANNEL,
    };

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
    private long action_at;
    private SourceDepot source_depot;
    private String merged_sha = "";
    private String content = "";
    private boolean srcExist;
    private String body_plan = "";
    private int granted = 0;

    public Merge(JSONObject json) {
        id = json.optInt("id");
        srcBranch = json.optString("srcBranch");
        desBranch = json.optString("desBranch");
        title = json.optString("title");
        iid = json.optInt("iid");
        merge_status = json.optString("merge_status");
        path = ProjectObject.translatePath(json.optString("path", ""));
        src_owner_name = json.optString("src_owner_name");
        src_project_name = json.optString("src_project_name");
        created_at = json.optLong("created_at");
        author = new UserObject(json.optJSONObject("author"));
        action_author = new ActionAuthor(json.optJSONObject("action_author"));
        action_at = json.optLong("action_at");
        if (json.has("source_depot")) {
            source_depot = new SourceDepot(json.optJSONObject("source_depot"));
        }
        merged_sha = json.optString("merged_sha");
        srcExist = json.optBoolean("srcExist");
        content = json.optString("content", "");
        body_plan = json.optString("body_plan", "");
        granted = json.optInt("granted", 0);
    }

    public void setAuthor(UserObject author) {
        this.author = author;
    }

    public void setAction_at(long action_at) {
        this.action_at = action_at;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSrcBranch(String srcBranch) {
        this.srcBranch = srcBranch;
    }

    public String getDesBranch() {
        return desBranch;
    }

    public void setDesBranch(String desBranch) {
        this.desBranch = desBranch;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public String getMerge_status() {
        return merge_status;
    }

    public void setMerge_status(String merge_status) {
        this.merge_status = merge_status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSrc_owner_name() {
        return src_owner_name;
    }

    public void setSrc_owner_name(String src_owner_name) {
        this.src_owner_name = src_owner_name;
    }

    public String getSrc_project_name() {
        return src_project_name;
    }

    public void setSrc_project_name(String src_project_name) {
        this.src_project_name = src_project_name;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public ActionAuthor getAction_author() {
        return action_author;
    }

    public void setAction_author(ActionAuthor action_author) {
        this.action_author = action_author;
    }

    public SourceDepot getSource_depot() {
        return source_depot;
    }

    public void setSource_depot(SourceDepot source_depot) {
        this.source_depot = source_depot;
    }

    public String getContent() {
        if (!content.isEmpty()) {
            return content;
        }

        return body_plan;
    }

    public boolean authorIsMe() {
        return author.isMe();
    }

    public UserObject getAuthor() {
        return author;
    }


    public boolean isStyleCanMerge() {
        return merge_status.equals(STYLE_CANMERGE);
    }

    public boolean isStyleCannotMerge() {
        return merge_status.equals(STYLE_CANNOTMERGE);
    }

    public String getTitle() {
        return title;
    }

    public int getIid() {
        return iid;
    }

    public int getId() {
        return id;
    }

    public ActionAuthor getActionAuthor() {
        return action_author;
    }

    public String getSrcBranch() {
        if (source_depot == null) {
            return srcBranch;
        } else {
            return src_owner_name + ":" + srcBranch;
        }
    }

    public Merge() {
    }

    public boolean isPull() {
        return source_depot != null;
    }

    public String getDescBranch() {
        if (source_depot == null) {
            return desBranch;
        } else {
            return desBranch;
        }

    }

    public String getMergeStatus() {
        return merge_status;
    }

    public boolean isMergeAccept() {
        return merge_status.equals(STYLE_ACCEPT);
    }

    public boolean isMergeTreate() {
        return merge_status.equals(STYLE_ACCEPT) ||
                merge_status.equals(STYLE_REFUSE);
    }

    public boolean isCanceled() {
        return merge_status.equals(STYLE_CANNEL);
    }

    public long getCreatedAt() {
        return created_at;
    }

    public long getAction_at() {
        return action_at;
    }

    private String getHostPublicHead(String end) {
        return Global.HOST_API + path + end;
    }

    public String getProjectPath() {
        int index = path.indexOf("/git/");
        if (index != -1) {
            return path.substring(0, index);
        }

        return path;
    }

    public int getGranted() {
        return granted;
    }

    public void setGranted(int granted) {
        this.granted = granted;
    }

    public String getHttpComments() {
        return getHostPublicHead("/comments");
    }

    public String getHttpActivities() {
        return getHostPublicHead("/activities");
    }


    public String getHttpDetail() {
        return getHostPublicHead("/?");  // 以前是 /base 但返回的内容缺少必要的字段。
    }

    public String getHttpReviewers() {
        return getHostPublicHead("/reviewers");
    }

    public String getHttpReviewGood() {
        return getHostPublicHead("/review_good");
    }

    public String getHttpAddReviewer() {
        return getHostPublicHead("/add_reviewer");
    }

    public String getHttpDelReviewer() {
        return getHostPublicHead("/del_reviewer");
    }

    public String getHttpCommits() {
        return getHostPublicHead("/commits");
    }

    public RequestData getHttpMerge(String message, boolean delSource) {
        String url = getHostPublicHead("/merge");

        RequestParams params = new RequestParams();
        params.put("del_source_branch", delSource);
        params.put("message", message);

        return new RequestData(url, params);

//        :
//        :Accept Merge Request #12 : (master1 -> master)
//        : title
//        : @陈超
//        : @陈超
//        : https://coding.net/u/1984/p/TestPrivate/git/merge/12;
    }

    public String getHttpFiles() {
        return getHostPublicHead("/commitDiffStat");
    }

    public String getHttpRefuse() {
        return getHostPublicHead("/refuse");
    }

    public String getHttpCancel() {
        return getHostPublicHead("/cancel");
    }

    public String getHttpGrant() {
        return getHostPublicHead("/grant");
    }

    public RequestData getHttpSendComment() {
        String url = getHttpHostComment();
        RequestParams params = new RequestParams();
        String mergeType = isPull() ? "PullRequestBean" : "MergeRequestBean";
        params.put("noteable_type", mergeType);
        params.put("noteable_id", id);

        return new RequestData(url, params);
    }

    public String getHttpDeleteComment(int commentId) {
        String url = getHttpHostComment();
        return url + "/" + commentId;
    }

    private String getHttpHostComment() {
        String gitHead = getHostPublicHead("");
        int index = gitHead.indexOf("/git/");
        String head = gitHead.substring(0, index);
        return head + "/git/line_notes";
    }

    public Spannable getTitleSpannable() {
        return Global.changeHyperlinkColor(title);
    }

    public String getTitleIId() {
        return "# " + iid;
    }

    public String getMergeAtMemberUrl() {
        String url = Global.HOST_API + getProjectPath() + "/relationships/context?context_type=%s&item_id=%d";
        String type = isPull() ? "pull_request_comment" : "merge_request_comment";
        return String.format(url, type, iid);
    }

    public String generalMergeMessage() {
        String template = "Accept %s #%d : (%s -> %s)";
        return String.format(template, ProjectObject.getTitle(isPull()), iid, srcBranch, desBranch);
    }

    public static class ActionAuthor extends UserObject implements Serializable {

        private int status;
        private int is_member;
        private int id;
        private int follows_count;
        private int fans_count;
        private int tweets_count;
        private boolean followed;
        private boolean follow;

        public ActionAuthor() {
        }

        public ActionAuthor(JSONObject json) {
            super(json);
            status = json.optInt("status");
            is_member = json.optInt("is_member");
            id = json.optInt("id");
            follows_count = json.optInt("follows_count");
            fans_count = json.optInt("fans_count");
            tweets_count = json.optInt("tweets_count");
            followed = json.optBoolean("followed");
            follow = json.optBoolean("follow");
        }
    }

    public static class Reviewer implements Serializable {
        public int value;
        public String volunteer; //invitee
        public UserObject user = new UserObject();
        public Reviewer(JSONObject json) throws JSONException {
            value = json.optInt("value");
            volunteer = json.optString("volunteer");
            if (json.has("reviewer")) {
                user = new UserObject(json.optJSONObject("reviewer"));
            }
        }

        public Reviewer(UserObject user) {
            this.user = user;
            volunteer = "invitee";
        }
    }

}
