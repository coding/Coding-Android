package net.coding.program.setting;

import android.content.pm.PackageInfo;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.WebActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_about)
public class AboutActivity extends BackActivity {

    @ViewById
    TextView version;

    @AfterViews
    final void initAboutActivity() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;

            String versionString = String.format("版本：%s", versionName);
            version.setText(versionString);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    void markCoding() {
        Global.updateByMarket(this);
    }

    @Click
    void checkUpdate() {
        Global.updateByMarket(this);
    }

    @Click
    void codingWebsite() {
        WebActivity_.intent(this).url(Global.HOST).start();
    }

}
