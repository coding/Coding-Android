package net.coding.program.common.activity;

import android.webkit.WebView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_webview_detail)
public class WebviewDetailActivity extends BackActivity {

    @Extra
    String comment;

    @ViewById
    WebView webView;

    @AfterViews
    void initWeviewDetailActivity() {
        Global.setWebViewContent(webView, "topic-android.html", comment);
    }
}
