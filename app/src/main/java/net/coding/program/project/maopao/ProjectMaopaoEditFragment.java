package net.coding.program.project.maopao;


import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.task.TaskDespEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_topic_edit)
public class ProjectMaopaoEditFragment extends TaskDespEditFragment {

    @AfterViews
    void initProjectMaopaoEditFragment() {
        edit.setHint(R.string.input_project_maopao_content);
    }

    @Override
    protected String getCustomUploadPhoto() {
        return Global.HOST_API + "/tweet/insert_image";
    }
}
