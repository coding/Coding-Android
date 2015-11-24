package net.coding.program.model;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.MyImageGetter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cc191954 on 14-8-9.
 * 动态的各种类型都在这里
 */
public class DynamicObject {

    static final String BLACK_HTML = "<font color='#666666'>%s</font>";
    static final int BLACK_COLOR = 0XFF666666;
    static final int BLACK_COLOR_9 = 0XFF999999;

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
            }
        }

        public Spanned title() {
            final String format = "%s %s";
            String title = String.format(format, user.getHtml(), action_msg);
            return Global.changeHyperlinkColor(title);
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            final String format = "%s";
            String content = String.format(format, black(pull_request_title));
            return Global.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
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
            return Global.changeHyperlinkColor(content, BLACK_COLOR, null);
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String noImageContent = HtmlContent.parseDynamic(mTaskComment.content);
            return Global.changeHyperlinkColor(noImageContent, BLACK_COLOR, imageGetter);
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String textContent = HtmlContent.parseToText(lineNote.getContent());
            String link = createLink(textContent, lineNote.getLinkPath());
            return Global.changeHyperlinkColor(link, BLACK_COLOR, imageGetter);
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
            path = json.optString("path");
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
        Depot depot;
        String merge_request_title;
        String merge_request_path;

        public MergeRequestBean(JSONObject json) throws JSONException {
            super(json);

            depot = new Depot(json.optJSONObject("depot"));
            merge_request_title = json.optString("merge_request_title");
            merge_request_path = json.optString("merge_request_path");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目%s中的 Merge Request";
            String title = String.format(format, user.getHtml(), action_msg, depot.getHtml());
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String content = createLink(merge_request_title, merge_request_path);
            return Global.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }

//        @Override
//        public String jump() {
//            return "不支持跳转到 MergeRequestBean";
//        }
    }

    public static class MergeRequestComment extends DynamicBaseObject implements Serializable {
        String merge_request_title;
        String merge_request_path;
        String comment_content;
        Depot depot;

        public MergeRequestComment(JSONObject json) throws JSONException {
            super(json);

            merge_request_title = json.optString("merge_request_title");
            merge_request_path = json.optString("merge_request_path");
            comment_content = json.optString("comment_content");
            depot = new Depot(json.optJSONObject("depot"));
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目%s中的 Merge Request %s";
            String mergeLink = createLink(merge_request_title, merge_request_path);
            String title = String.format(format, user.getHtml(), action_msg, depot.getHtml(), mergeLink);
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return Global.changeHyperlinkColor(comment_content, BLACK_COLOR, imageGetter);
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            final String format = "%s : %s";
            String content = String.format(format, black(pull_request_title), comment_content);
            return Global.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return Global.changeHyperlinkColor(project.getHtml(), BLACK_COLOR, imageGetter);
        }
    }


    public static class DynamicProject extends BaseProject {
        public DynamicProject(JSONObject json) throws JSONException {
            super(json);
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return Global.changeHyperlinkColor(project.getHtml(), BLACK_COLOR, imageGetter);
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
                return Global.changeHyperlinkColor(title);

            } else {
                final String format = "%s %s 讨论";
                String title = String.format(format, user.getHtml(), action_msg);
                return Global.changeHyperlinkColor(title);
            }
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return Global.changeHyperlinkColor(project_topic.getHtml(), BLACK_COLOR, imageGetter);
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
            path = json.optString("path");
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
                    path = json.optString("path");
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            String content = projectFileComment.getHtml();
            return Global.changeHyperlinkColor(HtmlContent.parseDynamic(content), BLACK_COLOR, imageGetter);

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

            type = json.optString("type");
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 文件";
            String title = String.format(format, user.getHtml(), action_msg);
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return Global.changeHyperlinkColor(file.getHtml(), BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            if (action.equals("delete_dir") ||
                    action.equals("delete_file")) {
                return super.jump();
            }

            // 文件夹 /u/8206503/p/TestIt2/attachment/65138             /projectid/5741/name/aa
            // 文件 //u/8206503/p/TestIt2/attachment/65683/preview/66171      /projectid/5741/name/aa.jpg
            return makeJump(file.path + "/projectid/" + mProjectId + "/name/" + file.name);
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
                    path = json.optString("path");
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return Global.changeHyperlinkColor(qc_task.getHtml(), BLACK_COLOR, imageGetter);
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
            return Global.changeHyperlinkColor(title);
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

            return Global.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }
    }

    public static class DynamicTask extends DynamicBaseObject {
        Origin_task origin_task;
        Project project;
        Task task;
        TaskObject.TaskComment taskComment;

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

            if (action.equals("update_deadline")) {
                action_msg = "更新了任务的截止日期";
            } else if (action.equals("update_priority")) {
                action_msg = "更新了任务的优先级";
            }

            if (json.has("taskComment")) {
                taskComment = new TaskObject.TaskComment(json.optJSONObject("taskComment"));
                taskComment.created_at = created_at;
            }
        }

        public TaskObject.TaskComment getTaskComment() {
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
                    return Global.changeHyperlinkColor(title);
                case "update_priority":
                case "update_description":
                    format = "%s %s";
                    title = String.format(format, user.getHtml(), action_msg);
                    return Global.changeHyperlinkColor(title);

                default:
                    format = "%s %s %s 的任务";
                    title = String.format(format, user.getHtml(), action_msg, task.owner.getHtml());
                    return Global.changeHyperlinkColor(title);
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
                    return Global.changeHyperlinkColor(title);
                case "update_priority":
                case "update_description":
                    format = "%s %s - %s";
                    title = String.format(format, userString, action_msg, time);
                    return Global.changeHyperlinkColor(title);
                case "commit_refer":
                    format = "%s 在分支%s%s任务 - %s<br/>%s:[%s]%s";
                    title = String.format(format, userString, ref, action_msg, time,
                            commit.committer.name,
                            commit.shortSha(),
                            commit.short_message);
                    return Global.changeHyperlinkColor(title, BLACK_COLOR_9);

                case "remove_watcher":
                case "add_watcher":
                    format = "%s %s - %s";
                    title = String.format(format, userString, action_msg, time);
                    return Global.changeHyperlinkColor(title);

                default:
                    format = "%s %s任务 - %s";
                    title = String.format(format, userString, action_msg, time);
                    return Global.changeHyperlinkColor(title);
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

                default:
                    s = task.getHtml();
                    break;
            }

            return Global.changeHyperlinkColor(s, BLACK_COLOR, imageGetter);


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

        public Depot(JSONObject json) throws JSONException {
            name = json.optString("name");
            path = json.optString("path");
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
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            if (action.equals("quit")) {
                return Global.changeHyperlinkColor(project.getHtml(), BLACK_COLOR, imageGetter);
            }
            return Global.changeHyperlinkColor(target_user.getHtml(), BLACK_COLOR, imageGetter);
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
            path = json.optString("path");
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
            path = json.optString("path");
        }

        public Owner() {
        }

        public Owner(UserObject data) {
            avatar = data.avatar;
            global_key = data.global_key;
            name = data.name;
            path = data.path;
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

        public Task(JSONObject json) throws JSONException {
            if (json.has("owner")) {
                owner = new Owner(json.optJSONObject("owner"));
            }

            path = json.optString("path");
            title = json.optString("title");
            deadline = json.optString("deadline");
            priority = json.optInt("priority");
            description = json.optString("description");
        }

        public String getHtml() {
            return String.format(BLACK_HTML, title);
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
            full_name = json.optString("full_name");
            name = json.optString("name");
            path = json.optString("path");
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

        public User(JSONObject json) throws JSONException {
            if (json.has("avatar")) {
                avatar = Global.replaceAvatar(json);
            }

            global_key = json.optString("global_key");
            name = json.optString("name");
            path = json.optString("path");
            follow = json.optInt("follow") != 0;
            followed = json.optInt("followed") != 0;
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
            path = json.optString("path");
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
}


