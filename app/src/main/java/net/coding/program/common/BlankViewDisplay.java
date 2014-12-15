package net.coding.program.common;

import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.message.UsersListFragment;
import net.coding.program.project.ProjectListFragment;
import net.coding.program.project.detail.ProjectDynamicFragment;
import net.coding.program.project.detail.ProjectGitFragment;
import net.coding.program.project.detail.TaskListFragment;
import net.coding.program.project.detail.TopicListFragment;

/**
 * Created by chaochen on 14-10-24.
 */
public class BlankViewDisplay {

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick) {
        View btn = v.findViewById(R.id.btnRetry);
        if (request) {
            btn.setVisibility(View.INVISIBLE);
        } else {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(onClick);
        }

        setBlank(itemSize, fragment, request, v);
    }

    private static void setBlank(int itemSize, Object fragment, boolean request, View v) {
        boolean show = (itemSize == 0);
        if (!show) {
            v.setVisibility(View.GONE);
            return;
        }
        v.setVisibility(View.VISIBLE);

        int iconId = R.drawable.ic_exception_no_network;
        String text = "";

        if (request) {
            if (fragment instanceof ProjectListFragment) {
                iconId = R.drawable.ic_exception_blank_task;
                text = "您还没有项目\n快去coding网站创建吧";

            } else if (fragment instanceof TaskListFragment) {
                iconId = R.drawable.ic_exception_blank_task;
                text = "您还没有任务\n赶快为团队做点贡献吧~";

            } else if (fragment instanceof TopicListFragment) {
                iconId = R.drawable.ic_exception_blank_task;
                text = "你还没有讨论\n创建一个讨论发表对项目的看法吧";

            } else if (fragment instanceof MaopaoListFragment) {
                iconId = R.drawable.ic_exception_blank_maopao;
                text = "还没有冒泡~";

            } else if (fragment instanceof UsersListFragment) {
                iconId = R.drawable.ic_exception_blank_maopao;
                text = "快和你的好友打个招呼吧~";
            } else if (fragment instanceof ProjectDynamicFragment) {
                iconId = R.drawable.ic_exception_blank_task;
                text = "这里还什么都没有\n赶快起来弄出一点动静吧";
            } else if (fragment instanceof ProjectGitFragment) {
                iconId = R.drawable.ic_exception_blank_task;
                text = "此项目的 Git 仓库为空";
            }

        } else {
            iconId = R.drawable.ic_exception_no_network;
            text = "获取数据失败\n请检查下网络是否通畅";
        }

        v.findViewById(R.id.icon).setBackgroundResource(iconId);
        ((TextView) v.findViewById(R.id.message)).setText(text);
    }
}
