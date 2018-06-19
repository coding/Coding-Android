package net.coding.program.project.maopao;

import android.content.Intent;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditPreviewActivity;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.topic.TopicData;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.project.detail.ProjectCampt;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONObject;

@EActivity(R.layout.activity_project_maopao_add)
public class ProjectMaopaoAddActivity extends MDEditPreviewActivity implements ProjectCampt {

    @Extra
    ProjectObject projectObject;

    @Extra
    Maopao.MaopaoObject maopao;

    private TopicData modifyData = new TopicData();

    @AfterViews
    protected final void initProjectMaopaoAddActivity() {
        if (maopao != null && !TextUtils.isEmpty(maopao.raw)) {
            modifyData.content = maopao.raw;
            setActionBarTitle("修改项目公告");
        }

        editFragment = CodingCompat.instance().getProjectMaopaoEditFragment();
        previewFragment = ProjectMaopaoPreviewFragment_.builder().build();
        initEditPreviewFragment();
        switchEdit();
    }

    @Override
    public void exit() {
        Global.hideSoftKeyboard(this);
        if (TextUtils.isEmpty(modifyData.content)) {
            showButtomToast("内容不能为空");
            return;
        }

        if (maopao == null) {
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
        } else {
            String url = String.format(Global.HOST_API + "/project/%s/tweet/%s", projectObject.getId(), maopao.id);
            RequestParams params = new RequestParams();
            params.put("raw", modifyData.content);
            MyAsyncHttpClient.put(this, url, params, new MyJsonResponse(this) {

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
    }

    @Override
    public void saveData(TopicData data) {
        modifyData = data;
    }

    @Override
    public TopicData loadData() {
        return modifyData;
    }

    @Override
    public String getProjectPath() {
        return projectObject.getProjectPath();
    }

    @Override
    public boolean isProjectPublic() {
        return false;
    }

    @Override
    public int getProjectId() {
        return projectObject.id;
    }
}
