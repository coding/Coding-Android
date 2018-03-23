package net.coding.program.third;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import net.coding.program.common.Global;
import net.coding.program.common.network.MyAsyncHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;

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

        if (url.toLowerCase().startsWith(Global.HOST.toLowerCase())) {
            conn.setRequestProperty("Cookie", MyAsyncHttpClient.getLoginCookie(context));
        }

        return conn;
    }

}
