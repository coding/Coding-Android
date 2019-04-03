package net.coding.program.common;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.view.View;

import net.coding.program.common.enter.GifImageSpan;
import net.coding.program.common.htmltext.GrayQuoteSpan;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.route.URLSpanNoUnderline;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by chenchao on 2017/11/22.
 */
public class GlobalCommon {

    public static Map<String, Class<?>> rounterMap = new HashMap<>();
    public static String ROUNTER_2FA = "ROUNTER_2FA";
    public static String ROUNTER_AUTH_LIST = "ROUNTER_AUTH_LIST";

    public static View.OnClickListener clickJumpWebView;
    public static View.OnClickListener mOnClickUser;

    public static int dpToPx(int dpValue) {
        return (int) (dpValue * GlobalData.sScale + 0.5f);
    }

    public static int dpToPx(double dpValue) {
        return (int) (dpValue * GlobalData.sScale + 0.5f);
    }

    public static int pxToDp(float pxValue) {
        return (int) (pxValue / GlobalData.sScale + 0.5f);
    }

    public static void start2FAActivity(Activity activity) {
        Intent intent;
        if (AccountInfo.loadAuthDatas(activity).isEmpty()) {
            intent = new Intent(activity, rounterMap.get(ROUNTER_2FA));
        } else {
            intent = new Intent(activity, rounterMap.get(ROUNTER_AUTH_LIST));
        }

        activity.startActivity(intent);
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
        return changeHyperlinkColor(content, imageGetter, tagHandler, CodingColor.select2);
    }

    public static Spannable changeHyperlinkColorMaopao(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, AssetManager assetManager) {
        Spannable s = changeHyperlinkColor(content, imageGetter, tagHandler, CodingColor.select2);
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
                        zoomDrwable(gifDrawable, true);
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

    // todo 删除
    public static void zoomDrwable(Drawable drawable, boolean isMonkey) {
        int width = isMonkey ? GlobalData.sEmojiMonkey : GlobalData.sEmojiNormal;
        drawable.setBounds(0, 0, width, width);
    }

    public static String mbToRmb(BigDecimal point) {
        String rmbString = String.valueOf(point.multiply(new BigDecimal(50)));
        if (rmbString.endsWith(".00")) {
            rmbString = rmbString.substring(0, rmbString.length() - 3);
        } else if (rmbString.endsWith(".0")) {
            rmbString = rmbString.substring(0, rmbString.length() - 2);
        }
        return "¥" + rmbString;
    }
}
