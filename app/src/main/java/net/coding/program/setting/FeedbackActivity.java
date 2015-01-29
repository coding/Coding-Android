package net.coding.program.setting;

import net.coding.program.R;
import net.coding.program.project.detail.TopicAddActivity;

import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_topic_add)
public class FeedbackActivity extends TopicAddActivity {

    @Override
    protected String getTopicId() {
        return "182";
    }

    @Override
    protected void showSuccess() {
        showButtomToast("反馈成功");
    }
}
