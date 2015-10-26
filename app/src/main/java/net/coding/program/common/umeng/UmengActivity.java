package net.coding.program.common.umeng;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.umeng.analytics.MobclickAgent;

import net.coding.program.MyApp;

/**
 * Created by chaochen on 14-10-9.
 */
public class UmengActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.openActivityDurationTrack(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
        MobclickAgent.onResume(this);

        MyApp.setMainActivityState(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        MobclickAgent.onPause(this);

        MyApp.setMainActivityState(false);
    }

    protected void umengEvent(String s, String param) {
        MobclickAgent.onEvent(this, s, param);
    }
}
