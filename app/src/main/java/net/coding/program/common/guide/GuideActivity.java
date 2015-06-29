package net.coding.program.common.guide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.prolificinteractive.parallaxpager.ParallaxContextWrapper;

import net.coding.program.LoginActivity;
import net.coding.program.R;

public class GuideActivity extends ActionBarActivity {

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
        setContentView(R.layout.activity_parallax);

        mUri = getIntent().getParcelableExtra(LoginActivity.EXTRA_BACKGROUND);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, new ParallaxFragment())
                    .commit();
        }
    }

    public Uri getUri() {
        return mUri;
    }
}
