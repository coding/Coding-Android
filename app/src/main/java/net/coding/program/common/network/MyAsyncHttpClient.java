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
        String userAgentValue = String.format("android %d %s coding_android", Build.VERSION.SDK_INT, versionName);
        mapHeaders.put("User-Agent", userAgentValue);
        mapHeaders.put("Accept", "*/*");
    }

    public static AsyncHttpClient createClient(Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
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

    public static HashMap<String, String> getMapHeaders() {
        return mapHeaders;
    }
}
