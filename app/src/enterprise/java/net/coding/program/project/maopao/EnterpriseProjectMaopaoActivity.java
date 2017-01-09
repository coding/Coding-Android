package net.coding.program.project.maopao;

import net.coding.program.R;

import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_project_maopao)
public class EnterpriseProjectMaopaoActivity extends ProjectMaopaoActivity {

    @Override
    protected void initAdapter() {
        projectMaopaoAdapter = new EnterpriseProjectMaopaoAdapter(listData, this, clickDelete, clickListItem);
    }

}
