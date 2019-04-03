package net.coding.program.thirdplatform;

import android.app.Activity;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

/**
 * Created by chenchao on 2017/12/20.
 */

public class ThirdPlatformLogin {

    public static void loginByWeixin(Activity activity, UMAuthListener authListener) {
        UMShareAPI.get(activity).getPlatformInfo(activity, SHARE_MEDIA.WEIXIN, authListener);
    }

    public static void loginOut(Activity activity) {
        UMShareAPI.get(activity).isAuthorize(activity, SHARE_MEDIA.WEIXIN);
        UMShareAPI.get(activity).deleteOauth(activity, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {

            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {

            }
        });

    }

}
