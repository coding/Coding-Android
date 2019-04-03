package net.coding.program.common.model;

import android.text.Spannable;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Merge implements Serializable {

    private int id;
    private String srcBranch = "";
    private String desBranch = "";
    private String title = "";
    private int iid;
    public Status merge_status = Status.CANCEL;
    private String path = "";
    private String src_owner_name = "";
    private String src_project_name = "";
    private long created_at;
    private UserObject author;
    private UserObject action_author;
    private long action_at;
    private Depot source_depot;
    private String merged_sha = "";
    private String content = "";
    private boolean srcExist;
    private String body_plan = "";
    private String body;
    private int granted = 0;
    private int commentCount;

    public Merge(JSONObject json) {
        id = json.optInt("id");
        if (json.has("srcBranch")) {
            srcBranch = json.optString("srcBranch");
        }
        if (json.has("source_branch")) {
            srcBranch = json.optString("source_branch");
        }

        if (json.has("desBranch")) {
            desBranch = json.optString("desBranch");
        }
        if (json.has("target_branch")) {
            desBranch = json.optString("target_branch");
        }

        title = json.optString("title");
        iid = json.optInt("iid");
        merge_status = Status.nameToEnum(json.optString("merge_status"));
        path = ProjectObject.translatePath(json.optString("path", ""));
        src_owner_name = json.optString("src_owner_name");
        src_project_name = json.optString("src_project_name");
        created_at = json.optLong("created_at");
        author = new UserObject(json.optJSONObject("author"));
        if (json.has("action_author")) {
            action_author = new UserObject(json.optJSONObject("action_author"));
        }
        action_at = json.optLong("action_at");
        if (json.has("source_depot")) {
            source_depot = new Depot(json.optJSONObject("source_depot"));
        }
        merged_sha = json.optString("merged_sha");
        srcExist = json.optBoolean("srcExist");
        content = json.optString("content", "");
        body_plan = json.optString("body_plan", "");
        body = json.optString("body", "");
        granted = json.optInt("granted", 0);
        commentCount = json.optInt("comment_count", 0);
    }

    public Merge() {
    }

    public String getBottomName() {
        return getTitleIId() + "  " + getAuthor().name;
    }

    public String getDesBranch() {
        return desBranch;
    }

    public void setDesBranch(String desBranch) {
        this.desBranch = desBranch;
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

    public Depot getSource_depot() {
        return source_depot;
    }


    public String getContent() {
        if (!content.isEmpty()) {
            return content;
        }

        return body_plan;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean authorIsMe() {
        return author.isMe();
    }

    public UserObject getAuthor() {
        return author;
    }

    public void setAuthor(UserObject author) {
        this.author = author;
    }

    public boolean isStyleCanMerge() {
        return merge_status == Status.CANMERGE;
    }

    public boolean isStyleCannotMerge() {
        return merge_status == Status.CANNOTMERGE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserObject getActionAuthor() {
        return action_author;
    }

    public String getSrcBranch() {
        if (source_depot == null) {
            return srcBranch;
        } else {
            return src_owner_name + ":" + srcBranch;
        }
    }

    public void setSrcBranch(String srcBranch) {
        this.srcBranch = srcBranch;
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

    public Status getMergeStatus() {
        return merge_status;
    }

    public boolean isMergeAccept() {
        return merge_status == Status.ACCEPTED;
    }

    public boolean isMergeRefuse() {
        return merge_status == Status.REFUSED;
    }

    //已处理
    public boolean isMergeTreate() {
        return merge_status == Status.ACCEPTED ||
                merge_status == Status.REFUSED;
    }

    public boolean isCanceled() {
        return merge_status == Status.CANCEL;
    }

    public long getCreatedAt() {
        return created_at;
    }

    public long getAction_at() {
        return action_at;
    }

    public void setAction_at(long action_at) {
        this.action_at = action_at;
    }

    public String getBody() {
        if (!body.isEmpty()) {
            return body;
        }

        return body_plan;
    }

    public int getCommentCount() {
        return commentCount;
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
        return GlobalCommon.changeHyperlinkColor(title);
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

    public enum Status {
        ACCEPTED(0xFFEB7A19, "已合并"),
        REFUSED(0xFFE84D60, "已拒绝"),
        CANCEL(0xFF76808E, "已取消"),
        CANMERGE(CodingColor.fontGreen, "可合并"),
        CANNOTMERGE(0xFFB17EDD, "不可自动合并");

        public int color;
        public String alics;

        Status(int color, String alics) {
            this.color = color;
            this.alics = alics;
        }

        public static Status nameToEnum(String name) {
            for (Status item : Status.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
            return CANCEL;
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


    public String getHttpComments() {
        return getHostPublicHead("/comments");
    }

    public String getCreateTime() {
        final SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(created_at);
    }

    public String getMergeStatusTxt() {
        return merge_status.alics;
    }

    public int getMergeStatusColor() {
        return merge_status.color;
    }


}
