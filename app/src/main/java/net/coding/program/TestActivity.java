package net.coding.program;

import android.os.Bundle;

import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.wiki.WikiMainActivity_;

import org.json.JSONObject;

public class TestActivity extends BackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.button).setOnClickListener(v -> click1());

        click1();
    }

    private void click1() {
        String urlProject = String.format(FileUrlActivity.getHostProject(), "codingcorp", "TestWiki");

        getNetwork(urlProject, urlProject);
        MyAsyncHttpClient.get(this, urlProject, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                try {
                    ProjectObject projectObject = new ProjectObject(response.optJSONObject("data"));
                    WikiMainActivity_.intent(TestActivity.this).project(projectObject).start();
                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
            }
        });
    }


}
