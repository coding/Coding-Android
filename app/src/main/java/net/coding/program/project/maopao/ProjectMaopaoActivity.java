package net.coding.program.project.maopao;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.Maopao;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_project_maopao)
@OptionsMenu(R.menu.project_maopao)
public class ProjectMaopaoActivity extends BackActivity implements FootUpdate.LoadMore {

    private static final int RESULT_ADD = 1;

    @Extra
    ProjectObject projectObject;

    @ViewById
    ListView listView;

    private String projectMaopaoUrl = "";

    private int lastId = RefreshBaseActivity.UPDATE_ALL_INT;


    View.OnClickListener clickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(v.getContext())
                    .setMessage("删除冒泡?")
                    .setPositiveButton("确定", (dialog, which) -> deleteMaopao(v))
                    .setNegativeButton("取消", null)
                    .show();
        }

        public void deleteMaopao(View v) {
            Maopao.MaopaoObject maopao = (Maopao.MaopaoObject) v.getTag();

            String url = String.format(Global.HOST_API + "/project/%s/tweet/%s", projectObject.getId(), maopao.id);
            MyAsyncHttpClient.delete(ProjectMaopaoActivity.this, url, new MyJsonResponse(ProjectMaopaoActivity.this) {
                @Override
                public void onMySuccess(JSONObject response) {
                    super.onMySuccess(response);
                    listData.remove(maopao);
                    projectMaopaoAdapter.notifyDataSetChanged();

                    showProgressBar(false);
                }

                @Override
                public void onMyFailure(JSONObject response) {
                    super.onMyFailure(response);
                    showProgressBar(false);
                }
            });

            showProgressBar(true);
        }
    };

    private List<Maopao.MaopaoObject> listData = new ArrayList<>();
    ProjectMaopaoAdapter projectMaopaoAdapter = new ProjectMaopaoAdapter(listData, this, clickDelete);

    @AfterViews
    void initProjectMaopaoActivity() {
        listView.setAdapter(projectMaopaoAdapter);

        onRefresh();
    }

    @Override
    public void loadMore() {
        if (lastId == 0) {
            return;
        }

        projectMaopaoUrl = String.format(Global.HOST_API + "/project/%s/tweet?last_id=%d", projectObject.getId(), lastId);
        getNetwork(projectMaopaoUrl, projectMaopaoUrl);
    }

    public void onRefresh() {
        initSetting();
        lastId = RefreshBaseActivity.UPDATE_ALL_INT;
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(projectMaopaoUrl)) {
            if (code == 0) {
                if (lastId == RefreshBaseActivity.UPDATE_ALL_INT) {
                    listData.clear();
                }

                JSONArray jsonArray = respanse.optJSONArray("data");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        listData.add(new Maopao.MaopaoObject(jsonArray.optJSONObject(i)));
                    }

                    lastId = listData.get(listData.size() - 1).id;
                } else {
                    lastId = 0;
                }

                projectMaopaoAdapter.notifyDataSetChanged();
            }

        }
    }

    @OptionsItem
    void actionAdd() {
        ProjectMaopaoAddActivity_.intent(this).projectObject(projectObject).startForResult(RESULT_ADD);
    }

    @OnActivityResult(RESULT_ADD)
    void onResultAdd(int resultCode) {
        if (resultCode == RESULT_OK) {
            onRefresh();
        }
    }

}
