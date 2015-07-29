package net.coding.program.common.network;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.common.Global;
import net.coding.program.common.network.apache.CustomSSLSocketFactory;
import net.coding.program.common.network.apache.CustomX509TrustManager;
import net.coding.program.model.AccountInfo;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Created by chaochen on 14-10-8.
 * 对 AsyncHttpClient 做了一些公共操作
 */
public class MyAsyncHttpClient {

    public static AsyncHttpClient createClient(Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
        if (!Global.HOST.equals(Global.DEFAULT_HOST)) {
            // 去除 ssl 验证
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new CustomX509TrustManager()},
                        new SecureRandom());
                SSLSocketFactory ssf = new CustomSSLSocketFactory(sslContext);
                ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                Scheme scheme = new Scheme("https", ssf, 443);
                client.getHttpClient().getConnectionManager().getSchemeRegistry()
                        .register(scheme);
            } catch (Exception e) {
                Global.errorLog(e);
            }


            AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(context);
            client.addHeader("Authorization", customHost.getCode());
        }

        String versionName = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (Exception e) {
            Global.errorLog(e);
        }

        final String userAgent = String.format("android %d %s", Build.VERSION.SDK_INT, versionName);
        client.addHeader("User-Agent", userAgent);
        client.addHeader("Referer", "https://coding.net");
        return client;
    }
}
