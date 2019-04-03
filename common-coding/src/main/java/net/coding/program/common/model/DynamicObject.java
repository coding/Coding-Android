package net.coding.program.common.model;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.param.MessageParse;
import net.coding.program.network.constant.VIP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by cc191954 on 14-8-9.
 * 动态的各种类型都在这里
 */
public class DynamicObject {

    static final String BLACK_HTML = "<font color='#425063'>%s</font>";
    static final int BLACK_COLOR = CodingColor.font2;
    static final int BLACK_COLOR_9 = CodingColor.font3;

    private static String createLink(String name, String link) {
        name = name.replaceAll("<a (?:.*?)>(.*?)</a>", "$1");
        final String TEMPLATE_LINK = "<a href=\"%s\">%s</a>";
        return String.format(TEMPLATE_LINK, link, name);
    }

    static String black(String s) {
        return String.format(BLACK_HTML, s);
    }

    public static class DynamicBaseObject implements Serializable {
        public String action = "";
        public String action_msg = "";
        public long created_at;
        public int id;
        public String target_type = "";

        public User user = new User();

        public DynamicBaseObject(JSONObject json) throws JSONException {
            action = json.optString("action");
            action_msg = json.optString("action_msg");

            try {
                created_at = json.optLong("created_at");
            } catch (Exception e) {
                created_at = Calendar.getInstance().getTimeInMillis();
            }

            id = json.optInt("id");
            target_type = json.optString("target_type");

            if (json.has("user")) {
                user = new User(json.optJSONObject("user"));
            } else if (json.has("author")) {
                user = new User(json.optJSONObject("author"));
            }
        }

        public Spanned title() {
            final String format = "%s %s";
            String title = String.format(format, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        public Spanned content(MyImageGetter imageGetter) {
            return new SpannableString(action_msg);
        }

        public String jump() {
            return "";
        }

        protected String makeJump(String url) {
            return Global.HOST + url;
        }
    }

    public static class BranchMember extends DynamicBaseObject implements Serializable {

        private static final long serialVersionUID = -4826429913816541264L;

        Owner targetUser = new Owner();
        String refPath;
        String refName;

        public BranchMember(JSONObject json) throws JSONException {
            super(json);
            if (json.has("target_user")) {
                targetUser = new Owner(json.optJSONObject("target_user"));
            }
            refPath = json.optString("ref_path", "");
            refName = json.optString("ref_name", "");
        }

        @Override
        public Spanned title() {
            if (action.equals("deny_push") || action.equals("allow_push")) {
                final String format = "%s %s %s 直接 Push 保护分支";
                String title = String.format(format, user.getHtml(), action_msg, targetUser.getHtml());
                return GlobalCommon.changeHyperlinkColor(title);

            } else if (action.equals("remove") || action.equals("add")) {
                final String format = "%s %s 分支管理员";
                String title = String.format(format, user.getHtml(), action_msg);
                return GlobalCommon.changeHyperlinkColor(title);
            }

            return super.title();
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            if (action.equals("deny_push") || action.equals("allow_push")) {
//                final String format = "%s %s %s 直接 Push 保护分支";
//                String title = String.format(format, user.getHtml(), action_msg, targetUser.getHtml());
//                return GlobalCommon.changeHyperlinkColor(title);
                return new SpannableString(refName);
            } else if (action.equals("remove") || action.equals("add")) {
                return GlobalCommon.changeHyperlinkColor(targetUser.getHtml(), BLACK_COLOR, imageGetter);
            }
            return super.content(imageGetter);
        }

        @Override
        public String jump() {
            if (action.equals("deny_push") || action.equals("allow_push")) {
                return makeJump(refPath);
            } else if (action.equals("remove") || action.equals("add")) {
                return makeJump(targetUser.path);
            }

            return super.jump();
        }
    }

    public static class ProtectedBranch extends DynamicBaseObject implements Serializable {

        String refName = "";
        String refPath = "";

        private static final long serialVersionUID = -8930401317426589369L;

        public ProtectedBranch(JSONObject json) throws JSONException {
            super(json);
            refPath = json.optString("ref_path", "");
            refName = json.optString("ref_name", "");
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return new SpannableString(refName);
        }

        @Override
        public String jump() {
            return Global.HOST + refPath;
        }
    }

    public static class Release extends DynamicBaseObject implements Serializable {

        private static final long serialVersionUID = 7557962756095745154L;

        int release_iid;
        String release_title;
        String release_path;
        String release_tag_name;

        public Release(JSONObject json) throws JSONException {
            super(json);
            release_iid = json.optInt("release_iid", 0);
            release_title = json.optString("release_title", "");
            release_path = json.optString("release_path", "");
            release_tag_name = json.optString("release_tag_name", "");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s版本";
            String title = String.format(format, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public String jump() {
            if (action.equals("delete")) {
                return "";
            }

            return Global.HOST + release_path;
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return new SpannableString(release_tag_name);
        }
    }


    public static class Milestone extends DynamicBaseObject implements Serializable {

        private static final long serialVersionUID = 7557962756095745154L;

        private String milePath = "";
        private String mileName = "";

        public Milestone(JSONObject json) throws JSONException {
            super(json);

           JSONObject projectJson = json.optJSONObject("project");
           String projectPath = projectJson.optString("path", "");

           JSONObject milestoneJson = json.optJSONObject("milestone");
           int id = milestoneJson.optInt("id", 0);

           milePath = String.format("%s/milestone/%s", projectPath, id);
           mileName = milestoneJson.optString("name", "");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s";
            String title = String.format(format, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public String jump() {
            if (action.equals("delete")) {
                return "";
            }

            return Global.HOST + milePath;
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return new SpannableString(mileName);
        }
    }


    public static class Wiki extends DynamicBaseObject implements Serializable {

        private static final long serialVersionUID = -3237817303362954197L;

        int wiki_iid;
        String wiki_path = "";
        String wiki_title = "";

        public Wiki(JSONObject json) throws JSONException {
            super(json);
            wiki_iid = json.optInt("wiki_iid", 0);
            wiki_path = json.optString("wiki_path", "");
            wiki_title = json.optString("wiki_title", "");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s Wiki";
            String title = String.format(format, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public String jump() {
            return Global.HOST + wiki_path;
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return new SpannableString(wiki_title);
        }
    }

    public static class PullRequestBean extends DynamicBaseObject implements Serializable {
        Depot depot;
        String pull_request_title;
        String pull_request_path;

        public PullRequestBean(JSONObject json) throws JSONException {
            super(json);

            if (json.has("depot")) {
                depot = new Depot(json.optJSONObject("depot"));
            }

            pull_request_title = json.optString("pull_request_title");
            pull_request_path = json.optString("pull_request_path");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目%s的 Pull Request";
            String title = String.format(format, user.getHtml(), action_msg, depot.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            final String format = "%s";
            String content = String.format(format, black(pull_request_title));
            return GlobalCommon.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            return makeJump(depot.path);
        }
    }

    public static class MySmalltaskComment {
        String content;
        String id;
        Owner owner;

        public MySmalltaskComment(JSONObject json) throws JSONException {
            content = json.optString("content");
            id = json.optString("id");

            owner = new Owner(json.optJSONObject("owner"));
        }

        public Spanned getContent() {
            return GlobalCommon.changeHyperlinkColor(content, BLACK_COLOR, null);
        }
    }


    public static class ProjectTweet extends DynamicBaseObject implements Serializable {
        String content;
        Project project;

        public ProjectTweet(JSONObject json) throws JSONException {
            super(json);

            project = new Project(json.getJSONObject("project"));
            content = json.optString("content", "");
        }

        @Override
        public Spanned title() {
            final String farmat = "%s %s 项目公告";
            HashMap<String, String> actionMap = new HashMap<>();
            actionMap.put("delete", "删除了");
            actionMap.put("update", "更新了");
            actionMap.put("create", "发布了");
            String actionString = actionMap.get(action);
            if (actionString == null) {
                actionString = "";
            }
            String title = String.format(farmat, user.getHtml(), actionString);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String noImageContent = HtmlContent.parseDynamic(content);
            return GlobalCommon.changeHyperlinkColor(noImageContent, BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            return makeJump(project.path + "/setting/notice");
        }
    }


    public static class MyTaskComment extends DynamicBaseObject implements Serializable {
        MySmalltaskComment mTaskComment;
        Task mTask;

        public MyTaskComment(JSONObject json) throws JSONException {
            super(json);

            mTaskComment = new MySmalltaskComment(json.optJSONObject("taskComment"));
            mTask = new Task(json.optJSONObject("task"));
        }

        @Override
        public Spanned title() {
            final String farmat = "%s %s 任务的评论";
            String title = String.format(farmat, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String noImageContent = HtmlContent.parseDynamic(mTaskComment.content);
            return GlobalCommon.changeHyperlinkColor(noImageContent, BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            return makeJump(mTask.path);
        }
    }

    public static class CommitLineNote extends DynamicBaseObject implements Serializable {

        Project project;
        line_note lineNote;

        public CommitLineNote(JSONObject json) throws JSONException {
            super(json);

            project = new Project(json.getJSONObject("project"));
            lineNote = new line_note(json.getJSONObject("line_note"));
        }

        @Override
        public Spanned title() {
            String s;
            switch (lineNote.noteable_type) {
                case "Commit":
                    s = "commit";
                    break;
                case "MergeRequestBean":
                    s = "Merge Request";
                    break;
                default: //  "PullRequestBean":
                    s = "提交";
            }
            final String farmat = "%s %s 项目 %s 的 %s %s";
            String title = String.format(farmat, user.getHtml(), action_msg, project.getHtml(), s, lineNote.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String textContent = HtmlContent.parseToText(lineNote.getContent());
            String link = createLink(textContent, lineNote.getLinkPath());
            return GlobalCommon.changeHyperlinkColor(link, BLACK_COLOR, imageGetter);
        }
    }

    public static class line_note implements Serializable {
        String content = ""; //: "<p>已经 push 了 fix，见下一个 commit</p>",
        int id;
        String commit_id = ""; //: "0f66fb520ee8560e63c4cdf1c7036eb9331119d7",
        String path = ""; // : "src/main/java/net/coding/core/Application.java",
        String noteable_type = "";

        // 这两个不是同时存在
        String commit_path = ""; //: "/u/wzw/p/coding/git/commit/0f66fb520ee8560e63c4cdf1c7036eb9331119d7"
        String noteable_url = ""; // "/u/1984/p/TestPrivate/git/merge/18"

        public line_note(JSONObject json) {
            content = json.optString("content");
            commit_id = json.optString("commit_id");
            path = ProjectObject.teamPath2User(json.optString("path"));
            id = json.optInt("id");
            noteable_type = json.optString("noteable_type", "");
            commit_path = json.optString("commit_path", "");
            noteable_url = json.optString("noteable_url", "");
        }

        public String getHtml() {
            final int len = 10;
            if (commit_id.length() >= len) {
                return commit_id.substring(0, len);
            }

            return "";
        }

        public String getLinkPath() {
            if (!commit_path.isEmpty())
                return commit_path;

            return noteable_url;
        }

        public String getContent() {
            return content;
        }
    }

    public static class MergeRequestBean extends DynamicBaseObject implements Serializable {
        final MergeRequestBaseDelegate mergeRequestBaseDelegate;

        public MergeRequestBean(JSONObject json) throws JSONException {
            super(json);
            mergeRequestBaseDelegate = new MergeRequestBaseDelegate(json);
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目%s中的 Merge Request";
            String title = String.format(format, user.getHtml(), action_msg, mergeRequestBaseDelegate.depot.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String content = createLink(mergeRequestBaseDelegate.merge_request_title, mergeRequestBaseDelegate.merge_request_path);
            return GlobalCommon.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            return makeJump(mergeRequestBaseDelegate.merge_request_path);
        }

    }

    public static class MergeRequestComment extends DynamicBaseObject implements Serializable {
        String comment_content;
        MergeRequestBaseDelegate mergeRequest;

        public MergeRequestComment(JSONObject json) throws JSONException {
            super(json);
            mergeRequest = new MergeRequestBaseDelegate(json);
            comment_content = json.optString("comment_content");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目%s中的 Merge Request %s";
            String mergeLink = createLink(mergeRequest.merge_request_title, mergeRequest.merge_request_path);
            String title = String.format(format, user.getHtml(), action_msg, mergeRequest.depot.getHtml(), mergeLink);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return GlobalCommon.changeHyperlinkColor(comment_content, BLACK_COLOR, imageGetter);
        }
    }

    public static class PullRequestComment extends DynamicBaseObject implements Serializable {

        Depot depot;
        String pull_request_title;
        String comment_content;
        String pull_request_path;

        public PullRequestComment(JSONObject json) throws JSONException {
            super(json);

            if (json.has("depot")) {
                depot = new Depot(json.optJSONObject("depot"));
            }

            pull_request_title = json.optString("pull_request_title");
            comment_content = json.optString("comment_content");
            pull_request_path = json.optString("pull_request_path");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目%s的 Pull Request";
            String title = String.format(format, user.getHtml(), action_msg, depot.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            final String format = "%s : %s";
            String content = String.format(format, black(pull_request_title), comment_content);
            return GlobalCommon.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }
    }

    public static class BaseProject extends DynamicBaseObject implements Serializable {

        public Project project;

        public BaseProject(JSONObject json) throws JSONException {
            super(json);

            if (json.has("project")) {
                project = new Project(json.optJSONObject("project"));
            }
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目";
            String title = String.format(format, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return GlobalCommon.changeHyperlinkColor(project.getHtml(), BLACK_COLOR, imageGetter);
        }
    }


    public static class DynamicProject extends BaseProject {

        User target_user;

        public DynamicProject(JSONObject json) throws JSONException {
            super(json);
            if (json.has("target_user")) {
                target_user = new User(json.optJSONObject("target_user"));
            }
        }

        @Override
        public Spanned title() {
            if (action.equals("transfer")) {
                String title = String.format("%s 将项目 %s 转让给了 %s", user.getHtml(), project.getHtml(), target_user.getHtml());
                return GlobalCommon.changeHyperlinkColor(title);
            } else {
                return super.title();
            }
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return super.content(imageGetter);
        }
    }

    public static class ProjectBase extends DynamicBaseObject implements Serializable {
        private Project project = new Project();

        public ProjectBase(JSONObject json) throws JSONException {
            super(json);

            if (json.has("project")) {
                project = new Project(json.optJSONObject("project"));
            }
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目 %s";
            String title = String.format(format, user.getHtml(), action_msg, project.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return GlobalCommon.changeHyperlinkColor(project.getHtml(), BLACK_COLOR, imageGetter);
        }
    }

    public static class ProjectWatcher extends ProjectBase implements Serializable {
        public ProjectWatcher(JSONObject json) throws JSONException {
            super(json);
        }
    }

    public static class ProjectStar extends ProjectBase implements Serializable {
        public ProjectStar(JSONObject json) throws JSONException {
            super(json);
        }
    }

    public static class DynamicProjectTopic extends DynamicBaseObject implements Serializable {
        public Project_topic project_topic;
        public Project project;

        public DynamicProjectTopic(JSONObject json) throws JSONException {
            super(json);

            if (json.has("project_topic")) {
                project_topic = new Project_topic(json.optJSONObject("project_topic"));
            }

            if (json.has("project")) {
                project = new Project(json.optJSONObject("project"));
            }
        }

        @Override
        public Spanned title() {
            if (action.equals("comment")) {
                final String format = "%s %s 讨论 %s";
                String title = String.format(format, user.getHtml(), action_msg, project_topic.parent.getHtml());
                return GlobalCommon.changeHyperlinkColor(title);

            } else {
                final String format = "%s %s 讨论";
                String title = String.format(format, user.getHtml(), action_msg);
                return GlobalCommon.changeHyperlinkColor(title);
            }
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return GlobalCommon.changeHyperlinkColor(project_topic.getHtml(), BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            if (project_topic.path.isEmpty()) {
                return "";
            }

            String parentPath = project_topic.parent.path;
            if (!parentPath.isEmpty()) { // 讨论的评论
                return makeJump(parentPath);
            }

            return makeJump(project_topic.path);
        }
    }

    public static class Project_topic implements Serializable {
        String path = "";
        String title = "";
        String content = "";
        Parent parent = new Parent();

        public Project_topic(JSONObject json) throws JSONException {
            path = ProjectObject.teamPath2User(json.optString("path"));
            title = json.optString("title");

            if (json.has("content")) {
                content = json.optString("content");
                parent = new Parent(json.optJSONObject("parent"));
            }
        }

        public String getHtml() {
            if (parent.isEmpty()) {
                return String.format(BLACK_HTML, title);
            } else {
                return String.format(BLACK_HTML, HtmlContent.parseReplacePhoto(content));
            }
        }

        static class Parent {
            String path = "";
            String title = "";

            public Parent() {
            }

            public Parent(JSONObject json) {
                try {
                    path = ProjectObject.teamPath2User(json.optString("path"));
                    title = json.optString("title");
                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            boolean isEmpty() {
                return title.isEmpty();
            }

            public String getHtml() {
                return String.format(BLACK_HTML, title);
            }
        }
    }

    public static class DynamicProjectFileComment extends DynamicBaseObject implements Serializable {

        private Project project;
        private ProjectFileComment projectFileComment;
        private ProjectFile projectFile;
        private OriginProjectFileComment origin_projectFileComment;

        public DynamicProjectFileComment(JSONObject json) throws JSONException {
            super(json);

            project = new Project(json.optJSONObject("project"));
            projectFileComment = new ProjectFileComment(json.optJSONObject("projectFileComment"));
            projectFile = new ProjectFile(json.optJSONObject("projectFile"));
            origin_projectFileComment = new OriginProjectFileComment(json.optJSONObject("origin_projectFileComment"));
        }

        @Override
        public Spanned title() {
            String title = String.format("%s %s 文件 %s 的评论", user.getHtml(), action_msg, projectFile.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String content = projectFileComment.getHtml();
            return GlobalCommon.changeHyperlinkColor(HtmlContent.parseDynamic(content), BLACK_COLOR, imageGetter);

        }

        public ProjectFileComment getProjectFileComment() {
            return projectFileComment;
        }

        public String getComment() {
            return projectFileComment.content;
        }

        public Owner getOwner() {
            return getProjectFileComment().getOwner();
        }

    }

    public static class DynamicProjectFile extends DynamicBaseObject implements Serializable {
        public String version = "";
        String content = "";
        File file;
        Project project;
        String type = "";
        int mProjectId;

        public DynamicProjectFile(JSONObject json) throws JSONException {
            super(json);

            content = json.optString("content");

            if (json.has("file")) {
                file = new File(json.optJSONObject("file"));
            }

            if (json.has("project")) {
                project = new Project(json.optJSONObject("project"));
            }

            version = json.optString("version", "");

            type = json.optString("type");
        }

        @Override
        public Spanned title() {
            String actionType = action.endsWith("dir") ? "文件夹" : "文件"; // create_dir
            final String format = "%s %s %s";
            String title = String.format(format, user.getHtml(), action_msg, actionType);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return GlobalCommon.changeHyperlinkColor(file.getHtml(), BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            if (action.equals("delete_dir") ||
                    action.equals("delete_file")) {
                return super.jump();
            }

            // 文件夹 /u/8206503/p/TestIt2/attachment/65138             /projectid/5741/name/aa
            // 文件 //u/8206503/p/TestIt2/attachment/65683/preview/66171      /projectid/5741/name/aa.jpg
            return makeJump(file.path);
//            + "/projectid/" + mProjectId + "/name/" + file.name);
        }

        public DynamicProjectFile projectId(int id) {
            mProjectId = id;
            return this;
        }

        public static class File {
            String name = "";
            String path = "";

            public File(JSONObject json) throws JSONException {
                if (json.has("name")) {
                    name = json.optString("name");
                }

                if (json.has("path")) {
                    path = ProjectObject.teamPath2User(json.optString("path"));
                }
            }

            public String getHtml() {
                return String.format(BLACK_HTML, name);
            }
        }
    }

    public static class DynamicQcTask extends DynamicBaseObject implements Serializable {
        public Project project;
        public Qc_task qc_task;

        public DynamicQcTask(JSONObject json) throws JSONException {
            super(json);

            project = new Project(json.optJSONObject("project"));
            qc_task = new Qc_task(json.optJSONObject("qc_task"));
        }

        @Override
        public Spanned title() {
            final String format = "%s 创建了 %s 的质量分析任务";
            String title = String.format(format, user.getHtml(), project.getHtml());
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return GlobalCommon.changeHyperlinkColor(qc_task.getHtml(), BLACK_COLOR, imageGetter);
        }
    }

    public static class DynamicDepotPush extends DynamicBaseObject implements Serializable {

        public ArrayList<Commit> commits;
        public Depot depot;
        public String old_sha_path = "";
        public String push_type = "";
        public String ref = "";
        public String ref_path = "";
        public String ref_type = "";

        public DynamicDepotPush(JSONObject json) throws JSONException {
            super(json);

            commits = new ArrayList<>();
            if (json.has("commits")) {
                JSONArray arrayCommits = json.optJSONArray("commits");

                for (int i = 0; i < arrayCommits.length(); ++i) {
                    commits.add(new Commit(arrayCommits.getJSONObject(i)));
                }
            }

            if (json.has("depot")) {
                depot = new Depot(json.optJSONObject("depot"));
            }

            old_sha_path = json.optString("old_sha_path");
            push_type = json.optString("push_type");
            ref = json.optString("ref");
            ref_path = json.optString("ref_path");
            ref_type = json.optString("ref_type");
        }

        @Override
        public Spanned title() {
            String branch = "分支";
            if (ref_type.equals("tag")) {
                branch = "标签";
            }
            final String format = "%s %s 项目%s " + BLACK_HTML;
            String html = createLink(ref, ref_path);
            String title = String.format(format, user.getHtml(), action_msg, branch, html);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        public String getBranch() {
            return ref;
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            if (commits.isEmpty()) {
                return new SpannableString("");
            }

            String content = "";

            for (int i = 0; i < commits.size(); ++i) {
                Commit commit = commits.get(i);
                String url = depot.path + "/commit/" + commit.sha;
                String display = commit.sha;
                if (display.length() > 7) {
                    display = display.substring(0, 7);
                }

                String html = String.format("<a href=\"%s\">[%s] %s</a>", url, display, commit.short_message);
                String singleContent = String.format("%s : %s", commit.committer.getHtml(), html);

                if (i > 0) {
                    content += "<br/>";
                }

                content += singleContent;
            }

            return GlobalCommon.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }
    }

    public static class DynamicMergeRequest extends DynamicBaseObject implements Serializable {

        public String comment_content;
        public int action_icon;
        public MergeComment comment;

        public static class MergeComment implements Serializable {

            public UserObject user;
            public TopicLabelObject label;

            private static final long serialVersionUID = -1623893273311282227L;

            public MergeComment(JSONObject json) {
                try {
                    if (json.has("author")) {
                        user = new UserObject(json.optJSONObject("author"));
                    }
                    if (json.has("reviewer")) {
                        user = new UserObject(json.optJSONObject("reviewer"));
                    }
                    if (json.has("label")) {
                        label = new TopicLabelObject(new JSONObject(json.optString("label")));
                    }
                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }
        }

        public DynamicMergeRequest(JSONObject json, boolean isComment) throws JSONException {
            this(json);
            if (isComment)
                action = "comment";
        }

        public DynamicMergeRequest(JSONObject json) throws JSONException {
            super(json);
            boolean outDate = false;
            comment_content = json.optString("content");
            if (json.has("commit")) {
                comment_content = json.optString("commit");
            }

            if (json.has("comment")) {
                comment = new MergeComment(json.optJSONObject("comment"));
            }
            if (TextUtils.isEmpty(action_msg)) {
                if (action.equals("create")) {
                    action_msg = "创建了合并请求";
                    action_icon = R.drawable.merge_request_create;
                } else if (action.equals("merge")) {
                    action_msg = "合并了该合并请求";
                    action_icon = R.drawable.merge_request_merge;
                } else if (action.equals("refuse")) {
                    action_msg = "拒绝了该合并请求";
                    action_icon = R.drawable.merge_request_reject;
                } else if (action.equals("cancel")) {
                    action_msg = "取消了该合并请求";
                    action_icon = R.drawable.merge_request_outdate;
                } else if (action.equals("update")) {
                    action_msg = "编辑了该合并请求";
                    action_icon = R.drawable.merge_request_edit_merge;
                } else if (action.equals("review")) {
                    action_msg = "对此合并请求评审 +1";
                    action_icon = R.drawable.merge_request_review;
                } else if (action.equals("review_undo")) {
                    action_msg = "撤消了对此合并请求评审 +1";
                    action_icon = R.drawable.merge_request_cancel_review;
                } else if (action.equals("grant")) {
                    action_msg = "授权了该合并请求";
                    action_icon = R.drawable.merge_request_unlock;
                } else if (action.equals("grant_undo")) {
                    action_msg = "取消授权了该合并请求";
                    action_icon = R.drawable.merge_request_lock;
                } else if (action.equals("push")) {
                    action_msg = "推送了新的提交，更新了该合并请求";
                    action_icon = R.drawable.merge_request_push_new;
                } else if (action.equals("update_title")) {
                    action_msg = "编辑了标题";
                    action_icon = R.drawable.merge_request_edit;
                } else if (action.equals("update_content")) {
                    action_msg = "编辑了描述";
                    action_icon = R.drawable.merge_request_edit;
                } else if (action.equals("comment")) {
                    action_msg = "发表了评论";
                } else if (action.equals("add_reviewer")) {
                    action_msg = getUserMessage("增加评审者 %s");
                    action_icon = R.drawable.merge_request_reviewer;
                } else if (action.equals("del_reviewer")) {
                    action_msg = getUserMessage("移除评审者 %s");
                    action_icon = R.drawable.merge_request_reviewer;
                } else if (action.equals("add_watcher")) {
                    action_msg = getUserMessage("添加关注者 %s");
                    action_icon = R.drawable.merge_request_watch;
                } else if (action.equals("del_watcher")) {
                    action_msg = getUserMessage("移除关注者 %s");
                    action_icon = R.drawable.merge_request_watch;
                } else if (action.equals("add_label")) {
                    action_msg = getLabelMessage("添加标签 %s");
                    action_icon = R.drawable.merge_request_tag;
                } else if (action.equals("del_label")) {
                    action_msg = getLabelMessage("移除标签 %s");
                    action_icon = R.drawable.merge_request_tag;
                } else if (action.equals("comment_commit")) {
                    outDate = json.optBoolean("outdated");
                    action_msg = "对文件改动发起了评论";
                    action_icon = outDate ? R.drawable.merge_request_outdate : R.drawable.merge_request_commont_commit;
                } else {
                    action_msg = "";
                    action_icon = R.drawable.merge_request_default;
                }
            }
        }

        private String getUserMessage(String template) {
            String s = "";
            if (comment != null && comment.user != null && comment.user.name != null) {
                s = comment.user.name;
            }
            return String.format(template, s);
        }

        private String getLabelMessage(String template) {
            String s = "";
            if (comment != null && comment.label != null && comment.label.name != null) {
                s = comment.label.name;
            }
            return String.format(template, s);
        }

        @Override
        public Spanned title() {
            String time = Global.dayToNow(created_at);
            if (action.equals("comment")) {
                return new SpannedString(user.getName());
            } else {
                final String format = "%s %s - %s";
                String userString = String.format(BLACK_HTML, user.getName());
                String title = String.format(format, userString, action_msg, time);
                return GlobalCommon.changeHyperlinkColor(title);
            }
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String contentString = comment_content;

            MessageParse parse = HtmlContent.parseMessage(contentString);
            return (GlobalCommon.changeHyperlinkColor(parse.text, imageGetter, Global.tagHandler));


//            return Global.changeHyperlinkColor(comment_content, imageGetter, null);
//            String textContent = HtmlContent.parseToText(comment_content);
//            return Global.changeHyperlinkColor(textContent, BLACK_COLOR, imageGetter);
        }
    }

    public static class DynamicMergeRequestCommentCommit extends DynamicMergeRequest {
        String commitId;
        String path;
        DiffFile.DiffSingleFile diffFile;

        public DynamicMergeRequestCommentCommit(JSONObject json) throws JSONException {
            super(json);
            commitId = json.optString("commitId");
            path = ProjectObject.teamPath2User(json.optString("path"));
            diffFile = new DiffFile.DiffSingleFile(commitId, path);
        }

        public DiffFile.DiffSingleFile getDiffSingleFile() {
            return diffFile;
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            ForegroundColorSpan span = new ForegroundColorSpan(CodingColor.fontGreen);
            SpannableString string = new SpannableString("点击查看评论详情");
            string.setSpan(span, 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return string;
        }
    }


    public static class DynamicTask extends DynamicBaseObject {
        Origin_task origin_task;
        Project project;
        Task task;
        SingleTask.TaskComment taskComment;
        Owner watcher;

        MergeRequestBaseDelegate mergeRequest;
        String ref;
        Commit commit;


        public DynamicTask(JSONObject json) throws JSONException {
            super(json);
            if (json.has("origin_task")) {
                origin_task = new Origin_task(json.optJSONObject("origin_task"));
            }

            if (json.has("commit")) {
                JSONObject commitJson = json.optJSONObject("commit");
                ref = commitJson.optString("ref", "");
                commit = new Commit(commitJson);
            }

            if (json.has("project")) {
                project = new Project(json.optJSONObject("project"));
            }

            if (json.has("task")) {
                task = new Task(json.optJSONObject("task"));
            }

            if (json.has("watcher")) {
                watcher = new Owner(json.optJSONObject("watcher"));
            }

            if (action.equals("update_deadline")) {
                action_msg = "更新了任务的截止日期";
            } else if (action.equals("update_priority")) {
                action_msg = "更新了任务的优先级";
            }

            if (json.has("taskComment")) {
                taskComment = new SingleTask.TaskComment(json.optJSONObject("taskComment"));
                taskComment.created_at = created_at;
                if (task != null) {
                    taskComment.taskId = task.id;
                }
            }

            if (MergeRequestBaseDelegate.has(json)) {
                mergeRequest = new MergeRequestBaseDelegate(json);
            }
        }

        public SingleTask.TaskComment getTaskComment() {
            return taskComment;
        }

        @Override
        public Spanned title() {
            final String format;
            final String title;

            switch (action) {
                case "update_deadline":
                    if (task.deadline.isEmpty()) {
                        action_msg = "移除了任务的截止日期";
                    }
                    format = "%s %s";
                    title = String.format(format, user.getHtml(), action_msg);
                    return GlobalCommon.changeHyperlinkColor(title);

                case "update_priority":
                case "update_description":
                    format = "%s %s";
                    title = String.format(format, user.getHtml(), action_msg);
                    return GlobalCommon.changeHyperlinkColor(title);

                case "reassign":
                    format = "%s %s %s 的任务给 %s";
                    title = String.format(format, user.getHtml(), action_msg, origin_task.owner.getHtml(), task.owner.getHtml());
                    return GlobalCommon.changeHyperlinkColor(title);

                case "remove_watcher":
                    format = "%s 删除了任务 %s 的关注者";
                    title = String.format(format, user.getHtml(), task.getHtml());
                    return GlobalCommon.changeHyperlinkColor(title);

                case "add_watcher":
                    format = "%s 添加了任务 %s 的关注者";
                    title = String.format(format, user.getHtml(), task.getHtml());
                    return GlobalCommon.changeHyperlinkColor(title);

                default:
                    format = "%s %s %s 的任务";
                    title = String.format(format, user.getHtml(), action_msg, task.owner.getHtml());
                    return GlobalCommon.changeHyperlinkColor(title);
            }
        }

        // 任务详情界面的动态
        public Spannable dynamicTitle() {
            final String format;
            final String title;

            String userString = String.format(BLACK_HTML, user.getName());
            String time = Global.dayToNow(created_at);
            switch (action) {
                case "update_deadline":
                    if (task.deadline.isEmpty()) {
                        action_msg = "移除了任务的截止日期";
                    }
                    format = "%s %s - %s";
                    title = String.format(format, userString, action_msg, time);
                    return GlobalCommon.changeHyperlinkColor(title);
                case "reassign":
                    format = "%s %s任务给 %s - %s";
                    title = String.format(format, userString, action_msg, task.owner.name, time);
                    return GlobalCommon.changeHyperlinkColor(title);
                case "update_priority":
                case "update_description":
                    format = "%s %s - %s";
                    title = String.format(format, userString, action_msg, time);
                    return GlobalCommon.changeHyperlinkColor(title);
                case "commit_refer":
                    format = "%s 在分支%s%s任务 - %s<br/>%s:[%s]%s";
                    title = String.format(format, userString, ref, action_msg, time,
                            commit.committer.name,
                            commit.shortSha(),
                            commit.short_message);
                    return GlobalCommon.changeHyperlinkColor(title, BLACK_COLOR_9);

                case "remove_watcher":
                case "add_watcher":
                    format = "%s %s - %s";
                    title = String.format(format, userString, action_msg, time);
                    return GlobalCommon.changeHyperlinkColor(title);

                default:
                    if (target_type.equals("MergeRequestBean")) {

                        String mergeInfo = String.format("<a href=\"%s\">#%s %s</a>", mergeRequest.merge_request_path,
                                mergeRequest.merge_request_iid, mergeRequest.merge_request_title);
                        format = "%s %s合并请求%s - %s";
                        title = String.format(format, userString, action_msg, mergeInfo, time);
                        return GlobalCommon.changeHyperlinkColor(title);
                    }
                    format = "%s %s任务 - %s";
                    title = String.format(format, userString, action_msg, time);
                    return GlobalCommon.changeHyperlinkColor(title);
            }
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            final String s;
            switch (action) {
                case "update_deadline":
                    if (task.deadline.isEmpty()) { // 移除了 deadline
                        s = task.getHtml();
                    } else {
                        Calendar data = Calendar.getInstance();
                        String time[] = task.deadline.split("-");
                        data.set(Integer.valueOf(time[0]), Integer.valueOf(time[1]) - 1, Integer.valueOf(time[2]));
                        String dataString = Global.getDataDetail(data.getTimeInMillis());
                        s = String.format("[%s] %s", dataString, task.getHtml());
                    }

                    break;

                case "update_priority":
                    final String priority[] = new String[]{
                            "有空再看",
                            "正常处理",
                            "优先处理",
                            "十万火急",
                    };

                    s = String.format("[%s] %s", priority[task.priority], task.getHtml());
                    break;

                case "update_description":
                    s = task.getDescripHtml();
                    break;

                case "remove_watcher":
                case "add_watcher":
                    s = watcher.getHtml();
                    break;

                case "delete":
                    s = task.title;
                    break;

                default:
                    s = task.getHtml();
                    break;
            }

            return GlobalCommon.changeHyperlinkColor(s, BLACK_COLOR, imageGetter);


        }

        @Override
        public String jump() {
            if (task.path.isEmpty()) { // 删除任务产生的动态
                return "";
            }

            return Global.HOST + task.path;
        }
    }

    public static class Depot {
        public String name = "";
        public String path = "";

        public Depot(JSONObject json) {
            name = json.optString("name");
            path = ProjectObject.teamPath2User(json.optString("path"));
        }

        public String getHtml() {
            return String.format(BLACK_HTML, name);
        }
    }

    public static class DynamicProjectMember extends DynamicBaseObject implements Serializable {
        public Project project;
        public User target_user;

        public DynamicProjectMember(JSONObject json) throws JSONException {
            super(json);

            if (json.has("project")) {
                project = new Project(json.optJSONObject("project"));
            }

            if (json.has("target_user")) {
                target_user = new User(json.optJSONObject("target_user"));
            }
        }

        @Override
        public Spanned title() {
            String format = "%s %s 项目成员";
            if (action.equals("quit")) {
                format = "%s %s 项目";
            }
            String title = String.format(format, user.getHtml(), action_msg);
            return GlobalCommon.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            if (action.equals("quit")) {
                return GlobalCommon.changeHyperlinkColor(project.getHtml(), BLACK_COLOR, imageGetter);
            }
            return GlobalCommon.changeHyperlinkColor(target_user.getHtml(), BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            if (action.equals("quit")) {
                return "";
            }

            return makeJump(target_user.path);
        }
    }

    public static class Origin_task implements Serializable {
        public String path = "";
        public String title = "";
        public Owner owner = new Owner();

        public Origin_task(JSONObject json) throws JSONException {
            path = ProjectObject.teamPath2User(json.optString("path"));
            title = json.optString("title");

            if (json.has("owner")) {
                owner = new Owner(json.optJSONObject("owner"));
            }
        }
    }

    public static class Owner implements Serializable {
        public String avatar = "";
        public String global_key = "";
        public String name = "";
        public String path = "";

        public Owner(JSONObject json) {
            if (json.has("avatar")) {
                avatar = Global.replaceAvatar(json);
            }

            global_key = json.optString("global_key");
            name = json.optString("name");
            path = ProjectObject.teamPath2User(json.optString("path"));
        }

        public Owner() {
        }

        public Owner(UserObject data) {
            avatar = data.avatar;
            global_key = data.global_key;
            name = data.name;
            path = data.path;
        }

        public boolean isMe() {
            return GlobalData.sUserObject.global_key.equals(global_key);
        }

        public String getName() {
            return name;
        }

        public String getHtml() {
            return HtmlContent.createUserHtml(global_key, name);
        }
    }

    public static class Task implements Serializable {
        public Owner owner = new Owner();
        public String path = "";
        public String title = "";
        public String deadline = "";
        public String description = "";
        public int priority = 0;
        public int id = 0;

        public Task(JSONObject json) throws JSONException {
            if (json.has("owner")) {
                owner = new Owner(json.optJSONObject("owner"));
            }

            path = ProjectObject.teamPath2User(json.optString("path"));
            title = json.optString("title");
            deadline = json.optString("deadline");
            priority = json.optInt("priority");
            description = json.optString("description");
            id = json.optInt("id", 0);
        }

        public String getHtml() {
            return createLink(title, path);
        }

        public String getDescripHtml() {
            return String.format(BLACK_HTML, HtmlContent.parseReplacePhoto(description).text);
        }
    }

    public static class Commit implements Serializable {
        public Committer committer = new Committer();
        public String sha = "";
        public String short_message = "";

        public Commit(JSONObject json) throws JSONException {
            sha = json.optString("sha", "");

            short_message = json.optString("short_message");
            if (json.has("committer")) {
                committer = new Committer(json.optJSONObject("committer"));
            }
        }

        public String shortSha() {
            if (sha.length() >= 7) {
                return sha.substring(0, 7);
            }

            return sha;
        }
    }

    public static class Committer implements Serializable {
        public String avatar = "";
        public String email = "";
        public String link = "";
        public String name = "";

        public Committer(JSONObject json) throws JSONException {
            if (json.has("avatar")) {
                avatar = Global.replaceAvatar(json);
            }

            email = json.optString("email");
            link = json.optString("link");
            name = json.optString("name");
        }

        public Committer() {
        }

        public String getHtml() {
            String item[] = link.split("/");
            return HtmlContent.createUserHtml(item[item.length - 1], name);
        }
    }

    public static class Project implements Serializable {
        public String full_name = "";
        public String name = "";
        public String path = "";

        public Project(JSONObject json) throws JSONException {
            full_name = json.optString("full_name", "");
            name = json.optString("name", "");
            path = ProjectObject.teamPath2User(json.optString("path", ""));
        }

        public Project() {
        }

        public String getHtml() {
            return String.format(DynamicObject.BLACK_HTML, full_name);
        }
    }

    public static class Qc_task implements Serializable {
        public String link = "";
        public User user = new User();

        public Qc_task(JSONObject json) throws JSONException {
            link = json.optString("link");

            if (json.has("user")) {
                user = new User(json.optJSONObject("user"));
            }
        }

        public Qc_task() {
        }

        public String getHtml() {
            return String.format(BLACK_HTML, link);
        }
    }

    public static class User implements Serializable {
        public String avatar = "";
        public String global_key = "";
        public String name = "";
        public String path = "";
        public boolean follow;
        public boolean followed;
        public VIP vip;

        public User(JSONObject json) throws JSONException {
            if (json.has("avatar")) {
                avatar = Global.replaceAvatar(json);
            }

            global_key = json.optString("global_key");
            name = json.optString("name");
            path = ProjectObject.teamPath2User(json.optString("path"));
            follow = json.optInt("follow") != 0;
            followed = json.optInt("followed") != 0;
            vip = VIP.Companion.id2Enum(json.optInt("vip", 1));
        }

        public User() {
        }

        public String getHtml() {
            return HtmlContent.createUserHtml(global_key, name);
        }

        public String getName() {
            return name;
        }
    }

    public static class ProjectFile implements Serializable {
        /**
         * id : 260700
         * title : 私信语音标注-Android.png
         * file_id : 260708
         * project_id : 136139
         * path : /u/suangsuang/p/No72_Andriod_voice/attachment/260707/preview/260708
         * owner : {"global_key":"8206503","name":"陈超","path":"/u/8206503","avatar":"/static/fruit_avatar/Fruit-1.png"}
         */
        private int id;
        private String title;
        private int file_id;
        private int project_id;
        private String path;
        private Owner owner;

        public ProjectFile(JSONObject json) {
            id = json.optInt("id");
            title = json.optString("title");
            file_id = json.optInt("file_id");
            project_id = json.optInt("project_id");
            path = ProjectObject.teamPath2User(json.optString("path"));
            owner = new Owner(json.optJSONObject("owner"));
        }

        String getHtml() {
            return createLink(title, path);
        }
    }

    public static class ProjectFileComment implements Serializable {
        private String content;
        private int id;
        private Owner owner;

        public ProjectFileComment(JSONObject json) {
            content = json.optString("content");
            id = json.optInt("id");
            owner = new Owner(json.optJSONObject("owner"));
        }

        public int getId() {
            return id;
        }

        public String getOwnerGlobalKey() {
            return owner.global_key;
        }

        public String getOwnerName() {
            return owner.name;
        }

        public Owner getOwner() {
            return owner;
        }

        String getHtml() {
            return content;
        }
    }

    public static class OriginProjectFileComment implements Serializable {

        private String title;

        public OriginProjectFileComment(JSONObject json) {
            title = json.optString("title");
        }

        public String getTitle() {
            return title;
        }
    }

    public static class MergeRequestBaseDelegate implements Serializable {
        Depot depot;
        String merge_request_title;
        String merge_request_path;
        int merge_request_iid;

        public MergeRequestBaseDelegate(JSONObject json) {
            depot = new Depot(json.optJSONObject("depot"));
            merge_request_title = json.optString("merge_request_title", "");
            merge_request_path = json.optString("merge_request_path", "");
            merge_request_iid = json.optInt("merge_request_iid", 0);
        }

        public static boolean has(JSONObject json) {
            return json.has("merge_request_path");
        }
    }
}


