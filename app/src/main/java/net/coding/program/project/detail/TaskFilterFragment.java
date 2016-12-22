package net.coding.program.project.detail;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.DrawerLayoutHelper;
import net.coding.program.common.FilterListener;
import net.coding.program.common.Global;
import net.coding.program.common.network.LoadingFragment;
import net.coding.program.event.EventFilterDetail;
import net.coding.program.model.FilterModel;
import net.coding.program.model.TaskCountModel;
import net.coding.program.model.TaskLabelModel;
import net.coding.program.model.TaskProjectCountModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by anfs on 16/12/2016.
 */
public class TaskFilterFragment extends LoadingFragment {

    //final String urlTaskLabels = Global.HOST_API + "/v2/tasks/search_filters";

    //项目外
    protected final String urlTaskCountAll = Global.HOST_API + "/tasks/count";
    protected final String urlTaskLabel = Global.HOST_API + "/projects/tasks/labels?role=";
    //项目外特定项目
    protected final String urlProjectTaskCount = Global.HOST_API + "/project/%s/tasks/counts";
    protected final String urlProjectTaskLabels = Global.HOST_API + "/project/%s/tasks/labels?role=";

    //项目内 全部成员
    //全部任务-标签-数量
    /**
     * 「全部任务」数量 = processing + done
     * 「我创建的」数量 = create
     * 进行中已完成的数量也有了
     */
    protected final String urlALL_Count = Global.HOST_API + "/project/%s/task/count";
    protected final String urlALL_WATCH_Count = Global.HOST_API + "/tasks/search?project_id=%s&watcher=%s";
    //全部任务」的标签
    protected final String urlALL_Label = Global.HOST_API + "/user/%s/project/%s/task/label?withCount=true";

    //某个成员的任务数量
    protected final String urlSome_Count = Global.HOST_API + "/project/%s/user/%s/tasks/counts";
    protected final String urlSome_Label = Global.HOST_API + "/project/%s/user/%s/tasks/labels";

    //任务筛选
    protected TextView toolBarTitle;
    protected List<TaskLabelModel> taskLabelModels = new ArrayList<>();
    protected final String[] mMeActions = new String[]{"owner", "watcher", "creator"};
    protected FilterModel mFilterModel;
    protected int statusIndex = 0;////筛选的index

    protected TaskCountModel mTaskCountModel;
    protected TaskProjectCountModel mTaskProjectCountModel;


    protected String getRole() {
        if (statusIndex >= mMeActions.length) {
            statusIndex = 0;
        }
        return mMeActions[statusIndex];
    }

    protected void initFilterViews() {
        toolBarTitle = (TextView) getActivity().findViewById(R.id.toolbarProjectTitle);
    }

    // 用于处理推送
    public void meActionFilter() {
        //确定是我的任务筛选
        iniTaskStatusLayout();
        iniTaskStatus();
    }

    private void iniTaskStatusLayout() {
        if (getActivity() == null) return;

        View viewById = getActivity().findViewById(R.id.ll_task_filter);
        viewById.setVisibility(viewById.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }


    private void iniTaskStatus() {
        if (getActivity() == null) return;

        int[] filterItem = {R.id.tv_status1, R.id.tv_status2, R.id.tv_status3};
        String[] filterTxtCount = new String[0];
        String[] filterTxt = new String[]{
                isProjectInner() ? "全部任务" : "我的任务",
                "我关注的",
                "我创建的"
        };

        if (mTaskCountModel != null) {
            filterTxtCount = new String[]{
                    String.format(" (%d)", mTaskCountModel.processing + mTaskCountModel.done),
                    String.format(" (%d)", mTaskCountModel.watchAll),
                    String.format(" (%d)", mTaskCountModel.create)
            };
        }

        if (mTaskProjectCountModel != null) {
            filterTxtCount = new String[]{
                    String.format(" (%d)", mTaskProjectCountModel.ownerProcessing + mTaskProjectCountModel.ownerDone),
                    String.format(" (%d)", mTaskProjectCountModel.watcherProcessing + mTaskProjectCountModel.watcherDone),
                    String.format(" (%d)", mTaskProjectCountModel.creatorProcessing + mTaskProjectCountModel.creatorDone)
            };
        }

        int font2 = getResources().getColor(R.color.font_2);
        int green = getResources().getColor(R.color.green);
        for (int i = 0; i < filterItem.length; i++) {
            TextView status = (TextView) getActivity().findViewById(filterItem[i]);
            int finalI = i;
            status.setOnClickListener(v -> {
                this.statusIndex = finalI;
                toolBarTitle.setText(filterTxt[finalI]);
                iniTaskStatus();
                iniTaskStatusLayout();
                sureFilter();
            });

            if (filterTxtCount.length == 3) {
                status.setText(filterTxt[i] + filterTxtCount[i]);
            } else {
                status.setText(filterTxt[i]);
            }

            status.setTextColor(i != this.statusIndex ? font2 : green);
            status.setCompoundDrawablesWithIntrinsicBounds(0, 0, i != this.statusIndex ? 0 : R.drawable.ic_task_status_list_check, 0);
        }
    }

    protected void postLabelJson(String tag, int code, JSONObject respanse) {

    }

    protected void sureFilter() {
        EventBus.getDefault().post(new EventFilterDetail(mMeActions[statusIndex], mFilterModel));
    }

    protected final void actionFilter() {
        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            return;
        }

        if (mFilterModel == null) {
            mFilterModel = new FilterModel(taskLabelModels);
        } else {
            mFilterModel.labelModels = taskLabelModels;
        }

        if (mTaskCountModel != null) {
            if (statusIndex == 0) {
                mFilterModel.statusTaskDoing = mTaskCountModel.processing;
                mFilterModel.statusTaskDone = mTaskCountModel.done;
            } else if (statusIndex == 1) {
                mFilterModel.statusTaskDoing = mTaskCountModel.watchAllProcessing;
                mFilterModel.statusTaskDone = mTaskCountModel.getWatcherDoneCount();
            } else if (statusIndex == 2) {
                mFilterModel.statusTaskDoing = mTaskCountModel.createProcessing;
                mFilterModel.statusTaskDone = mTaskCountModel.getCreatorDoneCount();
            }
        }

        if (mTaskProjectCountModel != null) {
            if (statusIndex == 0) {
                mFilterModel.statusTaskDoing = mTaskProjectCountModel.ownerProcessing;
                mFilterModel.statusTaskDone = mTaskProjectCountModel.ownerDone;
            } else if (statusIndex == 1) {
                mFilterModel.statusTaskDoing = mTaskProjectCountModel.watcherProcessing;
                mFilterModel.statusTaskDone = mTaskProjectCountModel.watcherDone;
            } else if (statusIndex == 2) {
                mFilterModel.statusTaskDoing = mTaskProjectCountModel.creatorProcessing;
                mFilterModel.statusTaskDone = mTaskProjectCountModel.creatorDone;
            }
        }

        DrawerLayoutHelper.getInstance().initData(getContext(), isProjectInner(), drawerLayout, mFilterModel, new FilterListener() {
            @Override
            public void callback(FilterModel filterModel) {
                mFilterModel = filterModel;
                sureFilter();
                changeFilterIcon(mFilterModel.isFilter());
            }
        });

        drawerLayout.openDrawer(GravityCompat.END);
    }

    private void changeFilterIcon(boolean isFilter) {
        if (getActivity() == null) return;
        ActionMenuItemView viewById = (ActionMenuItemView) getActivity().findViewById(R.id.action_filter);
        if (viewById != null) {
            viewById.setIcon(getResources().getDrawable(isFilter ? R.drawable.ic_menu_filter_selected : R.drawable.ic_menu_filter));
        }
    }

    public void setStatusIndex(int statusIndex) {
        this.statusIndex = statusIndex;
    }

    public int getStatusIndex() {
        return statusIndex;
    }

    @Override
    public void onRefresh() {

    }

    protected boolean isProjectInner() {
        return false;
    }
}
