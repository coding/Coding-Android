package net.coding.program.common;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import net.coding.program.ImagePagerFragment;
import net.coding.program.R;
import net.coding.program.maopao.MaopaoListBaseFragment;
import net.coding.program.message.MessageListActivity;
import net.coding.program.message.NotifyListActivity;
import net.coding.program.message.UsersListFragment;
import net.coding.program.project.ProjectListFragment;
import net.coding.program.project.detail.AttachmentsActivity;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity;
import net.coding.program.project.detail.AttachmentsTextDetailActivity;
import net.coding.program.project.detail.ProjectDynamicFragment;
import net.coding.program.project.detail.ProjectGitFragment;
import net.coding.program.project.detail.TaskListFragment;
import net.coding.program.project.detail.TopicListFragment;
import net.coding.program.project.detail.merge.MergeListFragment;
import net.coding.program.project.detail.merge.MergeReviewerListFragment;
import net.coding.program.project.maopao.ProjectMaopaoActivity;
import net.coding.program.subject.SubjectListFragment;
import net.coding.program.user.UserProjectListFragment;
import net.coding.program.user.team.TeamListActivity;

/**
 * Created by chaochen on 14-10-24.
 * 内容为空白时显示的提示语
 */
public class BlankViewDisplay {

    public static final String MY_PROJECT_BLANK = "您还木有项目呢，赶快起来创建吧～";
    public static final String OTHER_PROJECT_BLANK = "这个人很懒，一个项目都木有～";
    public static final String MY_SUBJECT_BLANK = "您还没有参与过话题呢～";
    public static final String OTHER_SUBJECT_BLANK = "TA 还没有参与过话题呢～";
    public static final String OTHER_MALL_ORDER_BLANK = "还没有订单记录~";
    public static final String OTHER_MALL_ORDER_BLANK_UNSEND = "没有未发货的订单记录~";
    public static final String OTHER_MALL_ORDER_BLANK_ALREADYSEND = "没有已发货的订单记录~";
    public static final String OTHER_MALL_EXCHANGE_BLANK = "还没有可兑换的商品呢~";

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick) {
        setBlank(itemSize, fragment, request, v, onClick, "");
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick, String tipString) {
        setBlank(itemSize, fragment, request, v, onClick, tipString, 0);
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick, String tipString, int iconId) {
        // 有些界面不需要显示blank状态
        if (v == null) {
            return;
        }

        View btn = v.findViewById(R.id.btnRetry);
        if (request) {
            btn.setVisibility(View.GONE);
        } else {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(onClick);
        }

        setBlank(itemSize, fragment, request, v, tipString, iconId);
    }

    private static void setBlank(int itemSize, Object fragment, boolean request, View v, String tipString, int iconId) {
        boolean show = (itemSize == 0);
        if (!show) {
            v.setVisibility(View.GONE);
            return;
        }
        v.setVisibility(View.VISIBLE);

        String text = "";

        if (tipString.isEmpty()) {
            if (request) {
                if (fragment instanceof ProjectListFragment) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = MY_PROJECT_BLANK;
                } else if (fragment instanceof TaskListFragment) {
                    iconId = R.drawable.ic_exception_blank_task_my;
                    text = "您还没有任务\n赶快为团队做点贡献吧~";
                } else if (fragment instanceof NotifyListActivity) {
                    iconId = R.drawable.ic_exception_blank_task_my;
                } else if (fragment instanceof TopicListFragment) {
                    iconId = R.drawable.ic_exception_blank_topic;
                    text = "还没有讨论\n创建一个讨论发表对项目的看法吧";
                } else if (fragment instanceof MaopaoListBaseFragment) {
                    iconId = R.drawable.ic_exception_blank_maopao;
                    text = "还没有发表过冒泡呢～";
                } else if (fragment instanceof SubjectListFragment) {
                    iconId = R.drawable.ic_exception_blank_maopao;
                    text = "还没有参与过话题呢~";
                } else if (fragment instanceof UsersListFragment) {
                    iconId = R.drawable.ic_exception_blank_message;
                    text = "还没有新消息~";
                } else if (fragment instanceof ProjectDynamicFragment) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = "这里还什么都没有\n赶快起来弄出一点动静吧";
                } else if (fragment instanceof MergeReviewerListFragment) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = "这里还什么都没有\n赶快起来弄出一点动静吧";
                } else if (fragment instanceof ProjectGitFragment) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = "此项目的 Git 仓库为空";
                } else if (fragment instanceof AttachmentsActivity) {
                    iconId = R.drawable.ic_exception_blank_dir;
                    text = " 这里还没有任何文件~";
                } else if (fragment instanceof UserProjectListFragment) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = OTHER_PROJECT_BLANK;
                } else if (fragment instanceof MessageListActivity) {
                    iconId = R.drawable.ic_exception_blank_message;
                    text = "无私信\n打个招呼吧~";
                } else if (fragment instanceof MergeListFragment) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = "这里还什么都没有\n赶快起来弄出一点动静吧~";
                } else if (fragment instanceof ImagePagerFragment
                        || fragment instanceof AttachmentsDownloadDetailActivity
                        || fragment instanceof AttachmentsHtmlDetailActivity
                        || fragment instanceof AttachmentsTextDetailActivity) {
                    iconId = R.drawable.ic_exception_no_network;
                    text = "晚了一步\n文件已经被人删除了~";
                } else if (fragment instanceof ProjectMaopaoActivity) {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = v.getContext().getString(R.string.project_maopao_list_empty);
                } else if (fragment instanceof ProjectDynamicFragment) {
                    iconId = R.drawable.ic_exception_blank_dynamic;
                    text = "暂无相关动态~";
                } else if (fragment instanceof TeamListActivity) {
                    iconId = R.drawable.ic_exception_blank_team;
                    text = "还没有创建团队~";
                } else {
                    iconId = R.drawable.ic_exception_blank_task;
                    text = "还什么都没有~";
                }
            } else {
                iconId = R.drawable.ic_exception_no_network;
                text = "获取数据失败\n请检查下网络是否通畅";
            }
        } else {
            if (request) {
                if (iconId == 0) {
                    iconId = R.drawable.ic_exception_blank_task;
                }
            } else {
                iconId = R.drawable.ic_exception_no_network;
            }

            if (TextUtils.isEmpty(tipString)) {
                if (request) {
                    text = "还什么都没有~";
                } else {
                    text = "获取数据失败";
                }
            } else {
                text = tipString;
            }
        }

        v.findViewById(R.id.icon).setBackgroundResource(iconId);
        TextView textView = (TextView) v.findViewById(R.id.message);
        textView.setText(text);
        textView.setLineSpacing(3.0f, 1.2f);
    }

}
