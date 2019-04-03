package net.coding.program;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
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
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.maopao.share.CustomShareBoard;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_web)
public class WebActivity extends BaseActivity {

    @Extra
    protected String url = Global.HOST;

    @Extra
    protected boolean share = false; // 可以弹出显示分享 Dialog

    @Extra
    protected String title = "";

    @ViewById
    protected WebView webView;

    @ViewById
    protected ProgressBar progressBar;
    protected TextView actionbarTitle;
    String loading = "";

    @AfterViews
    protected final void initWebActivity() {
        Log.d("", "WebActivity " + url);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        actionbarTitle.setText(title);

        loading = actionbarTitle.getText().toString();

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {

                                       @Override
                                       public void onProgressChanged(WebView view, int newProgress) {
                                           progressBar.setProgress(newProgress);
                                           if (newProgress == 100) {
                                               // 没有title显示网址
                                               String currentTitle = actionbarTitle.getText().toString();
                                               if (loading.equals(currentTitle) && TextUtils.isEmpty(title)) {
                                                   actionbarTitle.setText(url);
                                               }

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
                                           if (TextUtils.isEmpty(title)) {
                                               actionbarTitle.setText(title);
                                           }
                                       }
                                   }
        );

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webView.setWebViewClient(new CustomWebViewClient(this));
        String useAgent = MyAsyncHttpClient.getMapHeaders().get("User-Agent");
        webView.getSettings().setUserAgentString(useAgent);
        webView.loadUrl(url, MyAsyncHttpClient.getMapHeaders());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(net.coding.program.R.menu.menu_web, menu);

        if (!share) {
            menu.findItem(R.id.action_share).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        webView = null;

        CustomShareBoard.onDestory(this);

        super.onDestroy();
    }

    @OptionsItem
    protected final void action_browser() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(WebActivity.this, "用浏览器打开失败", Toast.LENGTH_LONG).show();
        }
    }

    @OptionsItem
    protected final void action_copy() {
        String urlString = url;
        if (urlString == null) {
            Toast.makeText(WebActivity.this, "复制链接失败", Toast.LENGTH_SHORT).show();
            return;
        }

        Global.copy(WebActivity.this, urlString);
        Toast.makeText(WebActivity.this, urlString + " 已复制", Toast.LENGTH_SHORT).show();
    }

    @OptionsItem
    protected final void action_share() {
        String urlString = url;
        if (urlString == null) {
            Toast.makeText(WebActivity.this, "获取链接失败", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = actionbarTitle.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(WebActivity.this, "获取标题失败", Toast.LENGTH_SHORT).show();
            return;
        }

        action_share_third();
    }

    @OptionsItem(android.R.id.home)
    protected final void actionBack() {
        finish();
    }

    void action_share_third() {
        String title = actionbarTitle.getText().toString();
        CustomShareBoard.ShareData shareData = new CustomShareBoard.ShareData("Coding", title, url);
        CustomShareBoard shareBoard = new CustomShareBoard(this, shareData);
        Rect rect = new Rect();
        View decorView = getWindow().getDecorView();
        decorView.getWindowVisibleDisplayFrame(rect);
        int winHeight = getWindow().getDecorView().getHeight();
        // 在 5.0 的android手机上，如果是 noactionbar，显示会有问题
        shareBoard.showAtLocation(decorView, Gravity.BOTTOM, 0, winHeight - rect.bottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomShareBoard.onActivityResult(requestCode, resultCode, data, this);
    }
}

