package net.coding.program.git;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.util.FileIOUtils;

import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;

import java.io.File;

public class GitCodeReadActivity extends BackActivity {

    public static final String PARAM = "PARAM";
    private final String baseUrl = "file:///android_asset/codemirror20180322/demo";

    private boolean isFirstLoading = true;

    File codeFile;

    WebView webView;
    private JSInterface jsInterface = new JSInterface();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_code_read);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith(baseUrl) && isFirstLoading) {
                    isFirstLoading = false;
                    setWebViewContent();
                }
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
//            settings.setAppCachePath(getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
//            settings.setCacheMode();


        updateData();
        initUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        updateData();
        setWebViewContent();
    }

    private void updateData() {
        codeFile = (File) getIntent().getSerializableExtra(PARAM);
        jsInterface.setCodeContent(FileIOUtils.readFile2String(codeFile));
        setActionBarTitle(codeFile.getName());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initUI() {
        try {
            webView.addJavascriptInterface(jsInterface, "Android");
            webView.loadUrl(this.baseUrl + "/loadmode.html");
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setWebViewContent() {
        String fileName = codeFile.getName();
        String script = String.format("javascript:setLanguage('%s')", fileName);
        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d("javascript:callJS()", "js result " + value);
            }
        });
    }

}
