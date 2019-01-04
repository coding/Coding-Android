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
import net.coding.program.common.event.EventFilterDetail;
import net.coding.program.common.event.EventUpdateTaskCount;
import net.coding.program.common.model.FilterModel;
import net.coding.program.common.model.TaskLabelModel;
import net.coding.program.common.model.TaskProjectCountModel;
import net.coding.program.common.network.LoadingFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by anfs on 16/12/2016.
 */
public class TaskFilterFragment extends LoadingFragment {

    //项目外
    protected final String urlTaskCountAll = Global.HOST_API + "/tasks/count";
    protected final String urlTaskLabel = Global.HOST_API + "/projects/tasks/labels?role=";
    protected final String urlTaskSomeCount_owner = Global.HOST_API + "/tasks/list?project_id=%s&owner=%s";
    protected final String urlTaskSomeCount_watcher = Global.HOST_API + "/tasks/list?project_id=%s&watcher=%s";
    protected final String urlTaskSomeCount_creator = Global.HOST_API + "/tasks/list?project_id=%s&creator=%s";
    protected final String urlTaskSomeOther = Global.HOST_API + "/project/%s/tasks/counts";

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
    protected final String urlALL_WATCH_Count = Global.HOST_API + "/tasks/list?project_id=%s&watcher=%s";
    //全部任务」的标签
    protected final String urlALL_Label = Global.HOST_API + "/user/%s/project/%s/task/label?withCount=true";

    //某个成员的任务数量
    protected final String urlSome_Count = Global.HOST_API + "/project/%s/user/%s/tasks/counts";
    protected final String urlSome_Label = Global.HOST_API + "/project/%s/user/%s/tasks/labels";
    protected final String[] mMeActions = new String[]{"owner", "watcher", "creator"};
    //任务筛选
    protected TextView toolBarTitle;
    protected List<TaskLabelModel> taskLabelModels = new ArrayList<>();
    protected FilterModel mFilterModel;
    protected int statusIndex = 0;////筛选的index

    //数量关联的唯一对象
    protected TaskProjectCountModel mTaskProjectCountModel;
    private DrawerLayout drawerLayout;

    protected String getRole() {
        if (statusIndex >= mMeActions.length) {
            statusIndex = 0;
        }
        return mMeActions[statusIndex];
    }

    protected void initFilterViews() {
        toolBarTitle = (TextView) getActivity().findViewById(R.id.mainTaskToolbarTitle);
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
        boolean needHide = viewById.getVisibility() == View.VISIBLE;
        viewById.setVisibility(needHide ? View.GONE : View.VISIBLE);
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

        if (mTaskProjectCountModel != null) {
            filterTxtCount = new String[]{
                    String.format(" (%s)", mTaskProjectCountModel.owner),
                    String.format(" (%s)", mTaskProjectCountModel.watcher),
                    String.format(" (%s)", mTaskProjectCountModel.creator)
            };
        }

        int font2 = getResources().getColor(R.color.font_1);
        int green = getResources().getColor(R.color.select_1);
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

    protected void sureFilter() {
        EventBus.getDefault().post(new EventFilterDetail(mMeActions[statusIndex], mFilterModel));
    }

    protected void setDrawerData() {
        if (mFilterModel == null) {
            mFilterModel = new FilterModel(taskLabelModels);
        } else {
            mFilterModel.labelModels = taskLabelModels;
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

        updateDrawer(mFilterModel);
    }

    private void updateDrawer(FilterModel filterModel) {
        drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            return;
        }
        DrawerLayoutHelper.getInstance().initData(getActivity(), drawerLayout, filterModel, new FilterListener() {
            @Override
            public void callback(FilterModel filterModel) {
                mFilterModel = filterModel;
                sureFilter();
                changeFilterIcon(mFilterModel.isFilter());
            }
        });
    }

    protected final void actionFilter() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void changeFilterIcon(boolean isFilter) {
        if (getActivity() == null) return;
        ActionMenuItemView viewById = (ActionMenuItemView) getActivity().findViewById(R.id.action_filter);
        if (viewById != null) {
            viewById.setIcon(getResources().getDrawable(isFilter ? R.drawable.ic_menu_filter_selected : R.drawable.ic_menu_filter));
        }
    }

    public int getStatusIndex() {
        return statusIndex;
    }

    public void setStatusIndex(int statusIndex) {
        this.statusIndex = statusIndex;
    }

    @Override
    public void onRefresh() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventUpdateCount(EventUpdateTaskCount event) {
        if (getActivity() == null) return;

        int[] filterItem = {R.id.tv_status1, R.id.tv_status2, R.id.tv_status3};
        String[] filterTxtCount = new String[0];
        String[] filterTxt = new String[]{
                isProjectInner() ? "全部任务" : "我的任务",
                "我关注的",
                "我创建的"
        };

        if (mTaskProjectCountModel != null) {
            filterTxtCount = new String[]{
                    String.format(" (%s)", mTaskProjectCountModel.owner),
                    String.format(" (%s)", mTaskProjectCountModel.watcher),
                    String.format(" (%s)", mTaskProjectCountModel.creator)
            };
        }

        for (int i = 0; i < filterItem.length; i++) {
            TextView status = getActivity().findViewById(filterItem[i]);
            if (filterTxtCount.length == 3) {
                status.setText(filterTxt[i] + filterTxtCount[i]);
            } else {
                status.setText(filterTxt[i]);
            }
        }
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    protected boolean isProjectInner() {
        return false;
    }
}
