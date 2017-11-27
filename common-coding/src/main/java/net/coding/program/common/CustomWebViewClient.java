package net.coding.program.common;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

/**
 * Created by chenchao on 2017/11/22.
 * todo delete
 */
public class CustomWebViewClient extends WebViewClient {

    private final Context mContext;

    public CustomWebViewClient(Context context, String content) {
        mContext = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

//        URLSpanNoUnderline.openActivityByUri(mContext, url, false, true);
        return true;
    }
}
