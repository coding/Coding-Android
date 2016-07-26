package net.coding.program.project.maopao;


import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_task_desp_edit)
public class ProjectMaopaoEditFragment extends TopicEditFragment {

    @AfterViews
    void initProjectMaopaoEditFragment() {
        edit.setHint("输入冒泡内容 ");
    }

    @Override
    protected String getCustomUploadPhoto() {
        return Global.HOST_API + "/tweet/insert_image";
    }
}
