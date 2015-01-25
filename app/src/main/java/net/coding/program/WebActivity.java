package net.coding.program;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.common.Global;
import net.coding.program.common.htmltext.URLSpanNoUnderline;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_web)
@OptionsMenu(R.menu.menu_web)
public class WebActivity extends Activity {

    @Extra
    String url = "https://coding.net";

    @ViewById
    WebView webView;

    @ViewById
    ProgressBar progressBar;

    private TextView actionbarTitle;
    private View actionbarClose;

    @AfterViews
    void init() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.actionbar_close_icon);

        actionbarTitle = (TextView) findViewById(R.id.actionbar_title);
        actionbarClose = findViewById(R.id.actionbar_close);
        actionbarClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {

                                       @Override
                                       public void onProgressChanged(WebView view, int newProgress) {
                                           progressBar.setProgress(newProgress);
                                           if (newProgress == 100) {
                                               progressBar.setVisibility(View.INVISIBLE);
                                               AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                                               animation.setDuration(500);
                                               animation.setAnimationListener(new Animation.AnimationListener() {
                                                   @Override
                                                   public void onAnimationStart(Animation animation) {
                                                   }

                                                   @Override
                                                   public void onAnimationEnd(Animation animation) {
                                                       progressBar.setVisibility(View.INVISIBLE);
                                                   }

                                                   @Override
                                                   public void onAnimationRepeat(Animation animation) {
                                                   }
                                               });
                                               progressBar.startAnimation(animation);
                                           } else {
                                               progressBar.setVisibility(View.VISIBLE);
                                           }
                                       }

                                       @Override
                                       public void onReceivedTitle(WebView view, String title) {
                                           actionbarTitle.setText(title);
                                       }
                                   }
        );

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webView.setWebViewClient(new CustomWebViewClient(this));
        webView.loadUrl(url);
    }

    @OptionsItem
    void action_copy() {
        String urlString = webView.getUrl();
        Global.copy(urlString, this);
        Toast.makeText(this, urlString + " 已复制", Toast.LENGTH_SHORT).show();
    }

    @OptionsItem
    void action_browser() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "用浏览器打开失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    public static class CustomWebViewClient extends WebViewClient {

        Context mContext;

        public CustomWebViewClient(Context context) {
            mContext = context;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return URLSpanNoUnderline.openActivityByUri(mContext, url, false, false);
        }
    }
}
