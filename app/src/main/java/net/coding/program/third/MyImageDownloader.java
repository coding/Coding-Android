package net.coding.program.third;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import net.coding.program.Global;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by chaochen on 14-10-7.
 */
public class MyImageDownloader extends BaseImageDownloader {

    public MyImageDownloader(Context context) {
        super(context);
        initSSLSocketFactory();
    }

    private SSLSocketFactory sf;

    private void initSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            sf = sslContext.getSocketFactory();

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }


    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = super.createConnection(url, extra);

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sf);
        }

        return conn;
    }

    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // Not implemented
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // Not implemented
        }
    }};


}
