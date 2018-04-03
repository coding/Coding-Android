package net.coding.program;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.coding.program.common.HtmlContent;
import net.coding.program.pickphoto.detail.ImagePagerActivity_;
import net.coding.program.route.URLSpanNoUnderline;

import java.util.ArrayList;

/**
 * Created by chenchao on 2017/11/27.
 */
public class CustomWebViewClientOpenNew extends WebViewClient {

    Context mContext;
    private ArrayList<String> mUris;

    public CustomWebViewClientOpenNew(Context context) {
        mContext = context;
    }

    public CustomWebViewClientOpenNew(Context context, String content) {
        mContext = context;
        mUris = HtmlContent.parseMessage(content).uris;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (mUris != null) {
            for (int i = 0; i < mUris.size(); ++i) {
                if (mUris.get(i).equals(url)) {
                    ImagePagerActivity_.intent(mContext)
                            .mArrayUri(mUris)
                            .mPagerPosition(i)
                            .start();
                    return true;
                }
            }
        }

        URLSpanNoUnderline.openActivityByUri(mContext, url, true, true);
        return true;
    }
}
