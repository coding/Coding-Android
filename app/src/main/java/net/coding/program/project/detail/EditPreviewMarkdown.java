package net.coding.program.project.detail;

import net.coding.program.common.model.topic.TopicData;

/**
 * Created by chenchao on 2017/4/13.
 * markdown 文件在编辑模式和预览模式间切换
 */
public interface EditPreviewMarkdown {

    void saveData(TopicData data);

    TopicData loadData();

    void switchPreview();

    void switchEdit();

    void exit();

    String getProjectPath();

    boolean isProjectPublic();
}
