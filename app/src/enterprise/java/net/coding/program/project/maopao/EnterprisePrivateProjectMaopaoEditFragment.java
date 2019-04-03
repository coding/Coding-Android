package net.coding.program.project.maopao;


import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.project.detail.ProjectCampt;
import net.coding.program.task.TaskDespEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_topic_edit)
public class EnterprisePrivateProjectMaopaoEditFragment extends TaskDespEditFragment {

    @AfterViews
    void initProjectMaopaoEditFragment() {
        edit.setHint(R.string.input_project_maopao_content);
    }

    @Override
    protected String getCustomUploadPhoto() {

        if (getActivity() instanceof ProjectCampt) {
            return String.format("%s/project/%s/file/upload", Global.HOST_API, ((ProjectCampt) getActivity()).getProjectId());
        } else {
            return Global.HOST_API + "/tweet/insert_image";
        }
    }
}
