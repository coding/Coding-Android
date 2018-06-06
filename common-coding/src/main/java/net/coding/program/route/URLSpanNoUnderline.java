package net.coding.program.route;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import net.coding.program.common.Global;

/**
 * Created by chaochen on 15/1/12.
 * 用来解析 url 以跳转到不同的界面
 */
public class URLSpanNoUnderline extends URLSpan {

    // TODO: 2018/6/6  需要修改
    public static final String PATTERN_URL_MESSAGE = "^(?:https?://[\\w.]*)?/user/messages/history/([\\w.-]+)$";
    private int color;

    public URLSpanNoUnderline(String url, int color) {
        super(url);
        this.color = color;
    }

    public static String createMessageUrl(String globalKey) {
        return Global.HOST + "/user/messages/history/" + globalKey;
    }

    public static void openActivityByUri(Context context, String uriString, boolean newTask) {
        openActivityByUri(context, uriString, newTask, true);
    }


    public static boolean openActivityByUri(Context context, String uriString, boolean newTask, boolean defaultIntent) {
        return openActivityByUri(context, uriString, newTask, defaultIntent, false);
    }

    public static URLCallback urlCallback;

    public interface URLCallback {
        boolean openActivityByUri(Context context, String uri, boolean newTask, boolean defaultIntent, boolean share);
    }

    public static boolean openActivityByUri(Context context, String uri, boolean newTask, boolean defaultIntent, boolean share) {
        return urlCallback.openActivityByUri(context, uri, newTask, defaultIntent, share);
    }

    public static String generateAbsolute(String jumpUrl) {
        if (jumpUrl == null) {
            return "";
        }

        String url = jumpUrl.replace("/u/", "/user/")
                .replace("/p/", "/project/");

        if (url.startsWith("/")) {
            url = Global.HOST_API + url;
        } else if (url.toLowerCase().startsWith(Global.HOST.toLowerCase())
                && !url.toLowerCase().startsWith(Global.HOST_API.toLowerCase())) {
            url = url.substring(Global.HOST.length());
            url = Global.HOST_API + url;
        }

        return url;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(color);
    }

    @Override
    public void onClick(View widget) {
        openActivityByUri(widget.getContext(), getURL(), false);
    }
}
