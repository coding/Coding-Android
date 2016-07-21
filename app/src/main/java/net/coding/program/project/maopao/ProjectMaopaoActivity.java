package net.coding.program.project.maopao;

import android.widget.ListView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.Maopao;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_project_maopao)
public class ProjectMaopaoActivity extends BackActivity implements FootUpdate.LoadMore {

    @Extra
    ProjectObject projectObject;

    @ViewById
    ListView listView;

    private String projectMaopaoUrl = "";

    private int lastId = RefreshBaseActivity.UPDATE_ALL_INT;

    private List<Maopao.MaopaoObject> listData = new ArrayList<>();
    ProjectMaopaoAdapter projectMaopaoAdapter = new ProjectMaopaoAdapter(listData);

    @AfterViews
    void initProjectMaopaoActivity() {
        listView.setAdapter(projectMaopaoAdapter);

        onRefresh();
    }

    @Override
    public void loadMore() {
        getNetwork(projectMaopaoUrl, projectMaopaoUrl);
    }

    public void onRefresh() {
        projectMaopaoUrl = String.format(Global.HOST_API + "/project/%s/tweet?last_id=%d", projectObject.getId(), lastId);
        initSetting();;
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
}
