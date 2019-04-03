package net.coding.program.project.detail;

import net.coding.program.R;
import net.coding.program.project.detail.merge.BranchManageFragment_;
import net.coding.program.project.detail.merge.ProjectMergeFragment_;
import net.coding.program.project.detail.merge.ProjectPullFragment_;
import net.coding.program.project.detail.merge.ReleaseManageFragment_;
import net.coding.program.project.detail.readme.ReadmeFragment_;

/**
 * Created by chenchao on 2017/1/3.
 */
public enum ProjectFunction {

    dynamic(R.id.itemDynamic, R.drawable.project_button_icon_dynamic, "动态", ProjectDynamicParentFragment_.class),
    task(R.id.itemTask, R.drawable.project_button_icon_task, "任务", ProjectTaskFragment_.class),
    taskBoard(R.id.itemTaskBoard, R.drawable.project_button_icon_task_board, "任务看板", null),
    topic(R.id.itemTopic, R.drawable.project_button_icon_topic, "讨论", TopicFragment_.class),
    document(R.id.itemDocment, R.drawable.project_button_icon_docment, "文件", null),
    code(R.id.itemCode, R.drawable.project_button_icon_code, "代码浏览", ProjectGitFragmentMain_.class),
    member(R.id.itemMember, R.drawable.project_button_icon_member, "成员", MembersListFragment_.class),
    readme(R.id.itemReadme, R.drawable.project_button_icon_readme, "README", ReadmeFragment_.class),
    merge(R.id.itemMerge, R.drawable.project_button_icon_merge, "合并请求", ProjectMergeFragment_.class),
    pullRequest(R.id.itemMerge, R.drawable.project_button_icon_merge, "Pull Request", ProjectPullFragment_.class),
    branchManage(R.id.itemBranch, R.drawable.project_button_icon_branch, "分支管理", BranchManageFragment_.class),
    releaseManage(R.id.itemRelease, R.drawable.project_button_icon_release, "发布管理", ReleaseManageFragment_.class),
    git(R.id.itemGit, R.drawable.project_button_icon_git, "本地阅读", null),
    wiki(R.id.itemWiki, R.drawable.project_button_icon_wiki, "Wiki", null);

    public int id;
    public int icon;
    public String title;
    public Class fragment;

    ProjectFunction(int id, int icon, String title, Class fragment) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.fragment = fragment;
    }

    public static ProjectFunction idToEnum(int id) {
        for (ProjectFunction item : values()) {
            if (item.id == id) {
                return item;
            }
        }

        return dynamic;
    }
}
