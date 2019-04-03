package net.coding.program.common;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.PersistentCookieStore;
import com.orhanobut.logger.Logger;
import com.readystatesoftware.viewbadger.BadgeView;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.cookie.Cookie;

/**
 * Created by cc191954 on 14-8-23.
 * 放一些公共的全局方法
 */
public class Global {

    public static final String DEFAULT_HOST = "https://coding.net";
    public static final String STAGING_HOST = "http://coding.codingprod.net";
    public static final String TESTING_HOST = "http://coding.t.codingprod.net";

    public static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd EEE");
    public static final SimpleDateFormat mDateYMDHH = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final int PHOTO_MAX_COUNT = 6;

    public static final int UPDATE_ALL_INT = 999999999;

    private static final String IMAGE_URL_SCAL = "%s?imageMogr2/thumbnail/!%s";
    private static final SimpleDateFormat sFormatToday = new SimpleDateFormat("今天 HH:mm");
    private static final SimpleDateFormat sFormatThisYear = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat sFormatOtherYear = new SimpleDateFormat("yy/MM/dd HH:mm");
    private static final SimpleDateFormat sFormatMessageToday = new SimpleDateFormat("今天");
    private static final SimpleDateFormat sFormatMessageThisYear = new SimpleDateFormat("MM/dd");
    private static final SimpleDateFormat sFormatMessageOtherYear = new SimpleDateFormat("yy/MM/dd");
    private static final String LOG_PREFIX = "coding_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;
    public static String HOST = DEFAULT_HOST;
    public static String HOST_API = HOST + "/api";
    /**
     * 语音文件存放目录
     */
    public static String sVoiceDir;
    public static SimpleDateFormat DateFormatTime = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat MonthDayFormatTime = new SimpleDateFormat("MMMdd日");
    public static SimpleDateFormat WeekFormatTime = new SimpleDateFormat("EEE");
    public static SimpleDateFormat NextWeekFormatTime = new SimpleDateFormat("下EEE");
    public static SimpleDateFormat LastWeekFormatTime = new SimpleDateFormat("上EEE");
    public static Html.TagHandler tagHandler = new Html.TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.toLowerCase().equals("code") && !opening) {
                output.append("\n\n");
            }
        }
    };
    public static DecimalFormat df = new DecimalFormat("#.00");
    private static SimpleDateFormat DayFormatTime = new SimpleDateFormat("yyyy-MM-dd");

    public static String dayFromTime(long time) {
        return DayFormatTime.format(time);
    }

    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    public static void tipCopyLink(Context context, String link) {
        if (context == null) {
            return;
        }

        if (TextUtils.isEmpty(link)) {
            Toast.makeText(context, "复制链接失败", Toast.LENGTH_LONG).show();
            return;
        }

        if (GlobalData.isEnterprise()) {
            Pattern pattern = Pattern.compile(Global.HOST + "/u/.*?/(.*)");
            Matcher matcher = pattern.matcher(link);
            if (matcher.find()) {
                link = String.format("%s/%s", Global.HOST, matcher.group(1));
            }
        }

        Global.copy(context, link);
        Toast.makeText(context, "已复制链接 " + link, Toast.LENGTH_LONG).show();
    }

    public static Spanned createBlueHtml(String begin, String middle, String end) {
        return createColorHtml(begin, middle, end, "#4F95E8");
    }

    public static Spanned createColorHtml(String s, int color) {
        return createColorHtml("", s, "", color);
    }

    public static Spanned createColorHtml(String begin, String middle, String end, int color) {
        String colorString = colorToString(color);
        return createColorHtml(begin, middle, end, colorString);
    }

    public static String colorToString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static String colorToStringNoWellNumber(int color) { // 生成的颜色，没有 # 号
        return String.format("%06X", 0xFFFFFF & color);
    }

//    public static String getAppPackage(Context context) {
//        try {
//            return context.getPackageName();
//            PackageManager pm = context.getPackageManager();
//            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
//            version.setText(packageInfo.packageName);
//        } catch (NameNotFoundException e) {}
//    }

    public static Spanned createColorHtml(String begin, String middle, String end, String color) {
        return Html.fromHtml(String.format("%s<font color=\"%s\">%s</font>%s", begin, color, middle, end));
    }

    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static Spanned createGreenHtml(String begin, String middle, String end) {
        return createColorHtml(begin, middle, end, CodingColor.fontGreen);
    }

    public static void display(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        String cookieString = "";
        for (Cookie cookie : cookieStore.getCookies()) {
            cookieString += cookie.getName() + "=" + cookie.getValue() + ";";
        }

        Log.d("", "cookie " + cookieString);
    }

    public static String getExtraString(Context context) {
        String FEED_EXTRA = "";
        if (FEED_EXTRA.isEmpty()) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo("net.coding.program", 0);
                String appVersion = pInfo.versionName;
                String phoneModel = Build.MODEL;
                FEED_EXTRA = String.format("Coding_Android/%s (Android %s; %s)", appVersion, Build.VERSION.SDK_INT, phoneModel);
            } catch (Exception e) {
            }
        }

        return FEED_EXTRA;
    }

    public static long longFromDay(String day) throws ParseException {
        final String format = "yyyy-MM-dd";
        final SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.parse(day).getTime();
    }

    public static boolean isEmptyContainSpace(EditText edit) {
        return edit.getText().toString().replace(" ", "").replace("　", "").isEmpty();
    }

    public static String dayCount(long time) {
        return DayFormatTime.format(time);
    }

    public static void errorLog(Exception e) {
        if (e == null) {
            return;
        }

        e.printStackTrace();
        Logger.e("Coding", "" + e);
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

    public static String decodeUtf8(String s) {
        try {
            return URLDecoder.decode(s, "utf-8");
        } catch (Exception e) {
            return "";
        }
    }

    public static String decodeUtf8Double(String s) {
        return decodeUtf8(decodeUtf8(s));
    }

    public static boolean isImageUri(String s1) {
        s1 = s1.toLowerCase();
        return s1.endsWith(".png")
                || s1.endsWith(".jpg")
                || s1.endsWith(".jpeg")
                || s1.endsWith(".bmp")
                || s1.endsWith(".gif");
    }

    public static void syncCookie(Context context, int iii) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = cookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = cookies.get(i);
            String value = cookie.getName() + "=" + cookie.getValue();
            cookieManager.setCookie(Global.HOST, value);
        }

        CookieSyncManager.getInstance().sync();// To get instant sync instead of waiting for the timer to trigger, the host can call this.
    }


    public static void syncCookie(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = cookieStore.getCookies();

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie eachCookie = cookies.get(i);
            cookieManager.setCookie(Global.HOST, String.format("%s=%s; Domain=%s; Path=%s",
                    eachCookie.getName(), eachCookie.getValue(), eachCookie.getDomain(),
                    eachCookie.getPath()));
        }

        CookieSyncManager.getInstance().sync();
    }

    public static void syncCookie(Context context, boolean bbb) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = cookieStore.getCookies();

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie eachCookie = cookies.get(i);
            String domain = eachCookie.getDomain();
            if (TextUtils.isEmpty(domain)) {
                final String div = "://";
                int start = Global.HOST.indexOf(div);
                domain = Global.HOST.substring(start + div.length());
            }
            String path = eachCookie.getPath();
            if (TextUtils.isEmpty(path)) {
                path = "/";
            }

            cookieManager.setCookie(Global.HOST, String.format("%s=%s; Domain=%s; Path=%s",
                    eachCookie.getName(), eachCookie.getValue(), domain,
                    path));
        }

        CookieSyncManager.getInstance().sync();
    }

    public static Drawable tintDrawable(Drawable drawable, int color) {
        ColorStateList colors = ColorStateList.valueOf(color);
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    public static void copy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String url = HtmlContent.parseReplaceHtml(content);
        cmb.setText(url);
    }

    public static String replaceAvatar(JSONObject json) {
        return replaceHeadUrl(json, "avatar");
    }

    public static String getErrorMsg(JSONObject jsonObject) {
        String s = "";
        try {
            JSONObject jsonData = jsonObject.getJSONObject("msg");
            String key = jsonData.keys().next();
            s = jsonData.getString(key);
        } catch (Exception e) {
            Global.errorLog(e);
        }

        return s;
    }

    // 用于头像，有些头像是 “/static/fruit_avatar/Fruit-12.png”
    public static String replaceHeadUrl(JSONObject json, String name) {
        String s = json.optString(name);
        return translateStaticIcon(s);
    }

    public static String translateStaticIcon(String s) {
        if (s.indexOf("/static/") == 0) {
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

    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String makeSmallUrl(ImageView view, String url) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        int max = Math.max(lp.height, lp.width);
        if (max > 0) {
            return makeSmallUrlSquare(url, max);
        }

        return url;
    }

    public static boolean canCrop(String url) {
        return url.startsWith("http") && (!url.contains("/thumbnail/"));
    }

    public static String makeSmallUrlSquare(String url, int widthPix) {
        if (canCrop(url)) {
            String parma = "";
            if (!url.contains("?imageMogr2/")) {
                parma = "?imageMogr2/";
            }
            return String.format("%s%s/!%sx%s", url, parma, widthPix, widthPix);
        } else {
            return url;
        }
    }

    public static String makeLargeUrl(String url) {
        final int MAX = 4096; // ImageView显示的图片不能大于这个数
        return String.format(IMAGE_URL_SCAL, url, MAX);
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

    public static String AUTHOR = "";

    public static Uri makeUri(Context context, File shareFile) {
        return android.support.v4.content.FileProvider.getUriForFile(context, AUTHOR, shareFile);
    }

    public static void startPhotoZoom(Activity activity, Uri uri, Uri outputUri, int requestCode) {
        final UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        int blue = 0xFF0060FF;
        options.setToolbarColor(blue);
        int white = 0xFFFFFFFF;
        options.setToolbarColor(white);
        options.setActiveWidgetColor(blue);
        options.setToolbarWidgetColor(blue);
        options.setStatusBarColor(0xFF757575);

        UCrop.of(uri, outputUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1024, 1024)
                .withOptions(options)
                .start(activity, requestCode);
    }

    public static void startPhotoZoom(Activity activity, Uri uri, Uri outputUri, int width, int height, int requestCode) {
        final UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        int blue = 0xFF0060FF;
        options.setToolbarColor(blue);
        int white = 0xFFFFFFFF;
        options.setToolbarColor(white);
        options.setActiveWidgetColor(blue);
        options.setToolbarWidgetColor(blue);
        options.setStatusBarColor(0xFF757575);

        UCrop.of(uri, outputUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(width, height)
                .withOptions(options)
                .start(activity, requestCode);
    }


    public static void startPhotoZoom(Fragment fragment, Activity activity, Uri uri, Uri outputUri, int requestCode) {
        final UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        int blue = 0xFF0060FF;
        options.setToolbarColor(blue);
        int white = 0xFFFFFFFF;
        options.setToolbarColor(white);
        options.setActiveWidgetColor(blue);
        options.setToolbarWidgetColor(blue);
        options.setStatusBarColor(0xFF757575);

        UCrop.of(uri, outputUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1024, 1024)
                .withOptions(options)
                .start(activity, fragment, requestCode);
    }

    static public String readTextFile(Context context, String assetFile) throws IOException {
        InputStream inputStream = context.getAssets().open(assetFile);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        inputStream.close();

        return outputStream.toString();
    }

    static public String readAssets(Context context, String assetFile) {
        try {
            return readTextFile(context, assetFile);
        } catch (Exception e) {
            Global.errorLog(e);
        }
        return "";
    }

    static public String readTextFile(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            return readTextFile(is);
        } catch (Exception e) {
            Global.errorLog(e);
        }

        return "";
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

    public static void initWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        // 防止webview滚动时背景变成黑色
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            webView.setBackgroundColor(0x00000000);
        } else {
            webView.setBackgroundColor(Color.argb(1, 0, 0, 0));
        }

        settings.setDefaultTextEncodingName("UTF-8");
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
        } catch (Exception e) {
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
//        String dataString = getDay(timeInMillis, false);
//        if (dataString == null) {
//            dataString = getWeek(timeInMillis);
//            if (dataString == null) {
//                dataString = MonthDayFormatTime.format(timeInMillis);
//            }
//        }
//
//        return String.format("%s %s", dataString, DateFormatTime.format(new Date(timeInMillis)));
        return dayToNow(timeInMillis, true);
    }

    public static String dayToNowCreate(long time) {
        return "创建于 " + Global.dayToNow(time);
    }

    public static String dayToNow(long time) {
        return dayToNow(time, true);
    }

    public static String dayToNow(long time, boolean displayHour) {
        long nowMill = System.currentTimeMillis();

        long minute = (nowMill - time) / 60000;
        if (minute < 60) {
            if (minute <= 0) {
                return "几秒前"; // 由于手机时间的原因，有时候会为负，这时候显示1秒前
            } else {
                return minute + "分钟前";
            }
        }

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        int year = calendar.get(GregorianCalendar.YEAR);
        int month = calendar.get(GregorianCalendar.MONTH);
        int day = calendar.get(GregorianCalendar.DAY_OF_MONTH);

        calendar.setTimeInMillis(nowMill);
        Long timeObject = new Long(time);
        if (calendar.get(GregorianCalendar.YEAR) != year) { // 不是今年
            SimpleDateFormat sFormatOtherYear = displayHour ? Global.sFormatOtherYear : Global.sFormatMessageOtherYear;
            return sFormatOtherYear.format(timeObject);
        } else if (calendar.get(GregorianCalendar.MONTH) != month
                || calendar.get(GregorianCalendar.DAY_OF_MONTH) != day) { // 今年
            SimpleDateFormat sFormatThisYear = displayHour ? Global.sFormatThisYear : Global.sFormatMessageThisYear;
            return sFormatThisYear.format(timeObject);
        } else { // 今天
            SimpleDateFormat sFormatToday = displayHour ? Global.sFormatToday : Global.sFormatMessageToday;
            return sFormatToday.format(timeObject);
        }
    }

    public static String simpleDayByNow(long time) {
        long nowMill = System.currentTimeMillis();

        long minute = (nowMill - time) / 60000;
        if (minute < 60) {
            if (minute <= 0) {
                return "几秒前"; // 由于手机时间的原因，有时候会为负，这时候显示1秒前
            } else {
                return minute + "分钟前";
            }
        } else {
            long hour = minute / 60;
            if (hour < 24) {
                return hour + "小时前";
            } else {
                long day = hour / 24 + (hour % 24 > 0 ? 1 : 0);
                if (day < 30) {
                    return day + "天前";
                } else {
                    long month = day / 30;
                    if (month < 12) {
                        return month + "个月前";
                    } else {
                        return month / 12 + "年前";
                    }
                }
            }
        }
    }
    public static String dayToNow(long time, String template) {
        return String.format(template, dayToNow(time));
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
        return df.format(size) + " " + units[i];
    }

    public static void setBadgeView(BadgeView badge, int count) {
        if (count > 0) {
            String countString = count > 99 ? "99+" : ("" + count);
            badge.setText(countString);
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.INVISIBLE);
        }
    }

    public static void updateByMarket(Context context) {
        try {
            Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            if (GlobalData.isEnterprise()) {
                Toast.makeText(context, "软件市场里暂时没有找到 Coding 企业版", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "软件市场里暂时没有找到Coding", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
