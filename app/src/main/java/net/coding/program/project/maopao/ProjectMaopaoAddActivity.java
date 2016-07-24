package net.coding.program.project.maopao;

import android.content.Intent;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.TopicAddActivity;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONObject;

@EActivity(R.layout.activity_project_maopao_add)
public class ProjectMaopaoAddActivity extends BackActivity implements TopicEditFragment.SaveData {

    @Extra
    ProjectObject projectObject;

    private TopicAddActivity.TopicData modifyData = new TopicAddActivity.TopicData();

    ProjectMaopaoEditFragment editFragment;
    ProjectMaopaoPreviewFragment previewFragment;

    @AfterViews
    protected final void initProjectMaopaoAddActivity() {
        editFragment = ProjectMaopaoEditFragment_.builder().build();
        previewFragment = ProjectMaopaoPreviewFragment_.builder().build();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, editFragment)
                .commit();
    }

    @Override
    public void exit() {
        String url = String.format(Global.HOST_API + "/project/%s/tweet", projectObject.getId());
        RequestParams params = new RequestParams();
        params.put("content", modifyData.content);
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(this) {

            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                showProgressBar(false);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false);
            }
        });

        showProgressBar(true);
    }

    @Override
    public void saveData(TopicAddActivity.TopicData data) {
        modifyData = data;
    }

    @Override
    public TopicAddActivity.TopicData loadData() {
        return modifyData;
    }

    @Override
    public void switchPreview() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, previewFragment)
                .commit();
    }

    @Override
    public void switchEdit() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, editFragment)
                .commit();
    }

    @Override
    public String getProjectPath() {
        return projectObject.getProjectPath();
    }

    @Override
    public boolean isProjectPublic() {
        return false;
    }
}
