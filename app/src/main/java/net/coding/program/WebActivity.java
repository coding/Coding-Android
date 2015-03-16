package net.coding.program;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.common.DialogUtil;
import net.coding.program.common.Global;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.umeng.UmengActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EActivity(R.layout.activity_web)
//@OptionsMenu(R.menu.menu_web)
@OptionsMenu(R.menu.common_more)
public class WebActivity extends UmengActivity {

    @Extra
    String url = "https://coding.net";

    @ViewById
    WebView webView;

    @ViewById
    ProgressBar progressBar;

    private TextView actionbarTitle;
    private View actionbarClose;

    String loading = "";

    @AfterViews
    void init() {
//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setCustomView(R.layout.actionbar_close_icon);
//        actionBar.setDisplayShowCustomEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_action_bar);
        setSupportActionBar(toolbar);

        actionbarTitle = (TextView) findViewById(R.id.actionbar_title);
        actionbarClose = findViewById(R.id.actionbar_close);
        actionbarClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loading = actionbarTitle.getText().toString();

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {

                                       @Override
                                       public void onProgressChanged(WebView view, int newProgress) {
                                           progressBar.setProgress(newProgress);
                                           if (newProgress == 100) {
                                               // 没有title显示网址
                                               String currentTitle = actionbarTitle.getText().toString();
                                               if (loading.equals(currentTitle)) {
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

//    @OptionsItem
//    void action_copy() {
//        String urlString = webView.getUrl();
//        Global.copy(this, urlString);
//        Toast.makeText(this, urlString + " 已复制", Toast.LENGTH_SHORT).show();
//    }

//    @OptionsItem
//    void action_browser() {
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
//            startActivity(intent);
//        } catch (Exception e) {
//            Toast.makeText(this, "用浏览器打开失败", Toast.LENGTH_LONG).show();
//        }
//    }

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

        super.onDestroy();
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

    @OptionsItem
    protected void action_more() {
        showRightTopPop();
    }

    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow = null;

    private void showRightTopPop() {
        if (mRightTopPopupWindow == null) {
            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList();
            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.copy_link), R.drawable.ic_menu_link);
            popupItemArrayList.add(downloadItem);

            DialogUtil.RightTopPopupItem browserItem = new DialogUtil.RightTopPopupItem(getString(R.string.open_by_browser), 0);
            popupItemArrayList.add(browserItem);

            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(this, popupItemArrayList, onRightTopPopupItemClickListener);
        }

        mRightTopPopupWindow.adapter.notifyDataSetChanged();

        Rect rectgle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        mRightTopPopupWindow.adapter.notifyDataSetChanged();
        mRightTopPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mRightTopPopupWindow.showAtLocation(getAnchorView(), Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);
    }

    private View getAnchorView() {
        return webView;
    }

    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    String urlString = webView.getUrl();
                    Global.copy(WebActivity.this, urlString);
                    Toast.makeText(WebActivity.this, urlString + " 已复制", Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(WebActivity.this, "用浏览器打开失败", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
            mRightTopPopupWindow.dismiss();
        }
    };

}
