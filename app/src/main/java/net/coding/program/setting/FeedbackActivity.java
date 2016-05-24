package net.coding.program.setting;

import android.content.pm.PackageInfo;
import android.os.Build;

import net.coding.program.R;
import net.coding.program.project.detail.TopicAddActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_topic_add)
public class FeedbackActivity extends TopicAddActivity {

    String FEED_EXTRA = "";

    @AfterViews
    protected void init2() {
        setActionBarTitle(R.string.title_activity_feedback);
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
    protected String getExtraString() {
        if (FEED_EXTRA.isEmpty()) {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo("net.coding.program", 0);
                String appVersion = pInfo.versionName;
                String phoneModel = Build.MODEL;
                int androidVersion = Build.VERSION.SDK_INT;
                FEED_EXTRA = String.format("\nCoding %s %s (%s)", appVersion, phoneModel, androidVersion);
            } catch (Exception e) {};
        }

        return FEED_EXTRA;
    }

    @Override
    protected String getSendingTip() {
        return "正在发表反馈...";
    }
}
