package net.coding.program;

import android.content.Context;

import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

/**
 * Created by chenchao on 15/9/7.
 * 第三方的 key，需要自己申请添加
 */
public class AllThirdKeys {

    // 分享到qq的key
    public static final String QQ_APP_ID = "1103192918";
    public static final String QQ_APP_KEY = "GmvQSB3JYtrUeQVm";

    // 分享到weixin的key 企业版
//    public static final String WX_APP_ID = "wxe900bf9f02a39867";
//    public static final String WX_APP_KEY = "c596424d983ec313d52c6bf9d0690124";


    // 分享到weixin的key
    public static final String WX_APP_ID = "wx6ef6df9e9706b1bd";
    public static final String WX_APP_KEY = "a54ee69fa26ece290fd92ac2897cc438";

    // 百度地图key
    public static final String geotable = "96880";
    public static final String ak = "ak046T8G6m1GvIPGKdOdq04D";
    public static final String sk = "8bEsFsyaAUZRS1XVIyqywGnBlitSyjj5";

    static final String WEIBO_KEY = "600921819";
    static final String WEIBO_SECRET = "65c17448203f0b0dda096556cdb91e85";
    static final String WEIBO_CALLBACK_URL = "http://sns.whalecloud.com/coding/phone/callback";

    public static void initInApp(Context context) {
        PlatformConfig.setWeixin(WX_APP_ID, WX_APP_KEY);
        PlatformConfig.setQQZone(QQ_APP_ID, QQ_APP_KEY);
        PlatformConfig.setSinaWeibo(WEIBO_KEY, WEIBO_SECRET, WEIBO_CALLBACK_URL);

        UMShareAPI.get(context);
    }

}

