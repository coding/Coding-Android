package net.coding.program.common;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.ImageView;

import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.MyApp;
import net.coding.program.common.htmltext.GrayQuoteSpan;
import net.coding.program.common.htmltext.URLSpanNoUnderline;

import org.apache.http.cookie.Cookie;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by cc191954 on 14-8-23.
 */
public class Global {

    public static final String HOST = "https://coding.net";

    public static SimpleDateFormat DateFormatTime = new SimpleDateFormat("HH:mm");

    private static SimpleDateFormat DayFormatTime = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat MonthDayFormatTime = new SimpleDateFormat("MMMdd日");

    public static SimpleDateFormat WeekFormatTime = new SimpleDateFormat("EEE");
    public static SimpleDateFormat NextWeekFormatTime = new SimpleDateFormat("下EEE");
    public static SimpleDateFormat LastWeekFormatTime = new SimpleDateFormat("上EEE");

    public static String dayFromTime(long time) {
        return DayFormatTime.format(time);
    }

    public static long longFromDay(String day) throws ParseException {
        final String format = "yyyy-MM-dd";
        final SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.parse(day).getTime();
    }

    public static String dayCount(long time) {
        return DayFormatTime.format(time);
    }

    public static void errorLog(Exception e) {
        e.printStackTrace();
        Log.e("", "" + e);
    }

    public static String encodeInput(String at, String input) {
        if (at == null || at.isEmpty()) {
            return input;
        }

        return String.format("@%s %s", at, input);
    }

    public static String encodeUtf8(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (Exception e) {
            return "";
        }
    }

    public static void initWebView(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);

        // 防止webview滚动时背景变成黑色
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            webView.setBackgroundColor(0x00000000);
        } else {
            webView.setBackgroundColor(Color.argb(1, 0, 0, 0));
        }

        webView.getSettings().setDefaultTextEncodingName("UTF-8");
    }

    public static boolean isImageUri(String s1) {
        s1 = s1.toLowerCase();
        return s1.endsWith(".png")
                || s1.endsWith(".jpg")
                || s1.endsWith(".jpeg")
                || s1.endsWith(".bmp")
                || s1.endsWith(".gif");
    }

    public static void syncCookie(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = cookieStore.getCookies();

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie eachCookie = cookies.get(i);
            String cookieString = eachCookie.getName() + "=" + eachCookie.getValue();
            cookieManager.setCookie(Global.HOST, cookieString);
        }

        CookieSyncManager.getInstance().sync();

    }

    public static void copy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content);
    }

    public static String replaceAvatar(JSONObject json) {
        return replaceUrl(json, "avatar");
    }

    public static String getErrorMsg(JSONObject jsonObject) {
        String s = "";
        try {
            JSONObject jsonData = jsonObject.getJSONObject("msg");
            String key = (String) jsonData.keys().next();
            s = jsonData.getString(key);
        } catch (Exception e) {
            Global.errorLog(e);
        }

        return s;
    }

    public static String replaceUrl(JSONObject json, String name) {
        String s = json.optString(name);
        if (s.indexOf("/static") == 0) {
            return Global.HOST + s;
        }

        return s;
    }

    public static void popSoftkeyboard(Context ctx, View view, boolean wantPop) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (wantPop) {
            view.requestFocus();
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static Html.TagHandler tagHandler = new Html.TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.toLowerCase().equals("code") && !opening) {
                output.append("\n\n");
            }
        }
    };

    private static final String IMAGE_URL_SCAL = "%s?imageMogr2/thumbnail/!%s";

    public static String makeSmallUrl(ImageView view, String url) {
        String realUrl = url.split("\\?")[0];

        if (url.indexOf("http") == 0) {
            // 头像再裁剪需要算坐标，就不改参数了
            // https://dn-coding-net-production-static.qbox.me/c28b97dd-61f2-41d4-bd7e-b04f0c634751.jpg?imageMogr2/auto-orient/format/jpeg/crop/!164x164a568a38
            if (url.contains("/crop/")) {
                return url;
            }

            ViewGroup.LayoutParams lp = view.getLayoutParams();
            String width = intToString(lp.width);
            String height = intToString(lp.height);

            // 如果初始化的时候没有长宽，默认取高度为200dp缩略图
            if (width.isEmpty() && height.isEmpty()) {
//                height = String.valueOf(Global.dpToPx(200));
                width = String.valueOf(Global.dpToPx(200));

            }
            return String.format(IMAGE_URL_SCAL, realUrl, width);
        } else {
            return realUrl;
        }
    }


    // 7牛图片接口，用法如下
    // http://developer.qiniu.com/docs/v6/api/reference/fop/image/imageview2.html
//    private static final String IMAGE_URL_CROP = "%s/?imageView2/4/w/%s/h/%s";
//    public static String makeSmallUrl(ImageView view, String url, int minWidth) {
//        String realUrl = url.split("\\?")[0];
//
//        if (url.indexOf("http") == 0) {
//            // 头像再裁剪需要算坐标，就不改参数了
//            // https://dn-coding-net-production-static.qbox.me/c28b97dd-61f2-41d4-bd7e-b04f0c634751.jpg?imageMogr2/auto-orient/format/jpeg/crop/!164x164a568a38
//            if (url.contains("/crop/")) {
//                return url;
//            }
//
//            ViewGroup.LayoutParams lp = view.getLayoutParams();
//
//            String width = intToString(Math.max(lp.width, lp.height));
//            String height = intToString(Math.min(lp.width, lp.height));
//
//            // 如果初始化的时候没有长宽，默认取高度为200dp缩略图
//            if (width.isEmpty() && height.isEmpty()) {
//                height = String.valueOf(Global.dpToPx(200));
//            }
//            return String.format(IMAGE_URL_CROP, realUrl, width, height);
//        } else {
//            return realUrl;
//        }
//
//    }

    public static String makeLargeUrl(String url) {
        final int MAX = 4096; // ImageView显示的图片不能大于这个数
        return String.format(IMAGE_URL_SCAL, url, MAX, MAX);
    }

    private static String intToString(int length) {
        String width;
        if (length > 0) {
            width = String.valueOf(length);
        } else {
            width = "";
        }

        return width;
    }

    public static int dpToPx(int dpValue) {
        return (int) (dpValue * MyApp.sScale + 0.5f);
    }

    public static int pxToDp(float pxValue) {
        return (int) (pxValue / MyApp.sScale + 0.5f);
    }

    public static Spannable changeHyperlinkColor(String content) {
        return Global.changeHyperlinkColor(content, null, null);
    }

    public static Spannable changeHyperlinkColor(String content, int color, MyImageGetter imageGetter) {
        return Global.changeHyperlinkColor(content, imageGetter, null);
    }

    public static Spannable changeHyperlinkColor(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        return changeHyperlinkColor(content, imageGetter, tagHandler, 0xFF3BBD79);
    }

    public static Spannable changeHyperlinkColor(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, int color) {
        Spannable s = (Spannable) Html.fromHtml(content, imageGetter, tagHandler);
        return getCustomSpannable(color, s);
    }

    public static Spannable recentMessage(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        String parse = HtmlContent.parseToText(content);

        Spannable s = (Spannable) Html.fromHtml(parse, imageGetter, null);
        return getCustomSpannable(0xff999999, s);
    }

    private static Spannable getCustomSpannable(int color, Spannable s) {
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

    static public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            Global.errorLog(e);
        }

        return outputStream.toString();
    }

    public static boolean isGif(String uri) {
        return uri.toLowerCase().endsWith(".gif");
    }

    // 通过文件头来判断是否gif
    public static boolean isGifByFile(File file) {
        try {
            int length = 10;
            InputStream is = new FileInputStream(file);
            byte[] data = new byte[length];
            is.read(data);
            String type = getType(data);
            is.close();

            if (type.equals("gif")) {
                return true;
            }
        } catch (IOException e) {
            Global.errorLog(e);
        }

        return false;
    }

    private static String getType(byte[] data) {
        String type = "";
        if (data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            type = "png";
        } else if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
            type = "gif";
        } else if (data[6] == 'J' && data[7] == 'F' && data[8] == 'I'
                && data[9] == 'F') {
            type = "jpg";
        }
        return type;
    }

    private static String getDay(long time, boolean showToday) {
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.set(calendarToday.get(Calendar.YEAR), calendarToday.get(Calendar.MONTH), calendarToday.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        final long oneDay = 1000 * 3600 * 24;
        long today = calendarToday.getTimeInMillis();
        long tomorrow = today + oneDay;
        long tomorrowNext = tomorrow + oneDay;
        long tomorrowNextNext = tomorrowNext + oneDay;
        long yesterday = today - oneDay;
        long lastYesterday = yesterday - oneDay;

        if (time >= today) {
            if (tomorrow > time) {
                if (showToday) {
                    return "今天";
                } else {
                    return "";
                }
            } else if (tomorrowNext > time) {
                return "明天";
            } else if (tomorrowNextNext > time) {
                return "后天";
            }
        } else {
            if (time > yesterday) {
                return "昨天";
            } else if (time > lastYesterday) {
                return "前天";
            }
        }

        return null;
    }

    private static String getWeek(long time) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        final long oneWeek = 1000 * 60 * 60 * 24 * 7;

        long weekBegin = today.getTimeInMillis();
        long nextWeekBegin = weekBegin + oneWeek;
        long nextnextWeekBegin = nextWeekBegin + oneWeek;
        long lastWeekBegin = weekBegin - oneWeek;

        if (time >= weekBegin) {
            if (nextWeekBegin > time) {
                return WeekFormatTime.format(time);
            } else if (nextnextWeekBegin > time) {
                return NextWeekFormatTime.format(time);
            }
        } else {
            if (time > lastWeekBegin) {
                return LastWeekFormatTime.format(time);
            }
        }
        return null;
    }

    public static String getDataDetail(long timeInMillis) {
        String dataString = getDay(timeInMillis, true);
        if (dataString == null) {
            dataString = getWeek(timeInMillis);
            if (dataString == null) {
                dataString = MonthDayFormatTime.format(timeInMillis);
            }
        }
        return dataString;
    }

    public static String getTimeDetail(long timeInMillis) {
        String dataString = getDay(timeInMillis, false);
        if (dataString == null) {
            dataString = getWeek(timeInMillis);
            if (dataString == null) {
                dataString = MonthDayFormatTime.format(timeInMillis);
            }
        }

        return String.format("%s %s", dataString, DateFormatTime.format(new Date(timeInMillis)));
    }

    public static class MessageParse {
        public String text = "";
        public ArrayList<String> uris = new ArrayList<>();

        public String toString() {
            String s = "text " + text + "\n";
            for (int i = 0; i < uris.size(); ++i) {
                s += uris.get(i) + "\n";
            }
            return s;
        }
    }

    public static String dayToNow(long time) {
        Calendar now = Calendar.getInstance();

        long minute = (now.getTimeInMillis() - time) / 60000;
        if (minute < 60) {
            if (minute == 0) {
                return "刚刚";
            } else {
                return minute + "分钟前";
            }
        }

        long hour = minute / 60;
        if (hour < 24) {
            return hour + "小时前";
        }

        long day = hour / 24;
        if (day < 30) {
            return day + "天前";
        }

        long month = day / 30;
        if (month < 12) {
            return month + "个月前";
        }

        long year = month / 12;
        return year + "年前";
    }

    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activityNetwork = mConnectivityManager.getActiveNetworkInfo();
            return activityNetwork != null && activityNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public static boolean isConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static DecimalFormat df = new java.text.DecimalFormat("#.00");

    /**
     * 显示文件大小,保留两位
     */
    public static String HumanReadableFilesize(double size) {
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        double mod = 1024.0;
        int i = 0;
        while (size >= mod) {
            size /= mod;
            i++;
        }
        //return Math.round(size) + units[i];
        return df.format(size) + " " + units[i];
    }
}
