package net.coding.program.common.umeng;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

import net.coding.program.common.GlobalData;

/**
 * Created by chaochen on 14-10-9.
 * 封装 umeng 统计
 */
public class UmengActivity extends AppCompatActivity {

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

        GlobalData.setMainActivityState(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        MobclickAgent.onPause(this);

        GlobalData.setMainActivityState(false);
    }

    protected void umengEvent(String s, String param) {
        MobclickAgent.onEvent(this, s, param);
    }
}
