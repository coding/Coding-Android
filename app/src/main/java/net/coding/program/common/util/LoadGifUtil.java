package net.coding.program.common.util;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by zjh on 2017/2/24.
 * 加载网络gif
 */

public class LoadGifUtil {
    private Context context;

    public LoadGifUtil(Context context) {
        this.context = context;
    }

    public void getGifImage(GifImageView gifImageView, String gifUrl) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        //通过URL获取字节数组
        asyncHttpClient
                .get(gifUrl, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        GifDrawable gifDrawable = null;
                        try {
                            gifDrawable = new GifDrawable(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //给该gifImageView设置动画背景图
                        gifImageView.setImageDrawable(gifDrawable);
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    }
                });
    }
}
