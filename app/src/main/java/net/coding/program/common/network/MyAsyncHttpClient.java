package net.coding.program.common.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

/**
 * Created by chaochen on 14-10-8.
 */
public class MyAsyncHttpClient {

    public static AsyncHttpClient createClient(Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
//        去除 ssl 验证
//        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, new TrustManager[]{new CustomX509TrustManager()},
//                    new SecureRandom());
//            SSLSocketFactory ssf = new CustomSSLSocketFactory(sslContext);
//            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//            Scheme scheme = new Scheme("https", ssf, 443);
//            client.getHttpClient().getConnectionManager().getSchemeRegistry()
//                    .register(scheme);
//
//        } catch (Exception e) {
//            Global.errorLog(e);
//        }

        return client;
    }

}
