package net.coding.program.third;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import net.coding.program.common.Global;

import org.apache.http.cookie.Cookie;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Created by chaochen on 14-10-7.
 */
public class MyImageDownloader extends BaseImageDownloader {

    public MyImageDownloader(Context context) {
        super(context);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = super.createConnection(url, extra);

        if (url.startsWith(Global.HOST)) {
            PersistentCookieStore cookieStore = new PersistentCookieStore(context);
            List<Cookie> cookies = cookieStore.getCookies();

            String sid = "";
            for (Cookie item : cookies) {
                if (item.getName().equals("sid")) {
                    sid = "sid=" + item.getValue();
                    break;
                }
            }

            conn.setRequestProperty("Cookie", sid);
        }

        return conn;
    }

}
