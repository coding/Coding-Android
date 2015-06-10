package net.coding.program.setting;

import net.coding.program.R;
import net.coding.program.project.detail.TopicAddActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_topic_add)
public class FeedbackActivity extends TopicAddActivity {

    @AfterViews
    protected void init2() {
        getSupportActionBar().setTitle(R.string.title_activity_feedback);
    }

    @Override
    public boolean canShowLabels() {
        return false;
    }

    @Override
    protected int getTopicId() {
        return 39583;
    }

    @Override
    public String getProjectPath() {
        return "/user/coding/project/Coding-Android/";
    }

    @Override
    public boolean isProjectPublic() {
        return true;
    }

    @Override
    protected void showSuccess() {
        showButtomToast("反馈成功");
    }

    @Override
    protected String getSendingTip() {
        return "正在发表反馈...";
    }
}
