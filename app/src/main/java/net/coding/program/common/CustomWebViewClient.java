package net.coding.program.common;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.coding.program.route.URLSpanNoUnderline;

import java.util.ArrayList;

/**
 * Created by chenchao on 2017/11/27.
 */
public class CustomWebViewClient extends WebViewClient {

    Context mContext;
    private ArrayList<String> mUris;

    public CustomWebViewClient(Context context) {
        mContext = context;
    }

    public CustomWebViewClient(Context context, String content) {
        mContext = context;
        mUris = HtmlContent.parseMessage(content).uris;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO: 2017/11/22  未实现
//            if (mUris != null) {
//        for (int i = 0; i < mUris.size(); ++i) {
//            if (mUris.get(i).equals(url)) {
//                ImagePagerActivity_.intent(mContext)
//                        .mArrayUri(mUris)
//                        .mPagerPosition(i)
//                        .start();
//                return true;
//            }
//        }
//            }
//
        boolean openActivity = URLSpanNoUnderline.openActivityByUri(mContext, url, false, false);
        if (!openActivity) {
            view.loadUrl(url);
        }

        return !openActivity;
    }
}
