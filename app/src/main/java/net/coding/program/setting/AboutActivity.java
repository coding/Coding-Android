package net.coding.program.setting;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.widget.TextView;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.umeng.UmengActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

@EActivity(R.layout.activity_about)
public class AboutActivity extends UmengActivity {

    @ViewById
    TextView version;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;

            String versionString = String.format("版本：%s", versionName);
            version.setText(versionString);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    long mLastTime = 0;
    int mClickCount = 0;

    @Click
    void icon() {
        long millis = Calendar.getInstance().getTimeInMillis();

        if (millis - mLastTime < 3000) {
            ++mClickCount;
        } else {
            mClickCount = 1;
            mLastTime = millis;
        }

        if (mClickCount == 5) {
        }
    }

    static final int RESULT_CODE_LOGIN_OUT = 30;

    @OnActivityResult(RESULT_CODE_LOGIN_OUT)
    void onResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
