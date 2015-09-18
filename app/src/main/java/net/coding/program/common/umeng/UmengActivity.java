package net.coding.program.common.umeng;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by chaochen on 14-10-9.
 */
public class UmengActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.openActivityDurationTrack(false);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        MobclickAgent.onPause(this);
    }

    protected void umengEvent(String s, String param) {
        MobclickAgent.onEvent(this, s, param);
    }
}
