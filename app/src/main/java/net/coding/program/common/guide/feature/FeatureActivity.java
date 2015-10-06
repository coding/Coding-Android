package net.coding.program.common.guide.feature;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.MainActivity_;
import net.coding.program.R;
import net.coding.program.model.AccountInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_feature)
public class FeatureActivity extends BaseActivity {

    @AfterViews
    protected final void initFeatureActivity() {
        AccountInfo.markGuideReaded(this);
    }

    @Click
    void clickGo() {
        finish();
        MainActivity_.intent(this).start();
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
}
