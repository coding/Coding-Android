package net.coding.program.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.webkit.WebView;

import net.coding.program.MyApp;
import net.coding.program.WebActivity;
import net.coding.program.common.activity.WebviewDetailActivity_;
import net.coding.program.common.enter.DrawableTool;
import net.coding.program.common.enter.GifImageSpan;
import net.coding.program.common.htmltext.GrayQuoteSpan;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.login.auth.AuthListActivity;
import net.coding.program.login.auth.Login2FATipActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.GitFileObject;
import net.coding.program.user.UserDetailActivity_;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by chenchao on 2017/11/22.
 */

public class GlobalCommon {
    public static View.OnClickListener clickUser = v -> {
        String globalKey = (String) v.getTag();
        UserDetailActivity_.intent(v.getContext())
                .globalKey(globalKey)
                .start();
    };
    public static View.OnClickListener clickJumpWebView = v -> {
        Object object = v.getTag();
        if (object instanceof String) {
            WebviewDetailActivity_.intent(v.getContext())
                    .comment((String) object)
                    .start();
        }
    };

    public static int dpToPx(int dpValue) {
        return (int) (dpValue * MyApp.sScale + 0.5f);
    }

    public static int dpToPx(double dpValue) {
        return (int) (dpValue * MyApp.sScale + 0.5f);
    }

    public static int pxToDp(float pxValue) {
        return (int) (pxValue / MyApp.sScale + 0.5f);
    }

    public static void start2FAActivity(Activity activity) {
        Intent intent;
        if (AccountInfo.loadAuthDatas(activity).isEmpty()) {
            intent = new Intent(activity, Login2FATipActivity.class);
        } else {
            intent = new Intent(activity, AuthListActivity.class);
        }

        activity.startActivity(intent);
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
        webview.setWebViewClient(new WebActivity.CustomWebViewClient(webview.getContext()));

    }

    public static Spannable changeHyperlinkColor(String content) {
        return changeHyperlinkColor(content, null, null);
    }

    public static Spannable changeHyperlinkColor(String content, MyImageGetter imageGetter) {
        return changeHyperlinkColor(content, imageGetter, null);
    }

    public static Spannable changeHyperlinkColor(String content, int linkColor) {
        return changeHyperlinkColor(content, null, Global.tagHandler, linkColor);
    }

    public static Spannable changeHyperlinkColor(String content, int color, MyImageGetter imageGetter) {
        return changeHyperlinkColor(content, imageGetter, null, color);
    }

    public static Spannable changeHyperlinkColor(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        return changeHyperlinkColor(content, imageGetter, tagHandler, CodingColor.fontGreen);
    }

    public static Spannable changeHyperlinkColorMaopao(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, AssetManager assetManager) {
        Spannable s = changeHyperlinkColor(content, imageGetter, tagHandler, CodingColor.fontGreen);
        return spannToGif(s, assetManager);
    }

    public static Spannable changeHyperlinkColor(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, int color) {
        Spannable s = (Spannable) Html.fromHtml(content, imageGetter, tagHandler);
        return getCustomSpannable(color, s);
    }

    static Spannable getCustomSpannable(int color, Spannable s) {
        URLSpan[] urlSpan = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : urlSpan) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL(), color);
            s.setSpan(span, start, end, 0);
        }

        QuoteSpan quoteSpans[] = s.getSpans(0, s.length(), QuoteSpan.class);
        for (QuoteSpan span : quoteSpans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            GrayQuoteSpan grayQuoteSpan = new GrayQuoteSpan();
            s.setSpan(grayQuoteSpan, start, end, 0);
        }

        return s;
    }

    public static Spannable recentMessage(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        String parse = HtmlContent.parseToText(content);

        Spannable s = (Spannable) Html.fromHtml(parse, imageGetter, null);
        return GlobalCommon.getCustomSpannable(CodingColor.font3, s);
    }

    private static Spannable spannToGif(Spannable s, AssetManager assetManager) {
        ImageSpan[] imageSpans = s.getSpans(0, s.length(), ImageSpan.class);

        final String[] gifEmojiName = new String[]{
                "festival-emoji-01.gif",
                "festival-emoji-02.gif",
                "festival-emoji-03.gif",
                "festival-emoji-04.gif",
                "festival-emoji-05.gif",
                "festival-emoji-06.gif",
                "festival-emoji-07.gif",
                "festival-emoji-08.gif",
        };

        for (ImageSpan imageSpan : imageSpans) {
            int start = s.getSpanStart(imageSpan);
            int end = s.getSpanEnd(imageSpan);

            String imageSource = imageSpan.getSource();
            for (String endString : gifEmojiName) {
                if (imageSource.endsWith(endString)) {
                    try {
                        GifDrawable gifDrawable = new GifDrawable(assetManager, endString);
                        DrawableTool.zoomDrwable(gifDrawable, true);
                        gifDrawable.setLoopCount(100);
                        GifImageSpan gifImageSpan = new GifImageSpan(gifDrawable);
                        s.removeSpan(imageSpan);
                        s.setSpan(gifImageSpan, start, end, 0);
                    } catch (Exception e) {
                        Global.errorLog(e);
                    }
                }
            }
        }

        return s;
    }
}
