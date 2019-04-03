package net.coding.program;

import android.content.Context;
import android.webkit.WebView;

import net.coding.program.common.Global;
import net.coding.program.common.model.GitFileObject;

/**
 * Created by chenchao on 2017/11/28.
 */

public class CodingGlobal {
    public enum WebviewType {
        markdown("markdown.html", "${webview_content}");

        public final String asset;
        public final String replace;

        WebviewType(String asset, String replace) {
            this.asset = asset;
            this.replace = replace;
        }
    }

    static public void setWebViewContent(WebView webView, WebviewType type, String content) {
        Context context = webView.getContext();
        Global.initWebView(webView);
        content = content.replace("{{CodingUrl}}", "");
        webView.setWebViewClient(new CustomWebViewClientOpenNew(context, content));
        try {
            Global.syncCookie(webView.getContext());
            String bubble = Global.readTextFile(context.getAssets().open(type.asset));
            webView.loadDataWithBaseURL(Global.HOST, bubble.replace(type.replace, content), "text/html", "UTF-8", null);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }


    static public void setWebViewContent(WebView webView, String tempate, String content) {
        Context context = webView.getContext();
        content = content.replace("{{CodingUrl}}", "");
        Global.initWebView(webView);
        webView.setWebViewClient(new CustomWebViewClientOpenNew(context, content));
        try {
            Global.syncCookie(webView.getContext());
            String bubble = Global.readTextFile(context.getAssets().open(tempate));
            webView.loadDataWithBaseURL(Global.HOST, bubble.replace("${webview_content}", content), "text/html", "UTF-8", null);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    static public void setWebViewContent(WebView webView, String tempate, String replaceString,
                                         String content, String replaceComment, String comment) {
        Context context = webView.getContext();
        Global.initWebView(webView);
        webView.setWebViewClient(new CustomWebViewClientOpenNew(context, content));
        try {
            Global.syncCookie(webView.getContext());
            String bubble = Global.readTextFile(context.getAssets().open(tempate));
            webView.loadDataWithBaseURL(Global.HOST, bubble.replace(replaceString, content).replace(replaceComment, comment), "text/html", "UTF-8", null);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    static public void setWebViewContent(WebView webview, GitFileObject gitFile) {
        Context context = webview.getContext();
        if (gitFile.lang.equals("markdown")) {
            try {
                String template = Global.readTextFile(context.getAssets().open("markdown.html"));
                webview.loadDataWithBaseURL(Global.HOST, template.replace("${webview_content}", gitFile.preview), "text/html", "UTF-8", null);

            } catch (Exception e) {
                Global.errorLog(e);
            }
        } else {
            try {
                String template = Global.readTextFile(context.getAssets().open("code.html"));
                String replaceData = gitFile.data.replace("<", "&lt;").replace(">", "&gt;").replace("\u2028", "").replace("\u2029", "");
                webview.loadDataWithBaseURL(Global.HOST, template.replace("${file_code}", replaceData).replace("${file_lang}", gitFile.lang), "text/html", "UTF-8", null);
            } catch (Exception e) {
                Global.errorLog(e);
            }
        }
        webview.setWebViewClient(new CustomWebViewClient(webview.getContext()));

    }
}
