package net.coding.program.network;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.widget.CommonListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.cookie.Cookie;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network {

    private static final int MAX_STALE = 60 * 60 * 24 * 28; // 无网络时，设置超时为4周

    public static CodingRequest getRetrofit(Context context) {
        return getRetrofit(context, CacheType.noCache, null);
    }

    public static CodingRequest getRetrofit(Context context, CommonListView listView) {
        return getRetrofit(context, CacheType.noCache, listView);
    }

    private static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    public static String getCookie(Context context, String url) {
        String host;
        if (url.toLowerCase().startsWith(Global.HOST.toLowerCase())) {
            host = Global.HOST;
        } else {
            return "";
        }

        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = cookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie eachCookie = cookies.get(i);
            String domain = eachCookie.getDomain();
            if (domain.startsWith(".")) {
                domain = domain.substring(1, domain.length());
            }
            String itemName = eachCookie.getName();
            if ((itemName.equalsIgnoreCase("sid") || itemName.equalsIgnoreCase("eid"))
                    && host.toLowerCase().endsWith(domain.toLowerCase())) {
                return String.format("%s=%s;", eachCookie.getName(), eachCookie.getValue());
            }
        }

        return "";
    }

    public static UpQboxRequest getRetrofitLoad(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://up.qbox.me/")
//                .baseUrl("http://upload.qiniu.com/") // 用于测试环境，但感觉没必要
                .addConverterFactory(GsonConverterFactory.create())
                .client(generateClient(context, null, null, false)) // 显示 log 会导致 RequestBody 调用 2 次 writeTo
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(UpQboxRequest.class);
    }

    public static CodingRequest getRetrofit(Context context, CacheType cacheType, CommonListView listView) {
        Interceptor interceptorCookie = chain -> {
            Request request = chain.request();

            request = request.newBuilder().addHeader("accept", "application/json").build();

            String url = request.url().toString();
            // 不是 coding 和 mart 就不添加 cookie
            if (url.toLowerCase().startsWith(Global.HOST.toLowerCase())) {
                String sid = getCookie(context, url);
                Request.Builder builder = request.newBuilder()
                        .addHeader("Cookie", sid);

                // 防止 CSRF
                PersistentCookieStore cookieStore = new PersistentCookieStore(context);
                List<Cookie> cookies = cookieStore.getCookies();
                for (int i = 0; i < cookies.size(); i++) {
                    Cookie eachCookie = cookies.get(i);
                    if (eachCookie.getName().compareToIgnoreCase("XSRF-TOKEN") == 0) {
                        builder.addHeader("X-XSRF-TOKEN", eachCookie.getValue());
                        break;
                    }
                }

                HashMap<String, String> headers = MyAsyncHttpClient.getMapHeaders();
                for (String key : headers.keySet()) {
                    if (!key.equals("Referer")) {
                        builder.addHeader(key, headers.get(key));
                    }
                }

                if (url.toLowerCase().startsWith(Global.HOST.toLowerCase())) {
                    builder.addHeader("Referer", Global.HOST);
                }

                String enterpriseGK = GlobalData.getEnterpriseGK();
                if (enterpriseGK.contains("_")) {
                    builder.addHeader("Wxapp-Enterprise", enterpriseGK);
                }

                request = builder.build();
            }

            Response proceed = chain.proceed(request);
            if (request.method().equals("GET")) {
                if (cacheType == CacheType.useCache) {
                    return proceed.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=" + 0)
                            .build();
                }
            }

            return proceed;
        };

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Global.HOST_API + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(generateClient(context, interceptorCookie, cacheType, true))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        if (listView != null) {
            listView.update(context, CommonListView.Style.loading);
        }

        return retrofit.create(CodingRequest.class);
    }

    static OkHttpClient generateClient(Context context, Interceptor interceptorCookie, CacheType cacheType, boolean showLog) {
        File httpCacheDirectory = new File(context.getCacheDir(), "HttpCache");
        Cache cache = new Cache(httpCacheDirectory, 100 * 1024 * 1024);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (showLog) builder.addInterceptor(interceptor);


        builder.addNetworkInterceptor(chain -> {
            Request request = chain.request();

            if (request.method().equals("GET")) {
                if (cacheType == CacheType.onlyCache) {
                    request = request.newBuilder()
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + MAX_STALE)
                            .build();
                } else if (cacheType == CacheType.useCache) {
                    request = request.newBuilder()
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=0")
                            .build();
                }
            }

            Response proceed = chain.proceed(request);
            if (request.method().equals("GET")) {
                if (cacheType == CacheType.useCache) {
                    return proceed.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=" + 0)
                            .build();
                }
            }

            return proceed;
        });

        if (interceptorCookie != null) builder.addInterceptor(interceptorCookie);


        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return builder
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request request = chain.request();

                    if (request.method().equals("GET")) {
                        if (cacheType == CacheType.onlyCache) {
                            request = request.newBuilder()
                                    .removeHeader("Cache-Control")
                                    .addHeader("Cache-Control", "public, only-if-cached, max-stale=" + MAX_STALE)
                                    .build();
                        }
                    }

                    Response proceed = chain.proceed(request);
//                    if (request.method().equals("GET")) {
//                        if (cacheType == CacheType.useCache) {
//                            return proceed.newBuilder()
//                                    .removeHeader("Pragma")
//                                    .removeHeader("Cache-Control")
//                                    .addHeader("Cache-Control", "public, max-age=" + 0)
//                                    .build();
//                        } else if (cacheType == CacheType.noCache) {
//                            return proceed.newBuilder()
//                                    .removeHeader("Pragma")
//                                    .removeHeader("Cache-Control")
//                                    .addHeader("Cache-Control", "public, only-if-cached, max-stale=" + MAX_STALE)
//                                    .build();
//                        }
//                    }

                    if (request.method().equals("GET")) {
                        if (cacheType == CacheType.onlyCache) {
                            proceed = proceed.newBuilder()
                                    .removeHeader("Pragma")
                                    .removeHeader("Cache-Control")
                                    .addHeader("Cache-Control", "public, only-if-cached, max-stale=" + MAX_STALE)
                                    .build();
                        }
                    }

                    return proceed;
                })
                .cache(cache)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public enum CacheType {
        noCache, // 不缓存数据, 仅使用网络
        useCache, // 有网络就用网络取到的数据, 没有就用 cache
        onlyCache // 只使用 cache
    }
}
