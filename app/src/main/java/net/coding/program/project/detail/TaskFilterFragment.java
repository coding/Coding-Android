package net.coding.program.project.detail;

import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.FilterDialog;
import net.coding.program.common.Global;
import net.coding.program.common.network.LoadingFragment;
import net.coding.program.event.EventFilterDetail;
import net.coding.program.message.JSONUtils;
import net.coding.program.model.TaskLabelModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by anfs on 16/12/2016.
 */
public class TaskFilterFragment extends LoadingFragment {

    final String urlTaskLabels = Global.HOST_API + "/v2/tasks/search_filters";
    //任务筛选
    protected TextView toolBarTitle;
    protected List<TaskLabelModel> taskLabelModels = new ArrayList<>();
    protected final String[] mMeActions = new String[]{"owner", "watcher", "creator"};
    protected FilterDialog.FilterModel mFilterModel;
    protected int statusIndex = 0;////筛选的index

    protected void initFilterViews() {
        toolBarTitle = (TextView) getActivity().findViewById(R.id.toolbarProjectTitle);
        getNetwork(urlTaskLabels, urlTaskLabels);
    }

    // 用于处理推送
    public void meActionFilter() {
        //确定是我的任务筛选
        iniTaskStatusLayout();
        iniTaskStatus();
    }

    protected void iniTaskStatusLayout() {
        if (getActivity() == null) return;

        View viewById = getActivity().findViewById(R.id.ll_task_filter);
        viewById.setVisibility(viewById.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }


    protected void iniTaskStatus() {
        if (getActivity() == null) return;

        int[] filterItem = {R.id.tv_status1, R.id.tv_status2, R.id.tv_status3};
        int font2 = getResources().getColor(R.color.font_2);
        int green = getResources().getColor(R.color.green);
        for (int i = 0; i < filterItem.length; i++) {
            TextView status = (TextView) getActivity().findViewById(filterItem[i]);
            int finalI = i;
            status.setOnClickListener(v -> {
                this.statusIndex = finalI;
                toolBarTitle.setText(status.getText());
                iniTaskStatus();
                iniTaskStatusLayout();
                sureFilter();
            });
            status.setTextColor(i != this.statusIndex ? font2 : green);
            status.setCompoundDrawablesWithIntrinsicBounds(0, 0, i != this.statusIndex ? 0 : R.drawable.ic_task_status_list_check, 0);
        }
    }

    protected void postLabelJson(String tag, int code, JSONObject respanse) {
        if (tag.equals(urlTaskLabels)) {
            if (code == 0) {
                try {
                    taskLabelModels = JSONUtils.getList("labels", respanse.getString("data"), TaskLabelModel.class);
                } catch (JSONException e) {
                    Global.errorLog(e);
                }
            }
        }
    }

    private void sureFilter() {
        EventBus.getDefault().post(new EventFilterDetail(mMeActions[statusIndex], mFilterModel));
    }

    protected final void actionFilter() {

        if (mFilterModel == null) {
            mFilterModel = new FilterDialog.FilterModel(taskLabelModels);
        } else {
            mFilterModel.labelModels = taskLabelModels;
        }

        FilterDialog.getInstance().show(getContext(), mFilterModel, new FilterDialog.SearchListener() {
            @Override
            public void callback(FilterDialog.FilterModel filterModel) {
                mFilterModel = filterModel;
                sureFilter();
                changeFilterIcon(mFilterModel.isFilter());
            }
        });
    }

    private void changeFilterIcon(boolean isFilter) {
        if (getActivity() == null) return;

        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (mToolbar != null) {
            ActionMenuItemView viewById = (ActionMenuItemView) mToolbar.findViewById(R.id.action_filter);
            if (viewById != null) {
                viewById.setIcon(getResources().getDrawable(isFilter ? R.drawable.ic_menu_filter_selected : R.drawable.ic_menu_filter));
            }
        }
    }

    @Override
    public void onRefresh() {

    }
}
