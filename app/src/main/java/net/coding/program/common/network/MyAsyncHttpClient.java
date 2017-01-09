package net.coding.program.common.network;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import net.coding.program.common.Global;
import net.coding.program.common.util.LogUtils;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.RequestData;

import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

import static net.coding.program.common.util.LogUtils.makeLogTag;

/**
 * Created by chaochen on 14-10-8.
 * 对 AsyncHttpClient 做了一些公共操作
 */
public class MyAsyncHttpClient {

    private static final String TAG = makeLogTag(MyAsyncHttpClient.class);

    public static void put(Context context, String url, RequestParams params, ResponseHandlerInterface response) {
        LogUtils.LOGD(TAG, "put " + url);
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
        client.put(context, url, params, response);
    }

    public static void post(Context context, String url, RequestParams params, ResponseHandlerInterface response) {
        LogUtils.LOGD(TAG, "post " + url);
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
        client.post(context, url, params, response);
    }

    public static void post(Context context, String url, ResponseHandlerInterface response) {
        LogUtils.LOGD(TAG, "post " + url);
        post(context, url, new RequestParams(), response);
    }

    public static void get(Context context, String url, ResponseHandlerInterface response) {
        LogUtils.LOGD(TAG, "get " + url);
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
        client.get(context, url, response);
    }

    public static void get(Context context, RequestData requestData, ResponseHandlerInterface response) {

        LogUtils.LOGD(TAG, "get " + requestData.url);
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
        client.get(context, requestData.url, requestData.params, response);
    }

    public static void delete(Context context, String url, ResponseHandlerInterface response) {
        LogUtils.LOGD(TAG, "delete " + url);
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
        client.delete(context, url, response);
    }

    private static HashMap<String, String> mapHeaders = new HashMap<>();

    public static void init(Context context) {
        mapHeaders.clear();
        mapHeaders.put("Referer", "https://coding.net");

        String versionName = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (Exception e) {
            Global.errorLog(e);
        }

        String userAgentValue = String.format("coding_android/%s (%s)", versionName, Build.VERSION.SDK_INT);

        mapHeaders.put("User-Agent", userAgentValue);
        mapHeaders.put("Accept", "*/*");
    }

    public static AsyncHttpClient createClient(Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);

        // todo 临时，方便进入生产环境
        BasicClientCookie devCookie = new BasicClientCookie("e_edv", "1");
        devCookie.setDomain(".coding.net");
        cookieStore.addCookie(devCookie);

        client.setCookieStore(cookieStore);

        if (!Global.HOST.equals(Global.DEFAULT_HOST)) {
            AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(context);
            client.addHeader("Authorization", customHost.getCode()); // 有可能会有密码
        }

        for (String item : mapHeaders.keySet()) {
            client.addHeader(item, mapHeaders.get(item));
        }

        client.setTimeout(60 * 1000);
        return client;
    }

    public static String getCookie(Context context, String url) {
        String host;
        if (url.startsWith(Global.HOST)) {
            host = Global.HOST;
        } else if (url.startsWith(Global.HOST)) {
            host = Global.HOST;
        } else {
            return "";
        }

        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = cookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie eachCookie = cookies.get(i);
            String domain = eachCookie.getDomain();
            if (domain == null) {
                domain = "";
            }
            if (domain.startsWith(".")) {
                domain = domain.substring(1, domain.length());
            }
            if (eachCookie.getName().toLowerCase().equals("sid") && host.endsWith(domain)) {
                return String.format("%s=%s;", eachCookie.getName(), eachCookie.getValue());
            }
        }

        return "";
    }

    public static HashMap<String, String> getMapHeaders() {
        return mapHeaders;
    }
}
