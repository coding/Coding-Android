package net.coding.program.guide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.prolificinteractive.parallaxpager.ParallaxContextWrapper;

import net.coding.program.LoginActivity;
import net.coding.program.R;
import net.coding.program.common.event.EventLoginSuccess;
import net.coding.program.common.ui.BaseActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GuideActivity extends BaseActivity {

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

//        ZhongQiuGuideActivity.showHolidayGuide(this);
        setContentView(R.layout.activity_parallax);

        mUri = getIntent().getParcelableExtra(LoginActivity.EXTRA_BACKGROUND);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, new ParallaxFragment())
                    .commit();
        }
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClose(EventLoginSuccess event) {
        finish();
    }

    public Uri getUri() {
        return mUri;
    }
}
