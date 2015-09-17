package net.coding.program.common.guide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.prolificinteractive.parallaxpager.ParallaxContextWrapper;

import net.coding.program.LoginActivity;
import net.coding.program.R;
import net.coding.program.login.ZhongQiuGuideActivity;

public class GuideActivity extends ActionBarActivity {

    public static final String BROADCAST_GUIDE_ACTIVITY = "BROADCAST_GUIDE_ACTIVITY";
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GuideActivity.this.finish();
        }
    };
    private Uri mUri;

    @Override
    protected void attachBaseContext(Context newBase) {
        //ParallaxPager and Calligraphy don't seem to play nicely together
        //The solution was to add a listener for view creation events so that we can hook up
        // Calligraphy to our view creation calls instead.
        super.attachBaseContext(
                new ParallaxContextWrapper(newBase, new OpenCalligraphyFactory())
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZhongQiuGuideActivity.showHolidayGuide(this);
        setContentView(R.layout.activity_parallax);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_GUIDE_ACTIVITY);
        registerReceiver(receiver, filter);

        mUri = getIntent().getParcelableExtra(LoginActivity.EXTRA_BACKGROUND);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, new ParallaxFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    public Uri getUri() {
        return mUri;
    }
}
