package net.coding.program.common;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.AndroidCharacter;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import net.coding.program.R;
import net.coding.program.task.TaskDescrip;

public class TermsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_terms);

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setBackgroundColor(0);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

        try {
            webView.loadDataWithBaseURL(null, Global.readTextFile(getAssets().open("terms")), "text/html", "UTF-8", null);
        } catch (Exception e) {
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
