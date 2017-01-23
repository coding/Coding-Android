package net.coding.program.setting;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import net.coding.program.EnterpriseApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.ProjectHomeActivity_;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_manage_project_list)
public class ManageProjectListActivity extends BackActivity {

    private static final String TAG_PROJECT = "TAG_PROJECT";

    @ViewById
    UltimateRecyclerView listView;

    ProjectAdapter adapter;

    ArrayList<ProjectObject> listData = new ArrayList<>();

    @AfterViews
    void initManageProjectListActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));
        listView.setEmptyView(R.layout.fragment_enterprise_project_empty, R.layout.fragment_enterprise_project_empty);

        adapter = new ProjectAdapter(listData);
        listView.setAdapter(adapter);
        listView.setDefaultOnRefreshListener(() -> onRefresh());
        listView.enableDefaultSwipeRefresh(true);

        onRefresh();
    }

    private void onRefresh() {
        String host = String.format("%s/team/%s/projects", Global.HOST_API, EnterpriseApp.getEnterpriseGK());
        getNetwork(host, TAG_PROJECT);
    }

    protected class ProjectAdapter extends easyRegularAdapter<ProjectObject, ProjectHolder> {

        public ProjectAdapter(List<ProjectObject> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.enterprise_manage_project_list_item;
        }

        @Override
        protected ProjectHolder newViewHolder(View view) {
            // // TODO: 2017/1/23 获取的 current_user_role_id 有问题。/api/team/codingapp/projects
//            view.setOnClickListener(clickItem);
            return new ProjectHolder(view);
        }

        @Override
        protected void withBindHolder(ProjectHolder holder, ProjectObject data, int position) {
            ProjectObject item = getItem(position);
            holder.name.setText(item.name);
            holder.memberCount.setText(String.format("%s 人", data.member_num));
            holder.rootLayout.setTag(item);
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);
        }

        @Override
        public ProjectHolder newHeaderHolder(View view) {
            return new ProjectHolder(view, true);
        }

        @Override
        public ProjectHolder newFooterHolder(View view) {
            return new ProjectHolder(view, true);
        }
    }

    public static class ProjectHolder extends UltimateRecyclerviewViewHolder {

        public TextView name;
        public TextView memberCount;
        public ImageView image;
        public View rootLayout;

        public ProjectHolder(View view, boolean isHeader) {
            super(view);
        }

        public ProjectHolder(View view) {
            super(view);
            rootLayout = view;
            name = (TextView) view.findViewById(R.id.name);
            memberCount = (TextView) view.findViewById(R.id.memberCount);
            image = (ImageView) view.findViewById(R.id.icon);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_PROJECT)) {
            if (code == 0) {
                listData.clear();
                JSONArray array = respanse.optJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    listData.add(new ProjectObject(array.optJSONObject(i)));
                }

                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private View.OnClickListener clickItem = v -> {
        Object object = v.getTag();
        if (object instanceof ProjectObject) {
            ProjectObject item = (ProjectObject) object;

            if (item.isJoined()) {
                ProjectHomeActivity_.intent(this)
                        .mProjectObject(item)
                        .startForResult(InitProUtils.REQUEST_PRO_UPDATE);
            } else {
                showMiddleToast("无权进入项目");
            }
        }
    };

}
