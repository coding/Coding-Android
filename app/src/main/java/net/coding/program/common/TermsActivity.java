package net.coding.program.common;

import android.webkit.WebView;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_terms)
public class TermsActivity extends BackActivity {

    @ViewById
    WebView webView;

    @AfterViews
    protected void initTermsActivity() {
        Global.initWebView(webView);
        try {
            webView.loadDataWithBaseURL(null, Global.readTextFile(getAssets().open("terms.html")), "text/html", "UTF-8", null);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }
}
