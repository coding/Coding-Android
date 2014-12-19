package net.coding.program.model;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import net.coding.program.Global;
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
 */
public class DynamicObject {

//                   ‘Project’,#
//                   ‘ProjectMember’,#
//            ‘Tweet’,
//            ‘TweetComment’,
//                   ‘Depot’,#
//                   ‘Task’,#
//            ‘UserFollow’,
//                   ‘ProjectFile’, #
//                   ‘ProjectTopic’,#
//                      ‘PullRequestBean’,#
//                      ‘PullRequestComment’,#
//                      ‘ProjectStar’,#
//                      ‘ProjectWatcher’,#
//                   ‘QcTask’#

    static final String BLACK_HTML = "<font color='#666666'>%s</font>";
    static final int BLACK_COLOR = 0xff666666;

    public static class DynamicBaseObject implements Serializable {
        public String action = "";
        public String action_msg = "";
        public long created_at;
        public String id = "";
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

            id = json.optString("id");
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

    static String black(String s) {
        return String.format(BLACK_HTML, s);
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
            String title = String.format(format, user.name, action_msg, depot.getHtml());
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
//            String noImageContent = Global.realParse1(mTaskComment.content, "<img src=\"", "\"", "<", "/>", "[图片]");
            String noImageContent = HtmlContent.parseDynamic(mTaskComment.content);
            return Global.changeHyperlinkColor(noImageContent, BLACK_COLOR, imageGetter);
        }

        @Override
        public String jump() {
            return makeJump(mTask.path);
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
            String content = merge_request_title;
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
            String title = String.format(format, user.name, action_msg, depot.getHtml(), merge_request_title);
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
            String title = String.format(format, user.name, action_msg, depot.getHtml());
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

    public static class ProjectNoContent extends DynamicBaseObject implements Serializable {
        public ProjectNoContent(JSONObject json) throws JSONException {
            super(json);
        }

        @Override
        public Spanned title() {
            final String format = "%s %s 项目";
            String title = String.format(format, user.getHtml(), action_msg);
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            return new SpannableString("");
        }
    }

    public static class ProjectWatcher extends ProjectNoContent implements Serializable {
        public ProjectWatcher(JSONObject json) throws JSONException {
            super(json);
        }
    }

    public static class ProjectStar extends ProjectNoContent implements Serializable {
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
                return String.format(BLACK_HTML, content);
            }
        }
    }

    public static class DynamicProjectFile extends DynamicBaseObject implements Serializable {
        String content = "";
        File file;
        Project project;
        String type = "";

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

        String mProjectId = "";

        public DynamicProjectFile projectId(String id) {
            mProjectId = id;
            return this;
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

            commits = new ArrayList<Commit>();
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
            final String format = "%s %s 项目分支 " + BLACK_HTML;
            String title = String.format(format, user.getHtml(), action_msg, ref);
            return Global.changeHyperlinkColor(title);
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            if (commits.isEmpty()) {
                return new SpannableString("");
            }

            String content = "";
            Commit commit = commits.get(0);
            content += commit.committer.getHtml();
            content += ":" + commit.short_message;

            for (int i = 1; i < commits.size(); ++i) {
                content += "<br/>";
                commit = commits.get(i);
                content += commit.committer.getHtml();
                content += ":" + commit.short_message;
            }

            return Global.changeHyperlinkColor(content, BLACK_COLOR, imageGetter);
        }
    }

    public static class DynamicTask extends DynamicBaseObject {
        Origin_task origin_task;
        Project project;
        Task task;

        public DynamicTask(JSONObject json) throws JSONException {
            super(json);
            if (json.has("origin_task")) {
                origin_task = new Origin_task(json.optJSONObject("origin_task"));
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
        }

        @Override
        public Spanned title() {
            if (action.equals("update_deadline") || action.equals("update_priority")) {
                final String format = "%s %s";
                String title = String.format(format, user.getHtml(), action_msg);
                return Global.changeHyperlinkColor(title);

            } else {
                final String format = "%s %s %s 的任务";
                String title = String.format(format, user.getHtml(), action_msg, task.owner.getHtml());
                return Global.changeHyperlinkColor(title);
            }
        }

        private String getDay(long time) {
            Calendar calendarToday = Calendar.getInstance();
            calendarToday.set(calendarToday.get(Calendar.YEAR), calendarToday.get(Calendar.MONTH), calendarToday.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

            final long oneDay = 1000 * 3600 * 24;
            long today = calendarToday.getTimeInMillis();
            long tomorrow = today + oneDay;
            long tomorrowNext = tomorrow + oneDay;
            long tomorrowNextNext = tomorrowNext + oneDay;
            long yesterday = today - oneDay;
            long lastYesterday = yesterday - oneDay;

            if (time >= today) {
                if (tomorrow > time) {
                    return "今天";
                } else if (tomorrowNext > time) {
                    return "明天";
                } else if (tomorrowNextNext > time) {
                    return "后天";
                }
            } else {
                if (time > yesterday) {
                    return "昨天";
                } else if (time > lastYesterday) {
                    return "前天";
                }
            }

            return null;
        }

        private String getWeek(long time) {

            Calendar today = Calendar.getInstance();
            today.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            today.set(Calendar.HOUR, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);

            final long oneWeek = 1000 * 60 * 60 * 24 * 7;

            long weekBegin = today.getTimeInMillis();
            long nextWeekBegin = weekBegin + oneWeek;
            long nextnextWeekBegin = nextWeekBegin + oneWeek;
            long lastWeekBegin = weekBegin - oneWeek;

            if (time >= weekBegin) {
                if (nextWeekBegin > time) {
                    return Global.WeekFormatTime.format(time);
                } else if (nextnextWeekBegin > time) {
                    return Global.NextWeekFormatTime.format(time);
                }
            } else {
                if (time > lastWeekBegin) {
                    return Global.LastWeekFormatTime.format(time);
                }
            }
            return null;
        }

        @Override
        public Spanned content(MyImageGetter imageGetter) {
            try {
                if (action.equals("update_deadline")) {
                    Calendar data = Calendar.getInstance();
                    String time[] = task.deadline.split("-");
                    data.set(Integer.valueOf(time[0]), Integer.valueOf(time[1]) - 1, Integer.valueOf(time[2]));
                    String dataString = getDay(data.getTimeInMillis());
                    if (dataString == null) {
                        dataString = getWeek(data.getTimeInMillis());
                        if (dataString == null) {
                            dataString = Global.MonthDayFormatTime.format(data.getTimeInMillis());
                        }
                    }

                    String s = String.format("[%s] %s", dataString, task.getHtml());
                    return Global.changeHyperlinkColor(s, BLACK_COLOR, imageGetter);
                } else if (action.equals("update_priority")) {
                    final String priority[] = new String[]{
                            "有空再看",
                            "正常处理",
                            "优先处理",
                            "十万火急",
                    };

                    String s = String.format("[%s] %s", priority[task.priority], task.getHtml());
                    return Global.changeHyperlinkColor(s, BLACK_COLOR, imageGetter);
                }
            } catch (Exception e) {
                Global.errorLog(e);
            }

            return Global.changeHyperlinkColor(task.getHtml(), BLACK_COLOR, imageGetter);
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

        public Owner(JSONObject json) throws JSONException {
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

        public String getHtml() {
            return HtmlContent.createUserHtml(global_key, name);
        }
    }

    public static class Task implements Serializable {
        public Owner owner = new Owner();
        public String path = "";
        public String title = "";
        public String deadline = "";
        public int priority = 0;

        public Task(JSONObject json) throws JSONException {
            if (json.has("owner")) {
                owner = new Owner(json.optJSONObject("owner"));
            }

            path = json.optString("path");
            title = json.optString("title");
            deadline = json.optString("deadline");
            priority = json.optInt("priority");
        }

        public String getHtml() {
            return String.format(BLACK_HTML, title);
        }
    }

    public static class Commit implements Serializable {
        public Committer committer = new Committer();
        public String sha = "";
        public String short_message = "";

        public Commit(JSONObject json) throws JSONException {
            sha = json.optString("sha");
            short_message = json.optString("short_message");
            if (json.has("committer")) {
                committer = new Committer(json.optJSONObject("committer"));
            }
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
    }

}


